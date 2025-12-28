# Schoolerz Native

Teen-parent marketplace MVP - native iOS and Android apps with Firebase backend.

## Project Structure
```
schoolerz-native/
├── ios/                    # SwiftUI app (iOS 17+)
├── android/                # Jetpack Compose app (API 26+)
├── functions/              # Firebase Cloud Functions (TypeScript)
├── firestore.rules         # Firestore Security Rules
└── docs/                   # Shared architecture docs
```

## Quick Start

### iOS
```bash
cd ios
open Schoolerz.xcodeproj
# Build and run (mock mode by default)
```

### Android
```bash
cd android
./gradlew assembleDebug
# Or open in Android Studio
```

### Firebase Functions
```bash
cd functions
npm install
npm run build
firebase deploy --only functions
```

## Mock Mode (Default)
Both apps run in **mock mode** without Firebase configs:
- No `GoogleService-Info.plist` needed for iOS
- No `google-services.json` needed for Android
- Uses local mock data for development

## Firebase Backend

### Cloud Functions (11 total)

| Phase | Function | Who Can Call | Description |
|-------|----------|--------------|-------------|
| **Core** | `createPost` | Any verified user | Create marketplace post |
| | `createComment` | Any user | Add comment with PII filtering |
| | `reportContent` | Any user | Report inappropriate content |
| | `blockUser` | Any user | Block another user |
| | `unblockUser` | Any user | Unblock a user |
| **Booking** | `createShortlist` | Teen2 (with linked parent) | Teen asks parent to connect |
| | `createBookingRequest` | Parent only | Parent requests service |
| | `respondToBookingRequest` | Teen1 or Parent | Accept/decline/complete/cancel |
| **Chat** | `createConversationFromAcceptedRequest` | Participants | Create chat after booking accepted |
| | `sendMessage` | Parent + Provider | Send PII-filtered message |
| **Contact** | `requestContactShare` | Parent only | Share phone/email |
| | `approveContactShare` | Provider/Parent | Approve contact reveal |

### Three Persona Model

| Persona | Role | Can Do | Cannot Do |
|---------|------|--------|-----------|
| **Teen1 (Provider)** | Posts services | Create posts, receive requests, chat after accepted | Cold-DM, share PII in posts |
| **Teen2 (Browser)** | Browses marketplace | Browse, shortlist, ask parent to connect | Message directly |
| **Parent (Coordinator)** | Primary contact | Create booking requests, chat, share contact | - |

### Safety Features
- PII detection (phone, email, address, social handles)
- Link blocking (URLs, short links, zoom/meet)
- Leetspeak normalization for filter bypass prevention
- Rate limiting per action
- Parent-first contact control
- All risky writes via Cloud Functions only

### Firestore Collections

| Collection | Prefix | Description |
|------------|--------|-------------|
| `users` | `usr_` | User profiles |
| `posts` | `pst_` | Marketplace posts |
| `posts/{id}/comments` | `cm_` | Post comments |
| `bookingRequests` | `br_` | Service requests |
| `shortlists` | `sl_` | Teen2 → Parent flow |
| `conversations` | `cv_` | Chat conversations |
| `conversations/{id}/messages` | `msg_` | Chat messages |
| `contactShares` | `cs_` | Contact sharing records |
| `reports` | - | Content reports |
| `blocks` | - | User blocks |

## Enable Firebase (Production)
1. iOS: Add `ios/GoogleService-Info.plist`
2. Android: Add `android/app/google-services.json`
3. Deploy functions: `firebase deploy --only functions`
4. Deploy rules: `firebase deploy --only firestore:rules`

## Architecture
See `docs/ARCHITECTURE.md` for detailed system design.

## Tech Stack
- **iOS**: SwiftUI, @Observable, Keychain, LAContext (iOS 17+)
- **Android**: Jetpack Compose, Hilt DI, StateFlow (API 26+)
- **Backend**: Firebase Cloud Functions v2, Firestore, TypeScript
