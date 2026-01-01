import * as admin from "firebase-admin";

// Structured rejection codes for better error handling
export type RejectCode =
  | "PII_PHONE"
  | "PII_EMAIL"
  | "PII_ADDRESS"
  | "PII_SOCIAL"
  | "PII_MEETUP"
  | "LINK"
  | "PROFANITY"
  | "SEXUAL"
  | "GROOMING"
  | "SELF_HARM"
  | "TOO_LONG";

export type FilterResult =
  | { ok: true; sanitized: string }
  | { ok: false; code: RejectCode; reason: string };

// Filter mode: STRICT for posts/comments, CHAT for even stricter real-time messaging
export type FilterMode = "STRICT" | "CHAT";

// PII patterns with specific types
const PII_PATTERNS: Record<string, RegExp> = {
  phone: /(\+?1?[-.\s]?)?(\(?\d{3}\)?[-.\s]?)?\d{3}[-.\s]?\d{4}/g,
  email: /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g,
  address: /\b\d{1,6}\s+[\w\s.#-]+\s+(st|street|ave|avenue|blvd|boulevard|dr|drive|rd|road|ln|lane|way|ct|court)\b/gi,
  social: /\b(snap|snapchat|ig|instagram|tiktok|discord|whatsapp|telegram|signal|twitter|x\.com|fb|facebook)\b[\s:@\-_.]*[@]?\w[\w._-]{0,30}/gi,
  meetup: /\b(meet\s+me\s+at|come\s+to|my\s+address\s+is|i\s+live\s+at|let'?s\s+meet\s+at|i'?m\s+at)\b/gi,
};

// Grooming patterns (CHAT mode only)
const GROOMING_PATTERNS: RegExp[] = [
  /\b(don'?t\s+tell\s+(your\s+)?(parents?|mom|dad|anyone))\b/gi,
  /\b(keep\s+(this|it)\s+secret)\b/gi,
  /\b(just\s+between\s+us)\b/gi,
  /\b(are\s+you\s+alone)\b/gi,
  /\b(how\s+old\s+are\s+you)\b/gi,
  /\b(send\s+(me\s+)?(a\s+)?(pic|photo|picture))\b/gi,
];

const LINK_PATTERNS: RegExp[] = [
  /\bhttps?:\/\/\S+/gi,
  /\bwww\.\S+/gi,
  /\b(meet\.google\.com\/\S+)\b/gi,
  /\b(zoom\.us\/j\/\S+)\b/gi,
  /\b(bit\.ly\/\S+|t\.co\/\S+|tinyurl\.com\/\S+|goo\.gl\/\S+)\b/gi,
  /\b[\w-]+\.(com|net|org|io|co|app|dev|me|info|biz)\b/gi, // Domain-like patterns
];

// Extended leetspeak mapping
const LEETSPEAK_MAP: Record<string, string> = {
  "$": "s",
  "@": "a",
  "0": "o",
  "1": "i",
  "3": "e",
  "4": "a",
  "5": "s",
  "7": "t",
  "8": "b",
  "9": "g",
  "!": "i",
  "|": "i",
  "(": "c",
  "<": "c",
  "{": "c",
  "+": "t",
};

// Common Cyrillic homoglyphs to Latin equivalents
const HOMOGLYPH_MAP: Record<string, string> = {
  "а": "a", // Cyrillic a
  "е": "e", // Cyrillic e
  "о": "o", // Cyrillic o
  "р": "p", // Cyrillic r (looks like p)
  "с": "c", // Cyrillic s (looks like c)
  "у": "y", // Cyrillic u (looks like y)
  "х": "x", // Cyrillic kh (looks like x)
  "і": "i", // Ukrainian i
  "ї": "i", // Ukrainian yi
  "ё": "e", // Cyrillic yo
  "ᴀ": "a", // Small caps
  "ᴇ": "e",
  "ᴏ": "o",
  "ᴜ": "u",
  "ɑ": "a", // IPA
  "ε": "e", // Greek epsilon
  "ο": "o", // Greek omicron
  "𝐚": "a", // Mathematical variants
  "𝐞": "e",
  "𝐢": "i",
  "𝐨": "o",
  "𝐮": "u",
};

/**
 * Normalize text for content detection:
 * 1. Convert to lowercase
 * 2. Replace homoglyphs with Latin equivalents
 * 3. Replace leetspeak with letters
 * 4. Collapse repeated characters (heyyyy -> hey)
 * 5. Strip non-alphanumeric except spaces
 */
function normalizeForDetection(text: string): string {
  let result = text.toLowerCase();

  // Replace homoglyphs
  for (const [char, replacement] of Object.entries(HOMOGLYPH_MAP)) {
    result = result.split(char).join(replacement);
  }

  // Replace leetspeak
  for (const [char, replacement] of Object.entries(LEETSPEAK_MAP)) {
    const escaped = char.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    result = result.replace(new RegExp(escaped, "g"), replacement);
  }

  // Collapse repeated characters (more than 2 in a row)
  result = result.replace(/(.)\1{2,}/g, "$1$1");

  // Strip non-alphanumeric except spaces
  result = result.replace(/[^\w\s]/g, " ");

  // Collapse multiple spaces
  result = result.replace(/\s+/g, " ").trim();

  return result;
}

/**
 * Check for PII patterns and return specific type if found
 * @param text - Text to check
 * @param normalizedText - Optional normalized text for additional checks (social/meetup)
 */
function detectPII(
  text: string,
  normalizedText?: string
): { type: string; code: RejectCode } | null {
  const codeMap: Record<string, RejectCode> = {
    phone: "PII_PHONE",
    email: "PII_EMAIL",
    address: "PII_ADDRESS",
    social: "PII_SOCIAL",
    meetup: "PII_MEETUP",
  };

  // Patterns that should ALSO run on normalized text to catch obfuscation
  const normalizedCheckPatterns = new Set(["social", "meetup"]);

  for (const [type, pattern] of Object.entries(PII_PATTERNS)) {
    pattern.lastIndex = 0;
    // Check raw text
    if (pattern.test(text)) {
      return { type, code: codeMap[type] };
    }

    // For social/meetup, also check normalized text to catch obfuscation
    if (normalizedText && normalizedCheckPatterns.has(type)) {
      pattern.lastIndex = 0;
      if (pattern.test(normalizedText)) {
        return { type, code: codeMap[type] };
      }
    }
  }
  return null;
}

/**
 * Check for links
 */
function containsLink(text: string): boolean {
  return LINK_PATTERNS.some((re) => {
    re.lastIndex = 0;
    return re.test(text);
  });
}

/**
 * Check for grooming patterns (CHAT mode only)
 */
function containsGrooming(text: string): boolean {
  return GROOMING_PATTERNS.some((re) => {
    re.lastIndex = 0;
    return re.test(text);
  });
}

/**
 * Load blocked word lists from Firestore config
 */
export async function loadBlockedLists(): Promise<{
  profanity: string[];
  sexual: string[];
  selfHarm: string[];
  grooming: string[];
}> {
  const snap = await admin.firestore().collection("config").doc("blockedContent").get();
  const data = snap.exists ? snap.data() : {};
  return {
    profanity: Array.isArray(data?.profanity) ? data!.profanity : [],
    sexual: Array.isArray(data?.sexual) ? data!.sexual : [],
    selfHarm: Array.isArray(data?.selfHarm) ? data!.selfHarm : [],
    grooming: Array.isArray(data?.grooming) ? data!.grooming : [],
  };
}

/**
 * Check if normalized text contains a word from the list using word boundaries
 * Uses regex word boundary matching to prevent "class" matching "ass"
 */
function listHitWithBoundary(normalized: string, list: string[]): string | null {
  for (const word of list) {
    const escapedWord = word.toLowerCase().replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    const pattern = new RegExp(`\\b${escapedWord}\\b`, "i");
    if (pattern.test(normalized)) {
      return word;
    }
  }
  return null;
}

/**
 * Main filter function with mode support
 *
 * @param input - Text to filter
 * @param opts - Options including maxLen and mode
 * @returns FilterResult with ok status and either sanitized text or rejection details
 */
export async function filterTextOrReject(
  input: string,
  opts?: { maxLen?: number; mode?: FilterMode }
): Promise<FilterResult> {
  const maxLen = opts?.maxLen ?? 800;
  const mode = opts?.mode ?? "STRICT";
  const raw = (input ?? "").trim();

  // Empty is OK
  if (!raw) return { ok: true, sanitized: "" };

  // Length check
  if (raw.length > maxLen) {
    return { ok: false, code: "TOO_LONG", reason: "Text too long" };
  }

  // Prepare sanitized and normalized text early for PII detection
  const sanitized = raw.replace(/\s+/g, " ").trim();
  const normalized = normalizeForDetection(sanitized);

  // PII detection - check raw text AND normalized text for social/meetup patterns
  const pii = detectPII(raw, normalized);
  if (pii) {
    return { ok: false, code: pii.code, reason: `Contains ${pii.type}` };
  }

  // Link detection
  if (containsLink(raw)) {
    return { ok: false, code: "LINK", reason: "Links are not allowed" };
  }

  // Grooming patterns (CHAT mode is stricter)
  if (mode === "CHAT" && containsGrooming(raw)) {
    return { ok: false, code: "GROOMING", reason: "Inappropriate content detected" };
  }

  // Load blocked lists
  const lists = await loadBlockedLists();

  // Check profanity with word boundaries
  const profanityHit = listHitWithBoundary(normalized, lists.profanity);
  if (profanityHit) {
    return { ok: false, code: "PROFANITY", reason: "Profanity not allowed" };
  }

  // Check sexual content with word boundaries
  const sexualHit = listHitWithBoundary(normalized, lists.sexual);
  if (sexualHit) {
    return { ok: false, code: "SEXUAL", reason: "Inappropriate content" };
  }

  // Check self-harm content with word boundaries
  const selfHarmHit = listHitWithBoundary(normalized, lists.selfHarm);
  if (selfHarmHit) {
    return { ok: false, code: "SELF_HARM", reason: "Self-harm content blocked" };
  }

  // Check grooming content from config (both modes)
  const groomingHit = listHitWithBoundary(normalized, lists.grooming);
  if (groomingHit) {
    return { ok: false, code: "GROOMING", reason: "Inappropriate content detected" };
  }

  // In CHAT mode, apply additional strictness
  if (mode === "CHAT") {
    // Block any remaining numbers that could be phone fragments
    const digitCount = (sanitized.match(/\d/g) || []).length;
    if (digitCount >= 7) {
      return { ok: false, code: "PII_PHONE", reason: "Too many numbers detected" };
    }
  }

  return { ok: true, sanitized };
}

/**
 * Quick check without async - for client-side pre-validation hints
 * Returns potential issues without hitting Firestore
 */
export function quickFilterCheck(input: string): { hasPII: boolean; hasLink: boolean } {
  const raw = (input ?? "").trim();
  return {
    hasPII: detectPII(raw) !== null,
    hasLink: containsLink(raw),
  };
}
