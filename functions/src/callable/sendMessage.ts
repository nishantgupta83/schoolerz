import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

/**
 * Send a message in a conversation.
 * Rules:
 * - Only Parent + Teen1 (provider) can message
 * - Teen2 (browser) CANNOT message
 * - Conversation must be tied to an accepted booking
 * - All messages filtered for PII/links
 */
export const sendMessage = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  // Check if user is chat restricted
  if (user.usr_isChatRestricted) {
    throw new HttpsError("permission-denied", "Chat restricted");
  }

  // Rate limit: 30 messages/hour
  await enforceRateLimit({ key: `sendMessage:${uid}`, windowSec: 3600, max: 30 });

  const data = req.data ?? {};
  const conversationId = String(data.conversationId || "");
  const text = String(data.text || "");

  if (!conversationId) {
    throw new HttpsError("invalid-argument", "conversationId required");
  }
  if (!text.trim()) {
    throw new HttpsError("invalid-argument", "text required");
  }

  // Filter text for PII/links/blocked content
  const textRes = await filterTextOrReject(text, { maxLen: 500 });
  if (!textRes.ok) {
    throw new HttpsError("invalid-argument", `Message blocked: ${textRes.reason}`);
  }

  // Verify conversation exists
  const convRef = admin.firestore().collection("conversations").doc(conversationId);
  const convSnap = await convRef.get();
  if (!convSnap.exists) {
    throw new HttpsError("not-found", "Conversation not found");
  }

  const conv = convSnap.data()!;

  // Verify user is a participant
  const participants = conv.cv_participants as string[];
  if (!participants.includes(uid)) {
    throw new HttpsError("permission-denied", "Not a participant");
  }

  // Verify booking context exists and is accepted
  const bookingRequestId = conv.cv_bookingRequestId as string;
  if (!bookingRequestId) {
    throw new HttpsError("failed-precondition", "Conversation has no booking context");
  }

  const brSnap = await admin.firestore()
    .collection("bookingRequests")
    .doc(bookingRequestId)
    .get();

  if (!brSnap.exists) {
    throw new HttpsError("failed-precondition", "Booking request not found");
  }

  const br = brSnap.data()!;

  // Verify booking is still in a valid state (accepted or completed)
  if (!["accepted", "completed"].includes(br.br_status)) {
    throw new HttpsError("failed-precondition", "Booking must be accepted or completed to chat");
  }

  // Check blocks between participants
  const otherParticipant = participants.find((p) => p !== uid);
  if (otherParticipant) {
    const [block1, block2] = await Promise.all([
      admin.firestore().collection("blocks").doc(`${uid}_${otherParticipant}`).get(),
      admin.firestore().collection("blocks").doc(`${otherParticipant}_${uid}`).get(),
    ]);
    if (block1.exists || block2.exists) {
      throw new HttpsError("permission-denied", "Blocked user");
    }
  }

  // Create message
  const now = admin.firestore.FieldValue.serverTimestamp();
  const messageDoc = {
    msg_senderId: uid,
    msg_senderSnapshot: {
      publicName: user.usr_publicName,
      avatarUrl: user.usr_avatarUrl ?? null,
      role: user.usr_role,
    },
    msg_type: "text",
    msg_text: textRes.sanitized,
    msg_createdAt: now,
  };

  const msgRef = convRef.collection("messages").doc();

  // Update conversation metadata in batch
  const batch = admin.firestore().batch();
  batch.set(msgRef, messageDoc);
  batch.update(convRef, {
    cv_lastMessageAt: now,
    cv_lastMessagePreview: textRes.sanitized.substring(0, 50),
    cv_messageCount: admin.firestore.FieldValue.increment(1),
    cv_updatedAt: now,
  });

  await batch.commit();

  return { ok: true, messageId: msgRef.id };
});
