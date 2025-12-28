import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

const SERVICE_TYPES = new Set([
  "lawn_care",
  "babysitting",
  "dog_walking",
  "tutoring",
  "tech_help",
  "errands",
  "other",
]);
const PRICE_TYPES = new Set(["hourly", "fixed", "free"]);

async function assertZipAllowed(zipcode: string): Promise<void> {
  const snap = await admin.firestore().collection("config").doc("allowedZipcodes").get();
  const zips = (snap.data()?.zipcodes ?? []) as string[];
  if (!zips.includes(zipcode)) {
    throw new HttpsError("invalid-argument", "Zipcode not allowed for rollout");
  }
}

export const createPost = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  await enforceRateLimit({ key: `createPost:${uid}`, windowSec: 3600, max: 5 });

  const data = req.data ?? {};
  const pst_type = String(data.pst_type || "");
  const pst_serviceType = String(data.pst_serviceType || "");
  const pst_priceType = String(data.pst_priceType || "");
  const pst_zipcode = String(data.pst_zipcode || "");
  const pst_neighborhoodId = String(data.pst_neighborhoodId || "");

  if (!["offer", "request"].includes(pst_type)) {
    throw new HttpsError("invalid-argument", "Invalid post type");
  }
  if (!SERVICE_TYPES.has(pst_serviceType)) {
    throw new HttpsError("invalid-argument", "Invalid serviceType");
  }
  if (!PRICE_TYPES.has(pst_priceType)) {
    throw new HttpsError("invalid-argument", "Invalid priceType");
  }
  if (!pst_zipcode || !pst_neighborhoodId) {
    throw new HttpsError("invalid-argument", "zipcode and neighborhoodId required");
  }

  await assertZipAllowed(pst_zipcode);

  const titleRes = await filterTextOrReject(String(data.pst_title || ""), { maxLen: 80 });
  if (!titleRes.ok) {
    throw new HttpsError("invalid-argument", `Title blocked: ${titleRes.reason}`);
  }

  const bodyRes = await filterTextOrReject(String(data.pst_body || ""), { maxLen: 800 });
  if (!bodyRes.ok) {
    throw new HttpsError("invalid-argument", `Body blocked: ${bodyRes.reason}`);
  }

  const pst_priceMin = data.pst_priceMin == null ? null : Number(data.pst_priceMin);
  const pst_priceMax = data.pst_priceMax == null ? null : Number(data.pst_priceMax);

  if (pst_priceType !== "free") {
    if (!(Number.isFinite(pst_priceMin) && pst_priceMin! >= 0)) {
      throw new HttpsError("invalid-argument", "priceMin required");
    }
    if (!(Number.isFinite(pst_priceMax) && pst_priceMax! >= pst_priceMin!)) {
      throw new HttpsError("invalid-argument", "priceMax must be >= priceMin");
    }
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const pst_authorSnapshot = {
    publicName: user.usr_publicName,
    avatarUrl: user.usr_avatarUrl ?? null,
    verificationStatus: user.usr_verificationStatus ?? "unverified",
    role: user.usr_role,
  };

  const postDoc = {
    pst_type,
    pst_authorId: uid,
    pst_authorSnapshot,
    pst_title: titleRes.sanitized,
    pst_body: bodyRes.sanitized,
    pst_serviceType,
    pst_priceType,
    pst_priceMin: pst_priceType === "free" ? null : pst_priceMin,
    pst_priceMax: pst_priceType === "free" ? null : pst_priceMax,
    pst_neighborhoodId,
    pst_zipcode,
    pst_imageUrls: [],
    pst_likeCount: 0,
    pst_commentCount: 0,
    pst_isActive: true,
    pst_createdAt: now,
    pst_updatedAt: now,
  };

  const ref = admin.firestore().collection("posts").doc();
  await ref.set(postDoc);

  return { ok: true, postId: ref.id };
});
