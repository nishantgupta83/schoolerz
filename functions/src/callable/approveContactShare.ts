import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

/**
 * Approve or decline a pending contact share request.
 * In the recommended flow:
 * 1. Parent requests contact share → status: "pending"
 * 2. Teen1's parent approves → status: "approved"
 *
 * For MVP, requestContactShare sets status to "shared" directly.
 * This function is for future use when approval flow is enabled.
 */
export const approveContactShare = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  const data = req.data ?? {};
  const shareId = String(data.shareId || "");
  const approve = Boolean(data.approve);

  if (!shareId) {
    throw new HttpsError("invalid-argument", "shareId required");
  }

  const shareRef = admin.firestore().collection("contactShares").doc(shareId);
  const shareSnap = await shareRef.get();
  if (!shareSnap.exists) {
    throw new HttpsError("not-found", "Contact share not found");
  }

  const share = shareSnap.data()!;

  // Verify status is pending
  if (share.cs_status !== "pending") {
    throw new HttpsError("failed-precondition", "Contact share is not pending");
  }

  // Get the booking request to find the provider's parent
  const brRef = admin.firestore()
    .collection("bookingRequests")
    .doc(share.cs_bookingRequestId);
  const brSnap = await brRef.get();
  if (!brSnap.exists) {
    throw new HttpsError("not-found", "Booking request not found");
  }

  const br = brSnap.data()!;
  const providerId = br.br_providerId as string;

  // Get provider user doc to check parent
  const providerSnap = await admin.firestore()
    .collection("users")
    .doc(providerId)
    .get();

  if (!providerSnap.exists) {
    throw new HttpsError("not-found", "Provider not found");
  }

  const provider = providerSnap.data()!;

  // For teens, their parent can approve
  // For parents acting as providers, they approve themselves
  const canApprove =
    uid === providerId || // Provider themselves
    uid === provider.usr_parentUserId; // Provider's parent

  if (!canApprove) {
    throw new HttpsError("permission-denied", "Not authorized to approve");
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const newStatus = approve ? "approved" : "declined";

  await shareRef.update({
    cs_status: newStatus,
    cs_respondedBy: uid,
    cs_respondedAt: now,
  });

  // If approved, add system message to conversation
  if (approve) {
    const convSnap = await admin.firestore()
      .collection("conversations")
      .where("cv_bookingRequestId", "==", share.cs_bookingRequestId)
      .limit(1)
      .get();

    if (!convSnap.empty) {
      const convRef = convSnap.docs[0].ref;
      const shareType = share.cs_type as string;
      const systemMsg = {
        msg_senderId: "system",
        msg_type: "contact_approved",
        msg_text: `Contact ${shareType} approved for sharing`,
        msg_metadata: { shareType, shareId },
        msg_createdAt: now,
      };
      await convRef.collection("messages").add(systemMsg);
      await convRef.update({
        cv_lastMessageAt: now,
        cv_lastMessagePreview: `Contact ${shareType} approved`,
        cv_messageCount: admin.firestore.FieldValue.increment(1),
      });
    }
  }

  return { ok: true, status: newStatus };
});
