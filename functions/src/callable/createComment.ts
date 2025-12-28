import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

export const createComment = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

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

  // Verify post exists
  const postSnap = await admin.firestore().collection("posts").doc(postId).get();
  if (!postSnap.exists) {
    throw new HttpsError("not-found", "Post not found");
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
