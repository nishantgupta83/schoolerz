import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

async function isBlockedBetween(a: string, b: string): Promise<boolean> {
  const [s1, s2] = await Promise.all([
    admin.firestore().collection("blocks").doc(`${a}_${b}`).get(),
    admin.firestore().collection("blocks").doc(`${b}_${a}`).get(),
  ]);
  return s1.exists || s2.exists;
}

export const createComment = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  // Rate limit: 20 comments per hour
  await enforceRateLimit({ key: `createComment:${uid}`, windowSec: 3600, max: 20 });

  const data = req.data ?? {};
  const postId = String(data.postId || "");
  const cm_text = String(data.cm_text || "");

  if (!postId) {
    throw new HttpsError("invalid-argument", "postId required");
  }

  const textRes = await filterTextOrReject(cm_text, { maxLen: 500 });
  if (!textRes.ok) {
    throw new HttpsError("invalid-argument", textRes.reason);
  }

  // Verify post exists and get author
  const postSnap = await admin.firestore().collection("posts").doc(postId).get();
  if (!postSnap.exists) {
    throw new HttpsError("not-found", "Post not found");
  }

  const post = postSnap.data()!;
  const postAuthorId = post.pst_authorId as string;

  // Check blocks between commenter and post author
  if (postAuthorId && await isBlockedBetween(uid, postAuthorId)) {
    throw new HttpsError("permission-denied", "Cannot comment on this post");
  }

  const cm_authorSnapshot = {
    publicName: user.usr_publicName,
    avatarUrl: user.usr_avatarUrl ?? null,
    role: user.usr_role,
  };

  const commentDoc = {
    cm_authorId: uid,
    cm_authorSnapshot,
    cm_text: textRes.sanitized,
    cm_createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  const ref = admin.firestore()
    .collection("posts")
    .doc(postId)
    .collection("comments")
    .doc();
  await ref.set(commentDoc);

  // Increment comment count atomically
  await admin.firestore().collection("posts").doc(postId).update({
    pst_commentCount: admin.firestore.FieldValue.increment(1),
  });

  return { ok: true, commentId: ref.id };
});
