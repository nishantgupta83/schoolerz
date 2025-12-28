import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { enforceRateLimit } from "../utils/rateLimit";
import { filterTextOrReject } from "../utils/contentFilter";
import { getUserOrThrow, requireAuth, requireNotBlocked } from "../utils/authz";

const REASONS = new Set([
  "spam",
  "harassment",
  "inappropriate",
  "scam",
  "pii_leak",
  "other",
]);

export const reportContent = onCall(async (req) => {
  const uid = await requireAuth(req.auth);
  const user = await getUserOrThrow(uid);
  requireNotBlocked(user);

  await enforceRateLimit({ key: `reportContent:${uid}`, windowSec: 86400, max: 10 });

  const data = req.data ?? {};
  const reason = String(data.reason || "");
  if (!REASONS.has(reason)) {
    throw new HttpsError("invalid-argument", "Invalid reason");
  }

  const descriptionRes = await filterTextOrReject(String(data.description || ""), {
    maxLen: 300,
  });
  if (!descriptionRes.ok) {
    throw new HttpsError("invalid-argument", `Description blocked: ${descriptionRes.reason}`);
  }

  const reportDoc = {
    rpt_reporterUserId: uid,
    rpt_reportedUserId: data.reportedUserId ?? null,
    rpt_reportedPostId: data.reportedPostId ?? null,
    rpt_reportedMessageId: data.reportedMessageId ?? null,
    rpt_reason: reason,
    rpt_description: descriptionRes.sanitized,
    rpt_status: "pending",
    rpt_createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  const ref = admin.firestore().collection("reports").doc();
  await ref.set(reportDoc);

  return { ok: true, reportId: ref.id };
});
