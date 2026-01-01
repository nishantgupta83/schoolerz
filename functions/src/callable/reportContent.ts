import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked, isNewUser } from "../utils/authz";

const REASONS = new Set([
  "spam",
  "harassment",
  "inappropriate",
  "scam",
  "pii_leak",
  "grooming",
  "false_positive", // For appeals
  "other",
]);

// Threshold for flagging reporter as spam
const REPORTER_SPAM_THRESHOLD = 50;

/**
 * Build a context snapshot for the report so admins can review even if content changes.
 */
async function buildContextSnapshot(data: any): Promise<{
  type: string;
  contentSnapshot: any | null;
}> {
  // If reporting a post
  if (data.reportedPostId) {
    const postSnap = await admin.firestore()
      .collection("posts")
      .doc(String(data.reportedPostId))
      .get();

    if (postSnap.exists) {
      const post = postSnap.data()!;
      return {
        type: "post",
        contentSnapshot: {
          pst_type: post.pst_type,
          pst_authorId: post.pst_authorId,
          pst_authorName: post.pst_authorSnapshot?.publicName ?? "Unknown",
          pst_title: post.pst_title,
          pst_body: (post.pst_body ?? "").substring(0, 200), // Truncate for storage
          pst_serviceType: post.pst_serviceType,
          pst_createdAt: post.pst_createdAt,
        },
      };
    }
    return { type: "post", contentSnapshot: null };
  }

  // If reporting a message
  if (data.reportedMessageId && data.conversationId) {
    const msgSnap = await admin.firestore()
      .collection("conversations")
      .doc(String(data.conversationId))
      .collection("messages")
      .doc(String(data.reportedMessageId))
      .get();

    if (msgSnap.exists) {
      const msg = msgSnap.data()!;
      return {
        type: "message",
        contentSnapshot: {
          msg_senderId: msg.msg_senderId,
          msg_text: (msg.msg_text ?? "").substring(0, 200),
          msg_createdAt: msg.msg_createdAt,
        },
      };
    }
    return { type: "message", contentSnapshot: null };
  }

  // If reporting a user (no content to snapshot)
  if (data.reportedUserId) {
    const userSnap = await admin.firestore()
      .collection("users")
      .doc(String(data.reportedUserId))
      .get();

    if (userSnap.exists) {
      const user = userSnap.data()!;
      return {
        type: "user",
        contentSnapshot: {
          usr_publicName: user.usr_publicName,
          usr_role: user.usr_role,
          usr_createdAt: user.usr_createdAt,
        },
      };
    }
    return { type: "user", contentSnapshot: null };
  }

  return { type: "unknown", contentSnapshot: null };
}

/**
 * Check if reporter is potentially spamming reports
 */
async function isReporterSpamming(uid: string): Promise<boolean> {
  const oneDayAgo = admin.firestore.Timestamp.fromDate(
    new Date(Date.now() - 24 * 60 * 60 * 1000)
  );

  const recentReports = await admin.firestore()
    .collection("reports")
    .where("rpt_reporterUserId", "==", uid)
    .where("rpt_createdAt", ">", oneDayAgo)
    .count()
    .get();

  return recentReports.data().count >= REPORTER_SPAM_THRESHOLD;
}

export const reportContent = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  // Rate limit with stricter limits for new users
  await enforceRateLimit({
    key: `reportContent:${uid}`,
    windowSec: 86400,
    max: 10,
    isNewUser: isNewUser(user),
    newUserMax: 3, // New users can only report 3 items/day
  });

  const data = req.data ?? {};
  const reason = String(data.reason || "");
  if (!REASONS.has(reason)) {
    throw new HttpsError("invalid-argument", "Invalid reason");
  }

  // Must report at least one thing
  if (!data.reportedUserId && !data.reportedPostId && !data.reportedMessageId) {
    throw new HttpsError("invalid-argument", "Must specify content to report");
  }

  const descriptionRes = await filterTextOrReject(String(data.description || ""), {
    maxLen: 300,
    mode: "STRICT",
  });
  if (!descriptionRes.ok) {
    throw new HttpsError("invalid-argument", `Description blocked: ${descriptionRes.reason}`);
  }

  // Build context snapshot for admin review
  const contextSnapshot = await buildContextSnapshot(data);

  // Check if reporter is spamming reports
  const isSpammer = await isReporterSpamming(uid);

  const now = admin.firestore.FieldValue.serverTimestamp();
  const reportDoc = {
    rpt_reporterUserId: uid,
    rpt_reporterSnapshot: {
      publicName: user.usr_publicName,
      role: user.usr_role,
    },
    rpt_reportedUserId: data.reportedUserId ?? null,
    rpt_reportedPostId: data.reportedPostId ?? null,
    rpt_reportedMessageId: data.reportedMessageId ?? null,
    rpt_conversationId: data.conversationId ?? null, // For message context
    rpt_reason: reason,
    rpt_description: descriptionRes.sanitized,
    rpt_contextType: contextSnapshot.type,
    rpt_contextSnapshot: contextSnapshot.contentSnapshot,
    rpt_status: "pending",
    rpt_isFlaggedReporter: isSpammer, // Flag for admin attention
    rpt_createdAt: now,
    rpt_updatedAt: now,
  };

  const ref = admin.firestore().collection("reports").doc();
  await ref.set(reportDoc);

  return { ok: true, reportId: ref.id };
});
