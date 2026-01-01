import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

const ALLOWED_STATUSES = new Set(["accepted", "declined", "completed", "cancelled"]);

/**
 * Respond to a booking request.
 * - Provider (Teen1) can: accept, decline (when pending)
 * - Requester (Parent) can: cancel (when pending/accepted), complete (when accepted)
 * Uses transaction to prevent race conditions.
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

  // Use transaction to prevent race conditions
  // Store booking data for notification after transaction
  let notificationData: {
    requesterId: string;
    providerName: string;
    requestId: string;
  } | null = null;

  await admin.firestore().runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (!snap.exists) {
      throw new HttpsError("not-found", "Booking request not found");
    }

    const br = snap.data()!;
    const isProvider = br.br_providerId === uid;
    const isRequester = br.br_requesterId === uid;

    if (!isProvider && !isRequester) {
      throw new HttpsError("permission-denied", "Not a participant");
    }

    // Enforce role constraints: provider must be teen, requester must be parent
    const [providerUser, requesterUser] = await Promise.all([
      getUserOrThrow(br.br_providerId),
      getUserOrThrow(br.br_requesterId),
    ]);
    if (providerUser.usr_role !== "teen") {
      throw new HttpsError("failed-precondition", "Provider must be teen");
    }
    if (requesterUser.usr_role !== "parent") {
      throw new HttpsError("failed-precondition", "Requester must be parent");
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

    tx.update(ref, updateData);

    // Store data for notification if accepting
    if (newStatus === "accepted") {
      notificationData = {
        requesterId: br.br_requesterId,
        providerName: providerUser.usr_publicName || "Provider",
        requestId,
      };
    }
  });

  // Send push notification after successful transaction (outside transaction)
  if (notificationData !== null) {
    const nd = notificationData as { requesterId: string; providerName: string; requestId: string };
    try {
      const fcmDoc = await admin.firestore()
        .collection("users")
        .doc(nd.requesterId)
        .collection("private")
        .doc("fcm")
        .get();

      const tokens = fcmDoc.exists ? (fcmDoc.data()?.tokens as string[] || []) : [];

      if (tokens.length > 0) {
        const message = {
          tokens,
          notification: {
            title: "Request Accepted!",
            body: `${nd.providerName} accepted your booking request`,
          },
          data: {
            requestId: nd.requestId,
            type: "booking_accepted",
          },
          android: {
            priority: "high" as const,
          },
          apns: {
            payload: {
              aps: {
                sound: "default",
              },
            },
          },
        };

        const response = await admin.messaging().sendEachForMulticast(message);

        // Clean up invalid tokens
        if (response.failureCount > 0) {
          const invalidTokens: string[] = [];
          response.responses.forEach((resp, idx) => {
            if (!resp.success && resp.error?.code === "messaging/registration-token-not-registered") {
              invalidTokens.push(tokens[idx]);
            }
          });

          if (invalidTokens.length > 0) {
            await admin.firestore()
              .collection("users")
              .doc(nd.requesterId)
              .collection("private")
              .doc("fcm")
              .update({
                tokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
              });
          }
        }
      }
    } catch (fcmError) {
      // Log but don't fail the request - notification is best-effort
      console.error("FCM notification failed:", fcmError);
    }
  }

  return { ok: true };
});
