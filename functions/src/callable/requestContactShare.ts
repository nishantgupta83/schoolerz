import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked, requireRole } from "../utils/authz";

const SHARE_TYPES = new Set(["phone", "email"]);

/**
 * Parent requests to share contact info with Teen1 (provider).
 * This is a button action, not free-text - reduces PII exposure.
 * Creates a contactShare record for tracking/audit.
 */
export const requestContactShare = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);
  requireRole(user, "parent"); // Only parents can share contact

  const data = req.data ?? {};
  const bookingRequestId = String(data.bookingRequestId || "");
  const shareType = String(data.shareType || "");

  if (!bookingRequestId) {
    throw new HttpsError("invalid-argument", "bookingRequestId required");
  }
  if (!SHARE_TYPES.has(shareType)) {
    throw new HttpsError("invalid-argument", "Invalid shareType (phone or email)");
  }

  // Verify booking request exists and is accepted/completed
  const brRef = admin.firestore().collection("bookingRequests").doc(bookingRequestId);
  const brSnap = await brRef.get();
  if (!brSnap.exists) {
    throw new HttpsError("not-found", "Booking request not found");
  }

  const br = brSnap.data()!;

  // Only requester (parent) can share contact
  if (br.br_requesterId !== uid) {
    throw new HttpsError("permission-denied", "Only requester can share contact");
  }

  // Booking must be accepted or completed
  if (!["accepted", "completed"].includes(br.br_status)) {
    throw new HttpsError("failed-precondition", "Booking must be accepted or completed");
  }

  const toUserId = br.br_providerId as string;

  // Check if already shared this type
  const existing = await admin.firestore()
    .collection("contactShares")
    .where("cs_bookingRequestId", "==", bookingRequestId)
    .where("cs_fromUserId", "==", uid)
    .where("cs_type", "==", shareType)
    .limit(1)
    .get();

  if (!existing.empty) {
    return { ok: true, shareId: existing.docs[0].id, alreadyShared: true };
  }

  // Create contact share record
  const now = admin.firestore.FieldValue.serverTimestamp();
  const shareDoc = {
    cs_bookingRequestId: bookingRequestId,
    cs_fromUserId: uid,
    cs_toUserId: toUserId,
    cs_type: shareType,
    cs_status: "shared", // Direct share for MVP; could be "pending" if approval needed
    cs_createdAt: now,
  };

  const shareRef = admin.firestore().collection("contactShares").doc();
  await shareRef.set(shareDoc);

  // Optionally: Add system message to conversation
  const convSnap = await admin.firestore()
    .collection("conversations")
    .where("cv_bookingRequestId", "==", bookingRequestId)
    .limit(1)
    .get();

  if (!convSnap.empty) {
    const convRef = convSnap.docs[0].ref;
    const systemMsg = {
      msg_senderId: "system",
      msg_type: "contact_shared",
      msg_text: `${user.usr_publicName} shared their ${shareType}`,
      msg_metadata: { shareType, shareId: shareRef.id },
      msg_createdAt: now,
    };
    await convRef.collection("messages").add(systemMsg);
    await convRef.update({
      cv_lastMessageAt: now,
      cv_lastMessagePreview: `Contact ${shareType} shared`,
      cv_messageCount: admin.firestore.FieldValue.increment(1),
    });
  }

  return { ok: true, shareId: shareRef.id, alreadyShared: false };
});
