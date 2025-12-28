import * as admin from "firebase-admin";
import { HttpsError } from "firebase-functions/v2/https";

export type UserDoc = {
  usr_role: "parent" | "teen";
  usr_publicName: string;
  usr_avatarUrl?: string | null;
  usr_verificationStatus?: string;
  usr_isBlocked?: boolean;
  usr_isChatRestricted?: boolean;
  usr_strikeCount?: number;
  usr_zipcode?: string;
  usr_neighborhoodId?: string;
  usr_parentUserId?: string | null;
  usr_childIds?: string[];
};

export async function requireAuth(contextAuth: any): Promise<string> {
  if (!contextAuth?.uid) {
    throw new HttpsError("unauthenticated", "Sign in required");
  }
  return contextAuth.uid as string;
}

export async function getUserOrThrow(userId: string): Promise<UserDoc> {
  const snap = await admin.firestore().collection("users").doc(userId).get();
  if (!snap.exists) {
    throw new HttpsError("failed-precondition", "User profile missing");
  }
  return snap.data() as UserDoc;
}

export function requireNotBlocked(user: UserDoc): void {
  if (user.usr_isBlocked) {
    throw new HttpsError("permission-denied", "Account blocked");
  }
}

export function requireRole(user: UserDoc, role: "parent" | "teen"): void {
  if (user.usr_role !== role) {
    throw new HttpsError("permission-denied", `Only ${role} allowed`);
  }
}

export function requireNotChatRestricted(user: UserDoc): void {
  if (user.usr_isChatRestricted) {
    throw new HttpsError("permission-denied", "Chat restricted");
  }
}
