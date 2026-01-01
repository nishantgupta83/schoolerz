import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";

/**
 * Scheduled function to decay strikes for users after 14 days of no violations.
 * Runs every 24 hours.
 *
 * Strike Decay Policy:
 * - Users with strikes > 0 AND lastStrikeAt older than 14 days get 1 strike removed
 * - If strike count drops to 0, user is unblocked (if blocked for strikes)
 * - Uses batch pagination to handle >100 users
 */
export const decayStrikes = onSchedule("every 24 hours", async () => {
  const DECAY_DAYS = 14;
  const BATCH_SIZE = 100;

  const cutoff = admin.firestore.Timestamp.fromDate(
    new Date(Date.now() - DECAY_DAYS * 24 * 60 * 60 * 1000)
  );

  let lastDoc: admin.firestore.DocumentSnapshot | null = null;
  let totalDecayed = 0;
  let hasMore = true;

  // Paginate through all eligible users
  while (hasMore) {
    let query = admin.firestore()
      .collection("users")
      .where("usr_strikeCount", ">", 0)
      .where("usr_lastStrikeAt", "<", cutoff)
      .orderBy("usr_lastStrikeAt")
      .limit(BATCH_SIZE);

    if (lastDoc) {
      query = query.startAfter(lastDoc);
    }

    const snap = await query.get();

    if (snap.empty) {
      hasMore = false;
      break;
    }

    const batch = admin.firestore().batch();

    snap.docs.forEach((doc) => {
      const data = doc.data();
      const currentStrikes = data.usr_strikeCount ?? 0;
      const newStrikeCount = Math.max(0, currentStrikes - 1);

      const updateData: Record<string, any> = {
        usr_strikeCount: newStrikeCount,
        usr_updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      };

      // Only reset lastStrikeAt if strikes drop to 0
      if (newStrikeCount === 0) {
        updateData.usr_lastStrikeAt = null;
      }

      // If strikes drop to 0 and user was blocked, unblock them
      // (Only if they were blocked due to strikes, not manual admin action)
      if (newStrikeCount === 0 && data.usr_isBlocked) {
        // Note: Only unblock if blocked due to strike escalation (5 strikes)
        // Check if this was a strike-based block (strikeCount was >= 5)
        if (currentStrikes >= 5) {
          updateData.usr_isBlocked = false;
        }
        // Also lift chat restriction when strikes decay
        if (data.usr_isChatRestricted && currentStrikes >= 3) {
          updateData.usr_isChatRestricted = false;
        }
      }

      batch.update(doc.ref, updateData);
    });

    await batch.commit();
    totalDecayed += snap.size;
    lastDoc = snap.docs[snap.docs.length - 1];

    // Check if there are more documents to process
    if (snap.size < BATCH_SIZE) {
      hasMore = false;
    }
  }

  console.log(`Decayed strikes for ${totalDecayed} users`);
});
