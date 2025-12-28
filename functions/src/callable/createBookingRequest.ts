import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked, requireRole } from "../utils/authz";

const DELIVERY_MODES = new Set(["in_person", "online"]);
const MEETING_PREFERENCES = new Set(["public_place", "online", "parent_will_share_privately"]);

async function isBlockedBetween(a: string, b: string): Promise<boolean> {
  const [s1, s2] = await Promise.all([
    admin.firestore().collection("blocks").doc(`${a}_${b}`).get(),
    admin.firestore().collection("blocks").doc(`${b}_${a}`).get(),
  ]);
  return s1.exists || s2.exists;
}

/**
 * Parent creates a booking request to a Teen1 (provider).
 * Only parents can create booking requests.
 */
export const createBookingRequest = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);
  requireRole(user, "parent");

  await enforceRateLimit({ key: `createBookingRequest:${uid}`, windowSec: 86400, max: 10 });

  const data = req.data ?? {};
  const br_postId = String(data.br_postId || "");
  if (!br_postId) {
    throw new HttpsError("invalid-argument", "br_postId required");
  }

  // Verify post exists and is active
  const postSnap = await admin.firestore().collection("posts").doc(br_postId).get();
  if (!postSnap.exists) {
    throw new HttpsError("not-found", "Post not found");
  }
  const post = postSnap.data()!;
  if (!post.pst_isActive) {
    throw new HttpsError("failed-precondition", "Post inactive");
  }

  const br_providerId = String(post.pst_authorId || "");
  if (!br_providerId) {
    throw new HttpsError("failed-precondition", "Post missing authorId");
  }

  // Check blocks
  if (await isBlockedBetween(uid, br_providerId)) {
    throw new HttpsError("permission-denied", "Blocked user");
  }

  // Validate structured fields
  const br_deliveryMode = String(data.br_deliveryMode || "online");
  const br_preferredDays = Array.isArray(data.br_preferredDays)
    ? data.br_preferredDays.map(String)
    : [];
  const br_timeWindow = String(data.br_timeWindow || "");
  const br_meetingPreference = String(data.br_meetingPreference || "online");

  if (!DELIVERY_MODES.has(br_deliveryMode)) {
    throw new HttpsError("invalid-argument", "Invalid br_deliveryMode");
  }
  if (!MEETING_PREFERENCES.has(br_meetingPreference)) {
    throw new HttpsError("invalid-argument", "Invalid br_meetingPreference");
  }
  if (!br_timeWindow) {
    throw new HttpsError("invalid-argument", "br_timeWindow required");
  }
  if (br_preferredDays.length === 0) {
    throw new HttpsError("invalid-argument", "br_preferredDays required");
  }

  // Filter notes
  const notesRes = await filterTextOrReject(String(data.br_notes || ""), { maxLen: 300 });
  if (!notesRes.ok) {
    throw new HttpsError("invalid-argument", `Notes blocked: ${notesRes.reason}`);
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const br_requesterSnapshot = {
    publicName: user.usr_publicName,
    avatarUrl: user.usr_avatarUrl ?? null,
    verificationStatus: user.usr_verificationStatus ?? "unverified",
  };

  const bookingDoc = {
    br_postId,
    br_postTitle: post.pst_title ?? "",
    br_providerId,
    br_requesterId: uid,
    br_requesterSnapshot,
    br_status: "pending",
    br_deliveryMode,
    br_preferredDays,
    br_timeWindow,
    br_meetingPreference,
    br_notes: notesRes.sanitized,
    br_createdAt: now,
    br_respondedAt: null,
    br_updatedAt: now,
  };

  const ref = admin.firestore().collection("bookingRequests").doc();
  await ref.set(bookingDoc);

  return { ok: true, requestId: ref.id };
});
