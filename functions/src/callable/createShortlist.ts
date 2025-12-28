import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

/**
 * Teen2 (browser) creates a shortlist entry for their parent to review.
 * Flow: Teen2 browses → taps "Ask Parent to Connect" → creates shortlist
 * Parent sees shortlist → taps "Connect" → creates BookingRequest
 */
export const createShortlist = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  // Must be a teen with a linked parent
  if (user.usr_role !== "teen") {
    throw new HttpsError("permission-denied", "Only teens can create shortlists");
  }
  if (!user.usr_parentUserId) {
    throw new HttpsError("failed-precondition", "No parent linked to account");
  }

  const data = req.data ?? {};
  const sl_postId = String(data.sl_postId || "");
  const sl_providerTeenId = String(data.sl_providerTeenId || "");

  if (!sl_postId) {
    throw new HttpsError("invalid-argument", "sl_postId required");
  }
  if (!sl_providerTeenId) {
    throw new HttpsError("invalid-argument", "sl_providerTeenId required");
  }
  if (sl_providerTeenId === uid) {
    throw new HttpsError("invalid-argument", "Cannot shortlist yourself");
  }

  // Verify post exists and is active
  const postSnap = await admin.firestore().collection("posts").doc(sl_postId).get();
  if (!postSnap.exists) {
    throw new HttpsError("not-found", "Post not found");
  }
  const post = postSnap.data()!;
  if (!post.pst_isActive) {
    throw new HttpsError("failed-precondition", "Post is inactive");
  }
  if (post.pst_authorId !== sl_providerTeenId) {
    throw new HttpsError("invalid-argument", "Provider does not match post author");
  }

  // Filter note if provided
  const noteRes = await filterTextOrReject(String(data.sl_note || ""), { maxLen: 200 });
  if (!noteRes.ok) {
    throw new HttpsError("invalid-argument", `Note blocked: ${noteRes.reason}`);
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const shortlistDoc = {
    sl_parentId: user.usr_parentUserId,
    sl_childTeenId: uid,
    sl_providerTeenId,
    sl_postId,
    sl_postTitle: post.pst_title ?? "",
    sl_note: noteRes.sanitized,
    sl_status: "pending",
    sl_createdAt: now,
    sl_updatedAt: now,
  };

  const ref = admin.firestore().collection("shortlists").doc();
  await ref.set(shortlistDoc);

  return { ok: true, shortlistId: ref.id };
});
