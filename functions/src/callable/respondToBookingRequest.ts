import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

const ALLOWED_STATUSES = new Set(["accepted", "declined", "completed", "cancelled"]);

/**
 * Respond to a booking request.
 * - Provider (Teen1) can: accept, decline (when pending)
 * - Requester (Parent) can: cancel (when pending/accepted), complete (when accepted)
 */
export const respondToBookingRequest = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  const data = req.data ?? {};
  const requestId = String(data.requestId || "");
  const newStatus = String(data.newStatus || "");

  if (!requestId) {
    throw new HttpsError("invalid-argument", "requestId required");
  }
  if (!newStatus) {
    throw new HttpsError("invalid-argument", "newStatus required");
  }
  if (!ALLOWED_STATUSES.has(newStatus)) {
    throw new HttpsError("invalid-argument", "Invalid status");
  }

  const ref = admin.firestore().collection("bookingRequests").doc(requestId);
  const snap = await ref.get();
  if (!snap.exists) {
    throw new HttpsError("not-found", "Booking request not found");
  }

  const br = snap.data()!;
  const isProvider = br.br_providerId === uid;
  const isRequester = br.br_requesterId === uid;

  if (!isProvider && !isRequester) {
    throw new HttpsError("permission-denied", "Not a participant");
  }

  // Provider can: accept/decline (when pending)
  if (newStatus === "accepted" || newStatus === "declined") {
    if (!isProvider) {
      throw new HttpsError("permission-denied", "Only provider can accept/decline");
    }
    if (br.br_status !== "pending") {
      throw new HttpsError("failed-precondition", "Only pending can be accepted/declined");
    }
  }

  // Requester can: cancel (when pending/accepted)
  if (newStatus === "cancelled") {
    if (!isRequester) {
      throw new HttpsError("permission-denied", "Only requester can cancel");
    }
    if (!["pending", "accepted"].includes(br.br_status)) {
      throw new HttpsError("failed-precondition", "Only pending/accepted can be cancelled");
    }
  }

  // Requester can: complete (when accepted)
  if (newStatus === "completed") {
    if (!isRequester) {
      throw new HttpsError("permission-denied", "Only requester can complete");
    }
    if (br.br_status !== "accepted") {
      throw new HttpsError("failed-precondition", "Only accepted can be completed");
    }
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const updateData: Record<string, any> = {
    br_status: newStatus,
    br_updatedAt: now,
  };

  // Set respondedAt for accept/decline
  if (newStatus === "accepted" || newStatus === "declined") {
    updateData.br_respondedAt = now;
  }

  await ref.update(updateData);

  return { ok: true };
});
