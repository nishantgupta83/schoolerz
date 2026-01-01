import * as admin from "firebase-admin";
import { HttpsError } from "firebase-functions/v2/https";

export interface RateLimitParams {
  key: string;
  windowSec: number;
  max: number;
  /** If true and user is new (<24h), apply stricter limit */
  isNewUser?: boolean;
  /** Stricter limit for new users (defaults to max/2) */
  newUserMax?: number;
}

/**
 * Enforce rate limit with transaction-safe counter
 *
 * @param params - Rate limit configuration
 * @throws HttpsError with code "resource-exhausted" if limit exceeded
 */
export async function enforceRateLimit(params: RateLimitParams): Promise<void> {
  const { key, windowSec, max, isNewUser, newUserMax } = params;
  const ref = admin.firestore().collection("rateLimits").doc(key);
  const now = admin.firestore.Timestamp.now();

  // Apply stricter limit for new users
  const effectiveMax = isNewUser ? (newUserMax ?? Math.ceil(max / 2)) : max;

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
    if (count >= effectiveMax) {
      throw new HttpsError("resource-exhausted", "Rate limit exceeded");
    }

    // Increment count
    tx.set(ref, { count: count + 1, updatedAt: now }, { merge: true });
  });
}

/**
 * Check rate limit without incrementing
 * Useful for showing remaining count to users
 */
export async function checkRateLimit(params: {
  key: string;
  windowSec: number;
  max: number;
}): Promise<{ remaining: number; resetsAt: Date }> {
  const { key, windowSec, max } = params;
  const ref = admin.firestore().collection("rateLimits").doc(key);
  const now = admin.firestore.Timestamp.now();

  const snap = await ref.get();
  if (!snap.exists) {
    return { remaining: max, resetsAt: new Date(now.toMillis() + windowSec * 1000) };
  }

  const data = snap.data()!;
  const windowStart = data.windowStart as admin.firestore.Timestamp | undefined;
  const count = (data.count as number | undefined) ?? 0;

  if (!windowStart || now.seconds - windowStart.seconds >= windowSec) {
    return { remaining: max, resetsAt: new Date(now.toMillis() + windowSec * 1000) };
  }

  const remaining = Math.max(0, max - count);
  const resetsAt = new Date(windowStart.toMillis() + windowSec * 1000);

  return { remaining, resetsAt };
}

/**
 * Burst rate limiter for high-frequency actions
 * Allows burst but limits sustained rate
 *
 * @param params - Burst limit configuration
 */
export async function enforceBurstRateLimit(params: {
  key: string;
  burstMax: number; // Max in burst window (e.g., 5 in 1 minute)
  burstWindowSec: number; // Burst window (e.g., 60 seconds)
  sustainedMax: number; // Max in sustained window (e.g., 20 in 1 hour)
  sustainedWindowSec: number; // Sustained window (e.g., 3600 seconds)
}): Promise<void> {
  const { key, burstMax, burstWindowSec, sustainedMax, sustainedWindowSec } = params;

  // Check both limits
  await Promise.all([
    enforceRateLimit({ key: `${key}:burst`, windowSec: burstWindowSec, max: burstMax }),
    enforceRateLimit({ key: `${key}:sustained`, windowSec: sustainedWindowSec, max: sustainedMax }),
  ]);
}
