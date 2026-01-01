import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import {
  getUserOrThrow,
  requireAuth,
  requireNotBlocked,
  assertLinkedParent,
  isBlockedBetween,
  isNewUser,
} from "../utils/authz";

/**
 * Teen2 (browser) creates a shortlist entry for their parent to review.
 * Flow: Teen2 browses → taps "Ask Parent to Connect" → creates shortlist
 * Parent sees shortlist → taps "Connect" → creates BookingRequest
 *
 * Uses deterministic doc ID to prevent duplicates: shortlist_{teenId}_{postId}
 */
export const createShortlist = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  // Must be a teen with a linked parent - returns parent ID if valid
  const parentId = assertLinkedParent(user);

  // Rate limit: 20 shortlists per day (stricter for new users)
  await enforceRateLimit({
    key: `createShortlist:${uid}`,
    windowSec: 86400,
    max: 20,
    isNewUser: isNewUser(user),
    newUserMax: 5, // New users can only create 5 shortlists/day
  });

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

  // Check if blocked
  if (await isBlockedBetween(uid, sl_providerTeenId)) {
    throw new HttpsError("permission-denied", "Cannot interact with this user");
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
  const noteRes = await filterTextOrReject(String(data.sl_note || ""), {
    maxLen: 200,
    mode: "STRICT",
  });
  if (!noteRes.ok) {
    throw new HttpsError("invalid-argument", `Note blocked: ${noteRes.reason}`);
  }

  // Deterministic doc ID prevents duplicates
  const shortlistDocId = `shortlist_${uid}_${sl_postId}`;
  const shortlistRef = admin.firestore().collection("shortlists").doc(shortlistDocId);

  // Use transaction to check for existing and create atomically
  const result = await admin.firestore().runTransaction(async (tx) => {
    const existingSnap = await tx.get(shortlistRef);

    if (existingSnap.exists) {
      const existing = existingSnap.data()!;
      // If already exists and not dismissed, return existing
      if (existing.sl_status !== "dismissed") {
        return { shortlistId: shortlistDocId, existing: true };
      }
      // If dismissed, allow re-creation by updating
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    const shortlistDoc = {
      sl_parentId: parentId,
      sl_childTeenId: uid,
      sl_providerTeenId,
      sl_postId,
      sl_postTitle: post.pst_title ?? "",
      sl_note: noteRes.sanitized,
      sl_status: "pending",
      sl_createdAt: existingSnap.exists ? existingSnap.data()!.sl_createdAt : now,
      sl_updatedAt: now,
    };

    tx.set(shortlistRef, shortlistDoc);
    return { shortlistId: shortlistDocId, existing: false };
  });

  return { ok: true, shortlistId: result.shortlistId, existing: result.existing };
});
