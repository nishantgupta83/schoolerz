import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

/**
 * Create a conversation when a booking request is accepted.
 * Called automatically or manually after Teen1 accepts a request.
 * Participants: Parent (requester) + Teen1 (provider)
 */
export const createConversationFromAcceptedRequest = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  const data = req.data ?? {};
  const requestId = String(data.requestId || "");

  if (!requestId) {
    throw new HttpsError("invalid-argument", "requestId required");
  }

  const brRef = admin.firestore().collection("bookingRequests").doc(requestId);
  const brSnap = await brRef.get();
  if (!brSnap.exists) {
    throw new HttpsError("not-found", "Booking request not found");
  }

  const br = brSnap.data()!;

  // Verify booking is accepted
  if (br.br_status !== "accepted") {
    throw new HttpsError("failed-precondition", "Booking must be accepted to create conversation");
  }

  // Only provider (Teen1) or requester (Parent) can create conversation
  const isProvider = br.br_providerId === uid;
  const isRequester = br.br_requesterId === uid;
  if (!isProvider && !isRequester) {
    throw new HttpsError("permission-denied", "Not a participant in this booking");
  }

  // Check if conversation already exists for this booking
  const existingConv = await admin.firestore()
    .collection("conversations")
    .where("cv_bookingRequestId", "==", requestId)
    .limit(1)
    .get();

  if (!existingConv.empty) {
    // Return existing conversation
    return { ok: true, conversationId: existingConv.docs[0].id, existing: true };
  }

  // Participants: Parent (requester) + Teen1 (provider)
  const participants = [br.br_requesterId, br.br_providerId];

  const now = admin.firestore.FieldValue.serverTimestamp();
  const conversationDoc = {
    cv_bookingRequestId: requestId,
    cv_postId: br.br_postId,
    cv_postTitle: br.br_postTitle ?? "",
    cv_participants: participants,
    cv_participantSnapshots: {
      [br.br_requesterId]: br.br_requesterSnapshot ?? {},
      [br.br_providerId]: {}, // Provider snapshot fetched separately if needed
    },
    cv_lastMessageAt: null,
    cv_lastMessagePreview: null,
    cv_messageCount: 0,
    cv_createdAt: now,
    cv_updatedAt: now,
  };

  const convRef = admin.firestore().collection("conversations").doc();
  await convRef.set(conversationDoc);

  return { ok: true, conversationId: convRef.id, existing: false };
});
