# Firestore Schema

All collections use field prefixes to prevent naming collisions and improve query clarity.

## Collections

### `/users/{userId}` (usr_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `usr_publicName` | string | Display name shown publicly (e.g., "Sarah J.") |
| `usr_displayNamePrivate` | string | Full name (never exposed to other users) |
| `usr_avatarUrl` | string? | Profile image URL |
| `usr_neighborhoodId` | string | Neighborhood identifier |
| `usr_zipcode` | string | User's zipcode |
| `usr_services` | string[] | Service types offered |
| `usr_bio` | string | Short bio (max 200 chars) |
| `usr_role` | "teen" \| "parent" | User role |
| `usr_ageGroup` | string | Age group (e.g., "13to17") |
| `usr_parentUserId` | string? | Parent's userId (for teens) |
| `usr_childIds` | string[] | Child userIds (for parents) |
| `usr_verificationStatus` | string | "unverified" \| "verified" |
| `usr_emailVerified` | boolean | Email verification status |
| `usr_phoneVerified` | boolean | Phone verification status |
| `usr_isVerified` | boolean | Overall verification status |
| `usr_isBlocked` | boolean | Account blocked status |
| `usr_isChatRestricted` | boolean | Chat restriction status |
| `usr_strikeCount` | number | Safety strike count |
| `usr_lastStrikeAt` | timestamp? | Last strike timestamp |
| `usr_createdAt` | timestamp | Account creation time |
| `usr_updatedAt` | timestamp | Last update time |

**Privacy Rules:**
- Never expose: `usr_displayNamePrivate`, email, phone, schoolName, gradeLevel
- Only show publicly: `usr_publicName`, `usr_avatarUrl`, `usr_verificationStatus`, `usr_role`, `usr_zipcode`, `usr_neighborhoodId`, `usr_services`, `usr_bio`

---

### `/posts/{postId}` (pst_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `pst_type` | "offer" \| "request" | Post type |
| `pst_authorId` | string | Author's userId |
| `pst_authorSnapshot` | object | Cached author info |
| `pst_title` | string | Post title (max 80 chars) |
| `pst_body` | string | Post content (max 800 chars) |
| `pst_serviceType` | string | Service category |
| `pst_priceType` | "hourly" \| "fixed" \| "free" | Pricing model |
| `pst_priceMin` | number? | Minimum price (cents) |
| `pst_priceMax` | number? | Maximum price (cents) |
| `pst_neighborhoodId` | string | Neighborhood identifier |
| `pst_zipcode` | string | Post zipcode |
| `pst_imageUrls` | string[] | Attached images (max 3) |
| `pst_likeCount` | number | Like count |
| `pst_commentCount` | number | Comment count |
| `pst_isActive` | boolean | Active/visible status |
| `pst_createdAt` | timestamp | Creation time |
| `pst_updatedAt` | timestamp | Last update time |

**Author Snapshot:**
```json
{
  "publicName": "Sarah J.",
  "avatarUrl": null,
  "verificationStatus": "verified",
  "role": "teen"
}
```

**Service Types:** `lawn_care`, `babysitting`, `dog_walking`, `tutoring`, `tech_help`, `errands`, `other`

---

### `/posts/{postId}/comments/{commentId}` (cm_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `cm_authorId` | string | Commenter's userId |
| `cm_authorSnapshot` | object | Cached author info |
| `cm_text` | string | Comment text (max 500 chars) |
| `cm_createdAt` | timestamp | Creation time |

**Note:** Comments MUST go through `createComment` Cloud Function for PII filtering.

---

### `/bookingRequests/{requestId}` (br_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `br_postId` | string | Related post ID |
| `br_postTitle` | string | Cached post title |
| `br_providerId` | string | Provider teen's userId |
| `br_requesterId` | string | Requester parent's userId |
| `br_requesterSnapshot` | object | Cached requester info |
| `br_status` | string | Request status |
| `br_deliveryMode` | "in_person" \| "online" | Delivery preference |
| `br_preferredDays` | string[] | Preferred days |
| `br_timeWindow` | string | Time preference |
| `br_meetingPreference` | string | Meeting type |
| `br_notes` | string | Additional notes (max 300 chars) |
| `br_createdAt` | timestamp | Creation time |
| `br_respondedAt` | timestamp? | Response time |
| `br_updatedAt` | timestamp | Last update time |

**Status Values:** `pending`, `accepted`, `declined`, `completed`, `cancelled`

**Meeting Preferences:** `public_place`, `online`, `parent_will_share_privately`

---

### `/shortlists/{shortlistId}` (sl_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `sl_parentId` | string | Parent's userId |
| `sl_childTeenId` | string | Teen browser's userId |
| `sl_providerTeenId` | string | Provider teen's userId |
| `sl_postId` | string | Related post ID |
| `sl_note` | string | Note from teen to parent |
| `sl_status` | string | Shortlist status |
| `sl_createdAt` | timestamp | Creation time |

**Status Values:** `pending`, `converted`, `dismissed`

---

### `/conversations/{conversationId}` (cv_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `cv_bookingRequestId` | string | Required booking request ID |
| `cv_participants` | string[] | Participant userIds |
| `cv_lastMessageAt` | timestamp | Last message time |
| `cv_createdAt` | timestamp | Creation time |

**Rule:** Conversation requires `cv_bookingRequestId` always. Only created when booking is accepted.

---

### `/conversations/{conversationId}/messages/{messageId}` (msg_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `msg_senderId` | string | Sender's userId |
| `msg_type` | string | Message type |
| `msg_text` | string | Message content |
| `msg_createdAt` | timestamp | Creation time |

**Message Types:** `text`, `contact_shared`, `system`

---

### `/contactShares/{shareId}` (cs_ prefix)

| Field | Type | Description |
|-------|------|-------------|
| `cs_bookingRequestId` | string | Related booking request |
| `cs_fromUserId` | string | Sharing user's ID |
| `cs_toUserId` | string | Receiving user's ID |
| `cs_type` | string | Contact type |
| `cs_createdAt` | timestamp | Creation time |

**Contact Types:** `phone`, `email`

---

### `/reports/{reportId}`

| Field | Type | Description |
|-------|------|-------------|
| `reporterUserId` | string | Reporter's userId |
| `reportedUserId` | string? | Reported user (if applicable) |
| `reportedPostId` | string? | Reported post (if applicable) |
| `reportedMessageId` | string? | Reported message (if applicable) |
| `reason` | string | Report reason |
| `description` | string | Report details |
| `status` | string | Report status |
| `createdAt` | timestamp | Creation time |

**Reasons:** `spam`, `harassment`, `inappropriate`, `scam`, `pii_leak`, `other`

---

### `/blocks/{blockerId}_{blockedId}`

| Field | Type | Description |
|-------|------|-------------|
| `blockerId` | string | Blocking user's ID |
| `blockedId` | string | Blocked user's ID |
| `createdAt` | timestamp | Block time |

---

### `/rateLimits/{key}`

| Field | Type | Description |
|-------|------|-------------|
| `windowStart` | timestamp | Window start time |
| `count` | number | Action count in window |
| `updatedAt` | timestamp | Last update time |

**Note:** Cleaned up by `cleanupRateLimits` scheduled function every 24 hours.

---

### `/config/allowedZipcodes`

| Field | Type | Description |
|-------|------|-------------|
| `zipcodes` | string[] | Allowed zipcodes for rollout |

---

### `/config/blockedContent`

| Field | Type | Description |
|-------|------|-------------|
| `profanity` | string[] | Blocked profanity words |
| `sexual` | string[] | Blocked sexual content |
| `selfHarm` | string[] | Blocked self-harm content |

---

## Indexes

### Feed Queries
```
posts: where pst_zipcode ==, pst_isActive == true, order by pst_createdAt desc
posts: where pst_zipcode ==, pst_serviceType ==, pst_isActive == true, order by pst_createdAt desc
```

### Booking Request Queries
```
bookingRequests: where br_providerId ==, order by br_createdAt desc
bookingRequests: where br_requesterId ==, order by br_createdAt desc
```

---

## Pagination Pattern

All list queries should use cursor-based pagination:

```typescript
// First page
const first = await db.collection('posts')
  .where('pst_zipcode', '==', zipcode)
  .where('pst_isActive', '==', true)
  .orderBy('pst_createdAt', 'desc')
  .limit(20)
  .get();

// Next page
const lastDoc = first.docs[first.docs.length - 1];
const next = await db.collection('posts')
  .where('pst_zipcode', '==', zipcode)
  .where('pst_isActive', '==', true)
  .orderBy('pst_createdAt', 'desc')
  .startAfter(lastDoc)
  .limit(20)
  .get();
```
