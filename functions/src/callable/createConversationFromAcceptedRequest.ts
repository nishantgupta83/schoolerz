import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

/**
 * Create a conversation when a booking request is accepted.
 * Called automatically or manually after Teen1 accepts a request.
 * Participants: Parent (requester) + Teen1 (provider)
 * Uses transaction to prevent duplicate conversations.
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

  // Use a deterministic doc ID based on booking request to prevent duplicates
  const convDocId = `conv_${requestId}`;
  const convRef = admin.firestore().collection("conversations").doc(convDocId);

  // Use transaction to prevent race conditions creating duplicate conversations
  const result = await admin.firestore().runTransaction(async (tx) => {
    const existingSnap = await tx.get(convRef);

    if (existingSnap.exists) {
      // Return existing conversation
      return { conversationId: convDocId, existing: true };
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
        [br.br_providerId]: {},
      },
      cv_lastMessageAt: null,
      cv_lastMessagePreview: null,
      cv_messageCount: 0,
      cv_createdAt: now,
      cv_updatedAt: now,
    };

    tx.set(convRef, conversationDoc);
    return { conversationId: convDocId, existing: false };
  });

  return { ok: true, ...result };
});
