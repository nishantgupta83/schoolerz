# App Store / Play Store Compliance Checklist

Pre-launch checklist for kid-safe marketplace apps targeting Apple App Store and Google Play Store.

## Ship-Blockers (Must Have Before Launch)

### Privacy & Transparency

- [x] **Privacy Policy link** inside app AND store listing
- [x] **Terms of Service** accessible from app
- [x] **Parent/Teen role** clearly described in onboarding
- [x] **Data collection disclosure** in app description

### PII Protection

- [x] **No phone/email/address fields** shown in public UI
- [x] **Server-side PII filtering** on all user-generated content
- [x] **publicName only** - never show full names publicly
- [x] **School/grade** never displayed publicly

### UGC Moderation

- [x] **Report button** on every content piece (post, comment, profile)
- [x] **Block button** immediately hides blocked user content
- [x] **Cloud Function-only writes** for risky content (posts, comments, messages)
- [x] **Content filter** catches PII, links, profanity, grooming patterns
- [x] **Strike system** for repeat offenders
- [x] **Admin review queue** for pending reports

### Rate Limiting & Abuse Prevention

- [x] **Rate limits** on all user actions
- [x] **Stricter limits for new users** (first 24h)
- [x] **Duplicate prevention** for booking requests
- [x] **Cooldown after decline** to prevent harassment
- [x] **Reporter spam detection** (50+ reports/day flagged)

### Chat Safety (If Applicable)

- [x] **Context-gated chat** - only after booking accepted
- [x] **Parent + provider only** - Teen2 (browser) cannot chat
- [x] **Server-side message filtering** - same PII/content rules
- [x] **Chat restriction capability** for strike 3+

### Data Retention

- [ ] **Reports retention plan** - how long to keep reports
- [ ] **Logs retention** - Cloud Function logs lifecycle
- [ ] **User data deletion** - process for account deletion requests
- [ ] **COPPA compliance** - parental consent for under-13 (if applicable)

---

## Apple App Store (Kids/UGC)

### App Review Guidelines Compliance

| Requirement | Status | Notes |
|-------------|--------|-------|
| 1.2 Safety - UGC apps must filter/moderate | ✅ | Cloud Functions filter all content |
| 1.2 Safety - Report mechanism required | ✅ | Report button on all content |
| 1.2 Safety - Block mechanism required | ✅ | Block user feature implemented |
| 1.2 Safety - Timely moderation response | ⚠️ | Need admin SLA (24h recommended) |
| 5.1.1 Data Collection - Privacy policy | ✅ | Link in app and store |
| 5.1.4 Kids Category - Enhanced privacy | ⚠️ | If targeting Kids category |

### Age Rating Considerations

- **12+** recommended minimum due to marketplace nature
- No explicit content (filtering ensures this)
- No gambling features
- No unrestricted web access

### Initial Launch Recommendation

- **NO chat initially** reduces App Review friction
- publicName + hidden PII is correct model
- Clean separation of teen/parent roles helps approval

---

## Google Play Store

### Developer Program Policies Compliance

| Requirement | Status | Notes |
|-------------|--------|-------|
| User Generated Content Policy | ✅ | Moderation system in place |
| Child Safety | ✅ | Parent-controlled booking flow |
| Restricted Content - Explicit | ✅ | Content filter active |
| Data Collection | ✅ | Minimal collection, disclosed |
| Families Policy (if applicable) | ⚠️ | See below |

### Families Self-Certified Ads SDK (If Monetizing with Ads)

- [ ] Use only Families-certified ad SDKs
- [ ] Configure child targeting flags in AdMob
- [ ] No behavioral advertising to children
- [ ] No personalized ads to under-13

### Data Safety Section

Fill out Google Play Console's Data Safety section:
- Data collected: Name, email, location (zipcode)
- Data shared: None with third parties
- Security: Encrypted in transit
- Deletion: Users can request deletion

---

## Three Persona Model Compliance

| Persona | Capability | Safety Enforcement |
|---------|------------|-------------------|
| **Teen1 (Provider)** | Posts services, responds in chat after accepted | Cannot initiate contact, PII filtered |
| **Teen2 (Browser)** | Browse, shortlist, ask parent to connect | Cannot message anyone directly |
| **Parent** | Browse, create booking requests, chat after accepted | Primary contact point, verified |

**Key Rule**: No direct teen-to-teen messaging. All contact goes through parent.

---

## Content Filter Coverage

| Attack Vector | Defense | Status |
|---------------|---------|--------|
| Phone with spaces/dashes | Regex normalization | ✅ |
| Email variations | Email pattern detection | ✅ |
| Hidden social handles "i g : name" | Social pattern detection | ✅ |
| Cyrillic lookalikes | Homoglyph normalization | ✅ |
| Leetspeak bypass | Extended leetspeak map | ✅ |
| Google Meet/Zoom links | Link pattern blocking | ✅ |
| "Meet me at..." | Meetup phrase detection | ✅ |
| Address with street types | Address pattern detection | ✅ |
| Grooming phrases (chat) | CHAT mode patterns | ✅ |
| Repeated characters | Character collapse | ✅ |
| Unicode tricks | Unicode stripping | ✅ |

---

## Pre-Launch Security Audit

### Cloud Functions

- [ ] All functions require authentication
- [ ] All functions check `requireNotBlocked()`
- [ ] All user input passes through `filterTextOrReject()`
- [ ] Rate limits on all write operations
- [ ] Role-based access enforced (parent vs teen)

### Firestore Rules

- [ ] No direct client writes to risky collections
- [ ] Read access limited to participants
- [ ] Admin fields protected from client updates

### Client Apps

- [ ] No hardcoded secrets
- [ ] API keys properly restricted
- [ ] Certificate pinning (optional but recommended)
- [ ] No debug logs in production builds

---

## Abuse Stress-Tests Results

Run these tests before launch:

| Test | Expected Behavior | Pass? |
|------|-------------------|-------|
| Post with phone number | Rejected with `PII_PHONE` | [ ] |
| Post with email | Rejected with `PII_EMAIL` | [ ] |
| Post with Instagram handle | Rejected with `PII_SOCIAL` | [ ] |
| Post with Google Meet link | Rejected with `LINK` | [ ] |
| Post with "meet me at 123 Main St" | Rejected with `PII_ADDRESS` or `PII_MEETUP` | [ ] |
| Spam 10 posts in 1 hour | Rate limit after 5 | [ ] |
| New user spam 5 posts in 1 hour | Rate limit after 2 | [ ] |
| Teen tries to create parent-type request | Permission denied | [ ] |
| Parent tries to create teen-type offer | Permission denied | [ ] |
| Blocked user tries to interact | Permission denied | [ ] |
| Duplicate booking request | Failed precondition | [ ] |
| Request after recent decline | Failed precondition (cooldown) | [ ] |
| Teen2 tries to message directly | No messaging available | [ ] |
| Report with profanity in description | Rejected | [ ] |

---

## Launch Readiness Summary

### Critical (Must Fix)
- [ ] Data retention policy documented
- [ ] Admin moderation SLA defined (recommend 24h)
- [ ] COPPA consent flow (if under-13 users)

### Important (Should Fix)
- [ ] Account deletion request process
- [ ] Privacy policy reviewed by legal
- [ ] Terms of service reviewed by legal

### Nice to Have
- [ ] Automated abuse detection alerts
- [ ] Admin dashboard UI (vs Firestore Console)
- [ ] User feedback/appeal system in-app

---

## Post-Launch Monitoring

### Daily

- Review all pending reports
- Check rate limit anomalies
- Monitor Cloud Function errors

### Weekly

- Review strike patterns
- Analyze content filter false positives
- Check for new abuse patterns

### Monthly

- Review blocked user appeals
- Update blocked content lists
- Policy compliance review
