import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import {
  getUserOrThrow,
  requireAuth,
  requireNotBlocked,
  requireVerified,
  createAuthorSnapshot,
  isNewUser,
} from "../utils/authz";

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
  requireVerified(user); // Must be verified to post

  // Apply rate limit with stricter limits for new users
  await enforceRateLimit({
    key: `createPost:${uid}`,
    windowSec: 3600,
    max: 5,
    isNewUser: isNewUser(user),
    newUserMax: 2, // New users can only create 2 posts/hour
  });

  const data = req.data ?? {};
  const pst_type = String(data.pst_type || "");
  const pst_serviceType = String(data.pst_serviceType || "");
  const pst_priceType = String(data.pst_priceType || "");
  const pst_zipcode = String(data.pst_zipcode || "");
  const pst_neighborhoodId = String(data.pst_neighborhoodId || "");

  // Validate post type
  if (!["offer", "request"].includes(pst_type)) {
    throw new HttpsError("invalid-argument", "Invalid post type");
  }

  // Role-based post type validation
  // Teen1 (provider) creates "offer" posts
  // Parent creates "request" posts
  if (pst_type === "offer" && user.usr_role !== "teen") {
    throw new HttpsError("permission-denied", "Only teens can create service offers");
  }
  if (pst_type === "request" && user.usr_role !== "parent") {
    throw new HttpsError("permission-denied", "Only parents can create service requests");
  }

  // Validate service type
  if (!SERVICE_TYPES.has(pst_serviceType)) {
    throw new HttpsError("invalid-argument", "Invalid serviceType");
  }

  // Validate price type
  if (!PRICE_TYPES.has(pst_priceType)) {
    throw new HttpsError("invalid-argument", "Invalid priceType");
  }

  // Validate location
  if (!pst_zipcode || !pst_neighborhoodId) {
    throw new HttpsError("invalid-argument", "zipcode and neighborhoodId required");
  }

  await assertZipAllowed(pst_zipcode);

  // Validate and filter title
  const titleRes = await filterTextOrReject(String(data.pst_title || ""), {
    maxLen: 80,
    mode: "STRICT",
  });
  if (!titleRes.ok) {
    throw new HttpsError("invalid-argument", `Title blocked: ${titleRes.reason}`);
  }

  // Validate and filter body
  const bodyRes = await filterTextOrReject(String(data.pst_body || ""), {
    maxLen: 800,
    mode: "STRICT",
  });
  if (!bodyRes.ok) {
    throw new HttpsError("invalid-argument", `Body blocked: ${bodyRes.reason}`);
  }

  // Validate price
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

  // imageUrls MUST be empty on create (controlled upload later)
  // Ignore any imageUrls passed by client
  const pst_imageUrls: string[] = [];

  const now = admin.firestore.FieldValue.serverTimestamp();
  const pst_authorSnapshot = createAuthorSnapshot(user);

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
    pst_imageUrls,
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
