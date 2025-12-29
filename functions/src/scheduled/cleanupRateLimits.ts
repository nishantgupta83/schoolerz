import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";

/**
 * Scheduled function to clean up stale rate limit documents.
 * Runs every 24 hours to remove documents older than 24 hours.
 * This prevents the rateLimits collection from growing indefinitely.
 */
export const cleanupRateLimits = onSchedule("every 24 hours", async () => {
  const cutoff = admin.firestore.Timestamp.fromDate(
    new Date(Date.now() - 24 * 60 * 60 * 1000)
  );

  // Query stale rate limit documents (updatedAt older than 24 hours)
  const stale = await admin.firestore()
    .collection("rateLimits")
    .where("updatedAt", "<", cutoff)
    .limit(500) // Batch size to avoid timeout
    .get();

  if (stale.empty) {
    console.log("No stale rate limit documents to clean up");
    return;
  }

  // Use batch delete for efficiency
  const batch = admin.firestore().batch();
  stale.docs.forEach(doc => batch.delete(doc.ref));
  await batch.commit();

  console.log(`Cleaned up ${stale.size} stale rate limit documents`);
});
