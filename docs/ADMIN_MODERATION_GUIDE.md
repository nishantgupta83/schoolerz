# Admin Moderation Guide

This guide provides Firestore Console queries and procedures for moderating Schoolerz content and users.

## Firestore Console Queries

### A) Users to Review

Find users who are blocked, have strikes, or are chat-restricted:

```
Collection: users
Filter: usr_isBlocked == true OR usr_strikeCount >= 2 OR usr_isChatRestricted == true
Sort: usr_lastStrikeAt desc
```

**Fields to examine:**
- `usr_publicName`: Display name
- `usr_role`: "teen" or "parent"
- `usr_strikeCount`: Number of violations
- `usr_isBlocked`: Account suspended
- `usr_isChatRestricted`: Cannot send messages
- `usr_lastStrikeAt`: When last violation occurred

---

### B) Open Reports

View all pending reports requiring admin action:

```
Collection: reports
Filter: rpt_status == "pending"
Sort: rpt_createdAt desc
```

**Fields to examine:**
- `rpt_reason`: spam, harassment, inappropriate, scam, pii_leak, grooming, false_positive, other
- `rpt_description`: Reporter's description
- `rpt_contextType`: "post", "message", "user", or "unknown"
- `rpt_contextSnapshot`: Snapshot of reported content at time of report
- `rpt_reporterUserId`: Who filed the report
- `rpt_reporterSnapshot`: Reporter's info at time of report
- `rpt_reportedUserId`: User being reported
- `rpt_reportedPostId`: Post being reported (if applicable)
- `rpt_reportedMessageId`: Message being reported (if applicable)
- `rpt_isFlaggedReporter`: True if reporter has 50+ reports/day (possible spam)

---

### C) High-Volume Posters (Spam Detection)

Check if a specific user is posting excessively:

```
Collection: posts
Filter: pst_authorId == [suspect_uid]
Sort: pst_createdAt desc
Limit: 50
```

**Red flags:**
- More than 5 posts in 1 hour
- Similar/duplicate titles
- Very short or template-like bodies
- Many posts with 0 engagement

---

### D) Booking Request Pressure Monitoring

Identify parents creating excessive booking requests:

```
Collection: bookingRequests
Filter: br_requesterId == [suspect_uid] AND br_status == "pending"
Sort: br_createdAt desc
```

**Watch for:**
- Parent creates 30+ pending requests/day
- Requests to many different providers in short time
- Repeatedly requesting same provider after declines

---

### E) Rate Limit Monitoring

Check for rate limit hits indicating abuse attempts:

```
Collection: rateLimits
Sort: updatedAt desc
Limit: 100
```

**Key patterns:**
- `createPost:uid` - Post creation limits
- `createBookingRequest:uid` - Booking request limits
- `reportContent:uid` - Report submission limits
- `sendMessage:uid` - Message sending limits

**Red flags:**
- Same key with high count near max
- Repeated hits on same key within window

---

### F) Shortlist Activity

Monitor teen shortlisting behavior:

```
Collection: shortlists
Filter: sl_childTeenId == [suspect_uid]
Sort: sl_createdAt desc
```

**Watch for:**
- More than 20 shortlists/day
- Shortlisting same provider multiple times
- Very rapid shortlisting (seconds apart)

---

### G) Blocked Users Pairs

View all active blocks:

```
Collection: blocks
Sort: createdAt desc
```

**Fields:**
- `blockerId`: User who initiated block
- `blockedId`: User being blocked
- `createdAt`: When block was created

---

### H) Content Filter Hits

Check for users repeatedly hitting content filters (requires logs or separate collection):

Look for patterns in Cloud Function logs:
- `Title blocked: PII_PHONE`
- `Body blocked: LINK`
- `Notes blocked: GROOMING`

---

## Moderation Actions

### Issue a Warning (Strike 1)

1. Find user in `users` collection
2. Update fields:
   ```json
   {
     "usr_strikeCount": 1,
     "usr_lastStrikeAt": <current timestamp>
   }
   ```

### Remove Content + Warning (Strike 2)

1. Find the offending post in `posts` collection
2. Update post:
   ```json
   {
     "pst_isActive": false
   }
   ```
3. Update user:
   ```json
   {
     "usr_strikeCount": 2,
     "usr_lastStrikeAt": <current timestamp>
   }
   ```

### Restrict Chat (Strike 3)

1. Update user:
   ```json
   {
     "usr_isChatRestricted": true,
     "usr_strikeCount": 3,
     "usr_lastStrikeAt": <current timestamp>
   }
   ```

### Suspend Account (Strike 5)

1. Update user:
   ```json
   {
     "usr_isBlocked": true,
     "usr_strikeCount": 5,
     "usr_lastStrikeAt": <current timestamp>
   }
   ```

---

## Report Resolution

### Approve Report (Take Action)

1. Apply appropriate strike action (above)
2. Update report:
   ```json
   {
     "rpt_status": "resolved",
     "rpt_resolution": "action_taken",
     "rpt_resolvedBy": "<admin_uid>",
     "rpt_resolvedAt": <current timestamp>,
     "rpt_updatedAt": <current timestamp>
   }
   ```

### Dismiss Report (No Action Needed)

1. Update report:
   ```json
   {
     "rpt_status": "dismissed",
     "rpt_resolution": "no_violation",
     "rpt_resolvedBy": "<admin_uid>",
     "rpt_resolvedAt": <current timestamp>,
     "rpt_updatedAt": <current timestamp>
   }
   ```

### Handle Appeal (reason: false_positive)

1. Review original strike
2. If overturning:
   - Reduce `usr_strikeCount` by 1
   - If `usr_strikeCount` == 0, set `usr_isBlocked` to false
   - Update report with `rpt_resolution: "appeal_granted"`
3. If denying:
   - Update report with `rpt_resolution: "appeal_denied"`

---

## Strike Decay Policy

Strikes decay automatically after 14 days of no violations.

**Manual decay (if needed):**
1. Check `usr_lastStrikeAt`
2. If more than 14 days ago:
   ```json
   {
     "usr_strikeCount": <current - 1>,
     "usr_lastStrikeAt": null
   }
   ```

---

## Flagged Reporter Handling

When `rpt_isFlaggedReporter` is true:

1. Review reporter's recent reports (last 24h)
2. Check for patterns:
   - Reporting same user repeatedly
   - Reporting legitimate content
   - Coordinated reporting with other accounts
3. If abuse confirmed:
   - Issue strike to reporter
   - Dismiss their pending reports
4. If legitimate high-volume reporting:
   - Process reports normally
   - Note: Power users may report many issues legitimately

---

## Emergency Procedures

### Mass Spam Attack

1. Identify common pattern (same content, IP, timing)
2. Query posts by pattern
3. Batch deactivate posts: `pst_isActive: false`
4. Block offending accounts

### Grooming/Safety Concern

1. **Immediately block** the accused account
2. Preserve all evidence (screenshots, exports)
3. Contact legal/safety team
4. Do NOT delete content until cleared by legal

### Data Breach/PII Leak

1. Identify affected content
2. Deactivate/remove content immediately
3. Document what was exposed
4. Contact affected users if identifiable
5. Report per data breach procedures

---

## Daily Moderation Checklist

- [ ] Review all pending reports (`rpt_status == "pending"`)
- [ ] Check for flagged reporters (`rpt_isFlaggedReporter == true`)
- [ ] Review rate limit anomalies
- [ ] Check for new blocked/suspended users
- [ ] Review any escalated issues from previous day

---

## Useful Firestore Console Shortcuts

### Quick User Lookup
```
Collection: users
Document ID: <paste user UID>
```

### Quick Post Lookup
```
Collection: posts
Document ID: <paste post ID>
```

### Recent Posts by User
```
Collection: posts
Filter: pst_authorId == <uid>
Sort: pst_createdAt desc
Limit: 20
```

### User's Booking Requests (as requester)
```
Collection: bookingRequests
Filter: br_requesterId == <uid>
Sort: br_createdAt desc
```

### User's Booking Requests (as provider)
```
Collection: bookingRequests
Filter: br_providerId == <uid>
Sort: br_createdAt desc
```
