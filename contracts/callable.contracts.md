# Cloud Functions Contract

All callable functions use Firebase Cloud Functions v2 with `onCall` handlers.

## Authentication

All functions require Firebase Authentication. Unauthenticated calls receive:
```json
{ "error": { "code": "unauthenticated", "message": "Sign in required" } }
```

## Response Format

**Success:**
```json
{ "ok": true, "...additionalFields" }
```

**Error:**
```json
{ "error": { "code": "error-code", "message": "Human readable message" } }
```

---

## Core Functions (Phase 1)

### `createPost`

Creates a new post (offer or request).

**Who Can Call:** Any verified user

**Rate Limit:** 5 per hour

**Input:**
```typescript
{
  type: "offer" | "request";           // Required
  title: string;                        // Required, max 80 chars
  body: string;                         // Required, max 800 chars
  serviceType: ServiceType;             // Required
  priceType: "hourly" | "fixed" | "free"; // Required
  priceMin?: number;                    // Required if priceType != "free" (cents)
  priceMax?: number;                    // Required if priceType != "free" (cents)
  zipcode: string;                      // Required
  neighborhoodId: string;               // Required
}
```

**Service Types:** `lawn_care`, `babysitting`, `dog_walking`, `tutoring`, `tech_help`, `errands`, `other`

**Output:**
```typescript
{ ok: true; postId: string }
```

**Errors:**
- `invalid-argument`: Missing/invalid fields, PII detected, links detected
- `resource-exhausted`: Rate limit exceeded

---

### `createComment`

Adds a comment to a post.

**Who Can Call:** Any authenticated user

**Rate Limit:** 20 per hour

**Input:**
```typescript
{
  postId: string;                       // Required
  cm_text: string;                      // Required, max 500 chars
}
```

**Output:**
```typescript
{ ok: true; commentId: string }
```

**Errors:**
- `invalid-argument`: Missing postId, PII detected, text too long
- `not-found`: Post not found
- `permission-denied`: User blocked by post author
- `resource-exhausted`: Rate limit exceeded

---

### `reportContent`

Reports content for moderation review.

**Who Can Call:** Any authenticated user

**Rate Limit:** 10 per day

**Input:**
```typescript
{
  reason: ReportReason;                 // Required
  description?: string;                 // Optional, max 300 chars
  reportedUserId?: string;              // One of these required
  reportedPostId?: string;
  reportedMessageId?: string;
}
```

**Report Reasons:** `spam`, `harassment`, `inappropriate`, `scam`, `pii_leak`, `other`

**Output:**
```typescript
{ ok: true; reportId: string }
```

---

### `blockUser`

Blocks a user from interacting with you.

**Who Can Call:** Any authenticated user

**Input:**
```typescript
{
  blockedId: string;                    // Required
}
```

**Output:**
```typescript
{ ok: true }
```

---

### `unblockUser`

Removes a user block.

**Who Can Call:** Any authenticated user

**Input:**
```typescript
{
  blockedId: string;                    // Required
}
```

**Output:**
```typescript
{ ok: true }
```

---

## Booking Functions (Phase 2)

### `createShortlist`

Teen2 (browser) creates a shortlist entry for parent review.

**Who Can Call:** Teen users linked to a parent

**Input:**
```typescript
{
  sl_postId: string;                    // Required
  sl_providerTeenId: string;            // Required
  sl_note?: string;                     // Optional, max 200 chars
}
```

**Output:**
```typescript
{ ok: true; shortlistId: string }
```

**Errors:**
- `permission-denied`: User is not a linked teen
- `not-found`: Post not found

---

### `createBookingRequest`

Parent creates a booking request to a teen provider.

**Who Can Call:** Parent users only

**Rate Limit:** 10 per day

**Input:**
```typescript
{
  br_postId: string;                    // Required
  br_deliveryMode: "in_person" | "online"; // Required
  br_preferredDays: string[];           // Required, e.g., ["Mon", "Wed"]
  br_timeWindow: string;                // Required, e.g., "3pm-6pm"
  br_meetingPreference: MeetingPref;    // Required
  br_notes?: string;                    // Optional, max 300 chars
}
```

**Meeting Preferences:** `public_place`, `online`, `parent_will_share_privately`

**Output:**
```typescript
{ ok: true; requestId: string }
```

**Errors:**
- `permission-denied`: Caller is not a parent, or blocked by provider
- `not-found`: Post not found
- `failed-precondition`: Post is inactive

---

### `respondToBookingRequest`

Provider accepts/declines, or requester cancels/completes.

**Who Can Call:**
- Provider: `accepted`, `declined`
- Requester: `cancelled`, `completed`

**Input:**
```typescript
{
  requestId: string;                    // Required
  newStatus: BookingStatus;             // Required
}
```

**Booking Statuses:** `accepted`, `declined`, `cancelled`, `completed`

**Output:**
```typescript
{ ok: true }
```

**Errors:**
- `permission-denied`: Wrong role for action
- `failed-precondition`: Invalid status transition

**Status Transitions:**
| Current Status | Valid Transitions | Who |
|----------------|-------------------|-----|
| `pending` | `accepted`, `declined`, `cancelled` | Provider (accept/decline), Requester (cancel) |
| `accepted` | `completed`, `cancelled` | Requester only |
| `declined` | - | Terminal |
| `completed` | - | Terminal |
| `cancelled` | - | Terminal |

---

## Chat Functions (Phase 4)

### `createConversationFromAcceptedRequest`

Creates a conversation after booking is accepted.

**Who Can Call:** Participants of an accepted booking request

**Input:**
```typescript
{
  requestId: string;                    // Required
}
```

**Output:**
```typescript
{ ok: true; conversationId: string; existing: boolean }
```

**Errors:**
- `permission-denied`: User not a participant
- `failed-precondition`: Booking not accepted

**Note:** Uses deterministic doc ID (`conv_{requestId}`) to prevent duplicates.

---

### `sendMessage`

Sends a message in an existing conversation.

**Who Can Call:** Parent (primary) or Teen1 Provider (responding only)

**Rate Limit:** 30 per hour

**Input:**
```typescript
{
  conversationId: string;               // Required
  msg_text: string;                     // Required, max 500 chars
}
```

**Output:**
```typescript
{ ok: true; messageId: string }
```

**Errors:**
- `permission-denied`: Not a participant, chat restricted
- `invalid-argument`: PII detected, links detected
- `resource-exhausted`: Rate limit exceeded

---

## Contact Sharing (Milestone C)

### `requestContactShare`

Parent requests to share contact with provider.

**Who Can Call:** Parent users only

**Input:**
```typescript
{
  bookingRequestId: string;             // Required
  cs_type: "phone" | "email";           // Required
}
```

**Output:**
```typescript
{ ok: true; shareId: string }
```

---

### `approveContactShare`

Approves a contact share request (optional - for teen's parent approval).

**Who Can Call:** Teen provider's parent

**Input:**
```typescript
{
  shareId: string;                      // Required
}
```

**Output:**
```typescript
{ ok: true }
```

---

## Scheduled Functions

### `cleanupRateLimits`

Runs every 24 hours to clean up stale rate limit documents.

**Schedule:** `every 24 hours`

**Behavior:** Deletes rate limit documents with `updatedAt` older than 24 hours.

---

## Content Filtering

All user-generated text is filtered for:

1. **PII Detection:**
   - Phone numbers (various formats)
   - Email addresses
   - Physical addresses
   - Social media handles (Snapchat, Instagram, TikTok, Discord, WhatsApp, Telegram, Signal)
   - Meetup phrases

2. **Link Blocking:**
   - HTTP/HTTPS URLs
   - www.* URLs
   - Zoom/Google Meet links
   - URL shorteners (bit.ly, t.co, tinyurl)

3. **Blocked Content:**
   - Profanity (loaded from `/config/blockedContent`)
   - Sexual content
   - Self-harm content

**Normalization:** Leetspeak is normalized ($ -> s, @ -> a, 0 -> o, 1 -> i, 3 -> e, 4 -> a, 5 -> s)

---

## Error Codes

| Code | Meaning |
|------|---------|
| `unauthenticated` | Not signed in |
| `permission-denied` | Not authorized for action |
| `invalid-argument` | Bad input (missing fields, PII, links) |
| `not-found` | Resource doesn't exist |
| `failed-precondition` | Invalid state for action |
| `resource-exhausted` | Rate limit exceeded |
| `internal` | Server error |

---

## Rate Limits Summary

| Function | Limit | Window |
|----------|-------|--------|
| `createPost` | 5 | 1 hour |
| `createComment` | 20 | 1 hour |
| `createBookingRequest` | 10 | 24 hours |
| `reportContent` | 10 | 24 hours |
| `sendMessage` | 30 | 1 hour |
