import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

export const blockUser = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  const blockedId = String(req.data?.blockedId || "");
  if (!blockedId || blockedId === uid) {
    throw new HttpsError("invalid-argument", "Invalid blockedId");
  }

  const blockDocId = `${uid}_${blockedId}`;
  await admin.firestore().collection("blocks").doc(blockDocId).set({
    blk_blockerId: uid,
    blk_blockedId: blockedId,
    blk_createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { ok: true };
});

export const unblockUser = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const blockedId = String(req.data?.blockedId || "");
  if (!blockedId || blockedId === uid) {
    throw new HttpsError("invalid-argument", "Invalid blockedId");
  }

  const blockDocId = `${uid}_${blockedId}`;
  await admin.firestore().collection("blocks").doc(blockDocId).delete();

  return { ok: true };
});
