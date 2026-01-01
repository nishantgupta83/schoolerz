import * as admin from "firebase-admin";
import { HttpsError } from "firebase-functions/v2/https";

/**
 * Full user document type (internal use only)
 * Contains all fields including private ones
 */
export type UserDocFull = {
  usr_role: "parent" | "teen";
  usr_publicName: string;
  usr_displayNamePrivate?: string;
  usr_avatarUrl?: string | null;
  usr_verificationStatus?: string;
  usr_emailVerified?: boolean;
  usr_phoneVerified?: boolean;
  usr_isVerified?: boolean;
  usr_isBlocked?: boolean;
  usr_isChatRestricted?: boolean;
  usr_strikeCount?: number;
  usr_lastStrikeAt?: admin.firestore.Timestamp | null;
  usr_zipcode?: string;
  usr_neighborhoodId?: string;
  usr_parentUserId?: string | null;
  usr_childIds?: string[];
  usr_createdAt?: admin.firestore.Timestamp;
  usr_updatedAt?: admin.firestore.Timestamp;
};

/**
 * Public-safe user fields (for snapshots and external use)
 * Never includes: displayNamePrivate, email, phone, school info
 */
export type UserDocPublic = {
  usr_role: "parent" | "teen";
  usr_publicName: string;
  usr_avatarUrl?: string | null;
  usr_verificationStatus?: string;
  usr_isVerified?: boolean;
  usr_zipcode?: string;
  usr_neighborhoodId?: string;
};

/**
 * Extract only public-safe fields from a full user document
 */
export function toPublicUser(user: UserDocFull): UserDocPublic {
  return {
    usr_role: user.usr_role,
    usr_publicName: user.usr_publicName,
    usr_avatarUrl: user.usr_avatarUrl ?? null,
    usr_verificationStatus: user.usr_verificationStatus,
    usr_isVerified: user.usr_isVerified,
    usr_zipcode: user.usr_zipcode,
    usr_neighborhoodId: user.usr_neighborhoodId,
  };
}

/**
 * Require authenticated user
 */
export async function requireAuth(contextAuth: any): Promise<string> {
  if (!contextAuth?.uid) {
    throw new HttpsError("unauthenticated", "Sign in required");
  }
  return contextAuth.uid as string;
}

/**
 * Get full user document (internal use)
 * Includes all fields for authorization checks
 */
export async function getUserOrThrow(userId: string): Promise<UserDocFull> {
  const snap = await admin.firestore().collection("users").doc(userId).get();
  if (!snap.exists) {
    throw new HttpsError("failed-precondition", "User profile missing");
  }
  return snap.data() as UserDocFull;
}

/**
 * Get only public-safe user fields
 * Use this when creating author snapshots for posts, comments, etc.
 */
export async function getPublicUserOrThrow(userId: string): Promise<UserDocPublic> {
  const user = await getUserOrThrow(userId);
  return toPublicUser(user);
}

/**
 * Require user is not blocked
 */
export function requireNotBlocked(user: UserDocFull): void {
  if (user.usr_isBlocked) {
    throw new HttpsError("permission-denied", "Account blocked");
  }
}

/**
 * Require specific role
 */
export function requireRole(user: UserDocFull, role: "parent" | "teen"): void {
  if (user.usr_role !== role) {
    throw new HttpsError("permission-denied", `Only ${role} allowed`);
  }
}

/**
 * Require user is not chat restricted
 */
export function requireNotChatRestricted(user: UserDocFull): void {
  if (user.usr_isChatRestricted) {
    throw new HttpsError("permission-denied", "Chat restricted");
  }
}

/**
 * Require user is verified (email or phone verified)
 * Use for sensitive actions like posting, booking, contact sharing
 */
export function requireVerified(user: UserDocFull): void {
  if (!user.usr_isVerified && !user.usr_emailVerified && !user.usr_phoneVerified) {
    throw new HttpsError("failed-precondition", "Account verification required");
  }
}

/**
 * Assert teen has a linked parent
 * Required for Teen2 (browser) to create shortlists
 */
export function assertLinkedParent(user: UserDocFull): string {
  if (user.usr_role !== "teen") {
    throw new HttpsError("permission-denied", "Only teens can use this feature");
  }
  if (!user.usr_parentUserId) {
    throw new HttpsError("failed-precondition", "Parent account not linked");
  }
  return user.usr_parentUserId;
}

/**
 * Check if user is a new account (created within last 24 hours)
 * Used for applying stricter rate limits to new users
 */
export function isNewUser(user: UserDocFull): boolean {
  if (!user.usr_createdAt) return true; // Assume new if no timestamp
  const createdAt = user.usr_createdAt.toDate();
  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
  return createdAt > oneDayAgo;
}

/**
 * Create an author snapshot for embedding in posts/comments/requests
 * Only includes public-safe fields
 */
export function createAuthorSnapshot(user: UserDocFull): {
  publicName: string;
  avatarUrl: string | null;
  verificationStatus: string;
  role: "parent" | "teen";
} {
  return {
    publicName: user.usr_publicName,
    avatarUrl: user.usr_avatarUrl ?? null,
    verificationStatus: user.usr_verificationStatus ?? "unverified",
    role: user.usr_role,
  };
}

/**
 * Check if two users have blocked each other
 */
export async function isBlockedBetween(userId1: string, userId2: string): Promise<boolean> {
  const [s1, s2] = await Promise.all([
    admin.firestore().collection("blocks").doc(`${userId1}_${userId2}`).get(),
    admin.firestore().collection("blocks").doc(`${userId2}_${userId1}`).get(),
  ]);
  return s1.exists || s2.exists;
}
