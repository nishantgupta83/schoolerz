import * as admin from "firebase-admin";

export type FilterResult =
  | { ok: true; sanitized: string }
  | { ok: false; code: "PII" | "LINK" | "BLOCKED"; reason: string };

const PII_PATTERNS: Record<string, RegExp> = {
  phone: /(\+?1?[-.\s]?)?(\(?\d{3}\)?[-.\s]?)?\d{3}[-.\s]?\d{4}/g,
  email: /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g,
  address: /\b\d{1,6}\s+[\w\s.#-]+\s+(st|street|ave|avenue|blvd|boulevard|dr|drive|rd|road|ln|lane|way|ct|court)\b/gi,
  social: /\b(snap|snapchat|ig|instagram|tiktok|discord|whatsapp|telegram|signal)\b[\s:@-]*[@]?\w[\w._-]{1,30}/gi,
  meetup: /\b(meet\s+me\s+at|come\s+to|my\s+address\s+is|i\s+live\s+at)\b/gi,
};

const LINK_PATTERNS: RegExp[] = [
  /\bhttps?:\/\/\S+/gi,
  /\bwww\.\S+/gi,
  /\b(meet\.google\.com\/\S+)\b/gi,
  /\b(zoom\.us\/j\/\S+)\b/gi,
  /\b(bit\.ly\/\S+|t\.co\/\S+|tinyurl\.com\/\S+)\b/gi,
];

const LEETSPEAK_MAP: Record<string, string> = {
  "$": "s",
  "@": "a",
  "0": "o",
  "1": "i",
  "3": "e",
  "4": "a",
  "5": "s",
};

function normalize(text: string): string {
  let result = text.toLowerCase();
  for (const [char, replacement] of Object.entries(LEETSPEAK_MAP)) {
    result = result.replace(new RegExp("\\" + char, "g"), replacement);
  }
  return result.replace(/[^\w\s]/g, " ");
}

function containsPII(text: string): string | null {
  for (const [type, pattern] of Object.entries(PII_PATTERNS)) {
    pattern.lastIndex = 0;
    if (pattern.test(text)) return type;
  }
  return null;
}

function containsLink(text: string): boolean {
  return LINK_PATTERNS.some((re) => {
    re.lastIndex = 0;
    return re.test(text);
  });
}

export async function loadBlockedLists(): Promise<{
  profanity: string[];
  sexual: string[];
  selfHarm: string[];
}> {
  const snap = await admin.firestore().collection("config").doc("blockedContent").get();
  const data = snap.exists ? snap.data() : {};
  return {
    profanity: Array.isArray(data?.profanity) ? data!.profanity : [],
    sexual: Array.isArray(data?.sexual) ? data!.sexual : [],
    selfHarm: Array.isArray(data?.selfHarm) ? data!.selfHarm : [],
  };
}

function listHit(normalized: string, list: string[]): boolean {
  return list.some(
    (w) =>
      normalized.includes(` ${w.toLowerCase()} `) ||
      normalized.startsWith(`${w.toLowerCase()} `) ||
      normalized.endsWith(` ${w.toLowerCase()}`)
  );
}

export async function filterTextOrReject(
  input: string,
  opts?: { maxLen?: number }
): Promise<FilterResult> {
  const maxLen = opts?.maxLen ?? 800;
  const raw = (input ?? "").trim();

  if (!raw) return { ok: true, sanitized: "" };
  if (raw.length > maxLen) {
    return { ok: false, code: "BLOCKED", reason: "Text too long" };
  }

  const piiType = containsPII(raw);
  if (piiType) {
    return { ok: false, code: "PII", reason: `Contains ${piiType}` };
  }

  if (containsLink(raw)) {
    return { ok: false, code: "LINK", reason: "Links are not allowed" };
  }

  const sanitized = raw.replace(/\s+/g, " ").trim();
  const lists = await loadBlockedLists();
  const norm = ` ${normalize(sanitized)} `;

  if (listHit(norm, lists.profanity)) {
    return { ok: false, code: "BLOCKED", reason: "Profanity not allowed" };
  }
  if (listHit(norm, lists.sexual)) {
    return { ok: false, code: "BLOCKED", reason: "Inappropriate content" };
  }
  if (listHit(norm, lists.selfHarm)) {
    return { ok: false, code: "BLOCKED", reason: "Self-harm content blocked" };
  }

  return { ok: true, sanitized };
}
