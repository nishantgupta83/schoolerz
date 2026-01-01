import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import {
  getUserOrThrow,
  requireAuth,
  requireNotBlocked,
  requireRole,
  requireVerified,
  createAuthorSnapshot,
  isBlockedBetween,
  isNewUser,
} from "../utils/authz";

const DELIVERY_MODES = new Set(["in_person", "online"]);
const MEETING_PREFERENCES = new Set(["public_place", "online", "parent_will_share_privately"]);

// Cooldown period after decline (24 hours in milliseconds)
const DECLINE_COOLDOWN_MS = 24 * 60 * 60 * 1000;

/**
 * Check if requester has a pending request for this post
 */
async function hasPendingRequest(requesterId: string, postId: string): Promise<boolean> {
  const existing = await admin.firestore()
    .collection("bookingRequests")
    .where("br_requesterId", "==", requesterId)
    .where("br_postId", "==", postId)
    .where("br_status", "==", "pending")
    .limit(1)
    .get();
  return !existing.empty;
}

/**
 * Check if requester is in cooldown after being declined
 */
async function isInDeclineCooldown(requesterId: string, postId: string): Promise<boolean> {
  const cooldownCutoff = admin.firestore.Timestamp.fromDate(
    new Date(Date.now() - DECLINE_COOLDOWN_MS)
  );

  const recentDeclined = await admin.firestore()
    .collection("bookingRequests")
    .where("br_requesterId", "==", requesterId)
    .where("br_postId", "==", postId)
    .where("br_status", "==", "declined")
    .where("br_respondedAt", ">", cooldownCutoff)
    .limit(1)
    .get();

  return !recentDeclined.empty;
}

/**
 * Parent creates a booking request to a Teen1 (provider).
 * Only parents can create booking requests.
 * Prevents duplicate pending requests and enforces cooldown after decline.
 */
export const createBookingRequest = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);
  requireRole(user, "parent");
  requireVerified(user); // Must be verified to create booking

  // Apply rate limit with stricter limits for new users
  await enforceRateLimit({
    key: `createBookingRequest:${uid}`,
    windowSec: 86400,
    max: 10,
    isNewUser: isNewUser(user),
    newUserMax: 3, // New users can only create 3 requests/day
  });

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

  // Verify post author is a teen (Teen1 provider model)
  const authorSnapshot = post.pst_authorSnapshot;
  if (authorSnapshot?.role !== "teen") {
    throw new HttpsError("failed-precondition", "Can only book services from teen providers");
  }

  const br_providerId = String(post.pst_authorId || "");
  if (!br_providerId) {
    throw new HttpsError("failed-precondition", "Post missing authorId");
  }

  // Check blocks
  if (await isBlockedBetween(uid, br_providerId)) {
    throw new HttpsError("permission-denied", "Cannot interact with this user");
  }

  // Prevent duplicate pending requests (1 pending per post per requester)
  if (await hasPendingRequest(uid, br_postId)) {
    throw new HttpsError("failed-precondition", "You already have a pending request for this post");
  }

  // Check cooldown after decline
  if (await isInDeclineCooldown(uid, br_postId)) {
    throw new HttpsError("failed-precondition", "Please wait before requesting again");
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
  const notesRes = await filterTextOrReject(String(data.br_notes || ""), {
    maxLen: 300,
    mode: "STRICT",
  });
  if (!notesRes.ok) {
    throw new HttpsError("invalid-argument", `Notes blocked: ${notesRes.reason}`);
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const br_requesterSnapshot = createAuthorSnapshot(user);

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
