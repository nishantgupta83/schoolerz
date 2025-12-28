import * as admin from "firebase-admin";
import { HttpsError } from "firebase-functions/v2/https";

export interface RateLimitParams {
  key: string;
  windowSec: number;
  max: number;
}

export async function enforceRateLimit(params: RateLimitParams): Promise<void> {
  const { key, windowSec, max } = params;
  const ref = admin.firestore().collection("rateLimits").doc(key);
  const now = admin.firestore.Timestamp.now();

  await admin.firestore().runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const data = snap.exists ? snap.data()! : null;

    const windowStart = data?.windowStart as admin.firestore.Timestamp | undefined;
    const count = (data?.count as number | undefined) ?? 0;

    // Reset window if expired
    if (!windowStart || now.seconds - windowStart.seconds >= windowSec) {
      tx.set(ref, { windowStart: now, count: 1, updatedAt: now }, { merge: true });
      return;
    }

    // Check limit
    if (count >= max) {
      throw new HttpsError("resource-exhausted", "Rate limit exceeded");
    }

    // Increment count
    tx.set(ref, { count: count + 1, updatedAt: now }, { merge: true });
  });
}
