import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";

/**
 * Scheduled function to clean up stale rate limit documents.
 * Runs every 24 hours to remove documents older than 24 hours.
 * This prevents the rateLimits collection from growing indefinitely.
 */
export const cleanupRateLimits = onSchedule("every 24 hours", async () => {
  const BATCH_SIZE = 500;
  const cutoff = admin.firestore.Timestamp.fromDate(
    new Date(Date.now() - 24 * 60 * 60 * 1000)
  );

  let hasMore = true;
  let totalCleaned = 0;

  // Paginate through all stale documents
  while (hasMore) {
    const stale = await admin.firestore()
      .collection("rateLimits")
      .where("updatedAt", "<", cutoff)
      .limit(BATCH_SIZE)
      .get();

    if (stale.empty) {
      hasMore = false;
      break;
    }

    const batch = admin.firestore().batch();
    stale.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();

    totalCleaned += stale.size;

    if (stale.size < BATCH_SIZE) {
      hasMore = false;
    }
  }

  console.log(`Cleaned up ${totalCleaned} stale rate limit documents`);
});
