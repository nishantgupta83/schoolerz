# Schoolerz Native

A native mobile marketplace connecting teenagers offering services with parents and community members. Built with **SwiftUI** (iOS) and **Jetpack Compose** (Android).

## Features

- **Social Feed**: Posts for service offers and requests with semantic search
- **Post Detail**: Full view with pricing, availability, skills, comments
- **Profile Management**: Teen profiles with services and school verification
- **PIN Authentication**: Secure local auth with biometric support
- **Email Verification**: School domain whitelist validation
- **Local Notifications**: In-app notification system
- **Mock Mode**: Runs without Firebase for development

## Project Structure

```
schoolerz-native/
├── ios/                     # SwiftUI app (iOS 17+)
│   └── Schoolerz/
│       ├── App/            # Entry point, DI container
│       ├── Core/           # Utilities, AppMode
│       ├── Data/           # Mock & Firebase repositories
│       ├── Domain/         # Models, protocols
│       └── Presentation/   # Views, ViewModels
├── android/                 # Jetpack Compose app (API 26+)
│   └── app/src/main/java/com/schoolerz/
│       ├── core/           # DI, utilities, security
│       ├── data/           # Mock & Firebase repositories
│       ├── domain/         # Models, repository interfaces
│       └── presentation/   # UI screens, ViewModels
├── functions/               # Firebase Cloud Functions (TypeScript)
│   └── src/                # Backend logic
├── firestore.rules          # Firestore Security Rules
└── docs/                    # Shared architecture docs
```

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPS                                  │
│  ┌─────────────────────────┐     ┌─────────────────────────┐       │
│  │      iOS (SwiftUI)      │     │  Android (Compose)      │       │
│  │  ┌───────────────────┐  │     │  ┌───────────────────┐  │       │
│  │  │   Presentation    │  │     │  │   Presentation    │  │       │
│  │  │  Views/ViewModels │  │     │  │ Screens/ViewModels│  │       │
│  │  └─────────┬─────────┘  │     │  └─────────┬─────────┘  │       │
│  │            │            │     │            │            │       │
│  │  ┌─────────▼─────────┐  │     │  ┌─────────▼─────────┐  │       │
│  │  │      Domain       │  │     │  │      Domain       │  │       │
│  │  │ Models/Protocols  │  │     │  │ Models/Interfaces │  │       │
│  │  └─────────┬─────────┘  │     │  └─────────┬─────────┘  │       │
│  │            │            │     │            │            │       │
│  │  ┌─────────▼─────────┐  │     │  ┌─────────▼─────────┐  │       │
│  │  │       Data        │  │     │  │       Data        │  │       │
│  │  │  Mock/Firebase    │  │     │  │  Mock/Firebase    │  │       │
│  │  └───────────────────┘  │     │  └───────────────────┘  │       │
│  └─────────────────────────┘     └─────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      FIREBASE BACKEND                                │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Cloud Functions                           │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │   │
│  │  │ Posts   │ │ Booking │ │  Chat   │ │ Safety  │           │   │
│  │  │ CRUD    │ │ Flow    │ │ System  │ │ Filters │           │   │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │   │
│  │       │           │           │           │                 │   │
│  │       └───────────┴───────────┴───────────┘                 │   │
│  │                           │                                  │   │
│  │                    ┌──────▼──────┐                          │   │
│  │                    │ PII Filter  │                          │   │
│  │                    │ Rate Limit  │                          │   │
│  │                    └──────┬──────┘                          │   │
│  └───────────────────────────┼─────────────────────────────────┘   │
│                              │                                      │
│  ┌───────────────────────────▼─────────────────────────────────┐   │
│  │                     Firestore                                │   │
│  │  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐        │   │
│  │  │ users │ │ posts │ │booking│ │ chats │ │reports│        │   │
│  │  └───────┘ └───────┘ └───────┘ └───────┘ └───────┘        │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                        USER JOURNEY                                   │
└──────────────────────────────────────────────────────────────────────┘

  [App Launch]──▶[PIN Auth]──▶[Feed]──▶[Search/Filter]──▶[Post Detail]
        │            │           │                              │
        ▼            ▼           ▼                              ▼
   ┌─────────┐  ┌─────────┐ ┌─────────┐                  ┌─────────┐
   │Biometric│  │ Secure  │ │Semantic │                  │ Contact │
   │ Option  │  │ Storage │ │ Search  │                  │ Author  │
   └─────────┘  └─────────┘ └─────────┘                  └─────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                      BOOKING FLOW                                     │
└──────────────────────────────────────────────────────────────────────┘

  Teen2 (Browser)          Parent              Teen1 (Provider)
        │                    │                       │
        │  [Shortlist Post]  │                       │
        ├───────────────────▶│                       │
        │                    │  [Create Booking]     │
        │                    ├──────────────────────▶│
        │                    │                       │
        │                    │  [Accept/Decline]     │
        │                    │◀──────────────────────┤
        │                    │                       │
        │                    │  [Chat Opens]         │
        │                    │◀─────────────────────▶│
        │                    │                       │
```

## Tech Stack

| Platform | Technology |
|----------|------------|
| iOS | SwiftUI, @Observable, Keychain, LAContext |
| Android | Jetpack Compose, Hilt DI, StateFlow, EncryptedSharedPreferences |
| Backend | Firebase Cloud Functions v2, Firestore, TypeScript |
| Auth | PIN with salted SHA-256, biometric support |

## Quick Start

### iOS
```bash
cd ios
open Schoolerz.xcodeproj
# Build and run (Cmd+R) - runs in mock mode
```

### Android
```bash
cd android
./gradlew assembleDebug
# Or open in Android Studio and run
```

### Firebase Functions
```bash
cd functions
npm install
npm run build
firebase deploy --only functions
```

**No Firebase configuration required** - apps run in mock mode by default.

## Test Coverage

| Platform | Tests | Coverage |
|----------|-------|----------|
| iOS | 232 | ~80% |
| Android | 237 | ~80% |

### Run Tests

**Android:**
```bash
cd android
./gradlew testDebugUnitTest
```

**iOS:**
```bash
cd ios
xcodebuild test -scheme Schoolerz -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Cloud Functions (11 total)

| Phase | Function | Who Can Call | Description |
|-------|----------|--------------|-------------|
| **Core** | `createPost` | Verified user | Create marketplace post |
| | `createComment` | Any user | Add comment with PII filter |
| | `reportContent` | Any user | Report content |
| | `blockUser` / `unblockUser` | Any user | User blocking |
| **Booking** | `createShortlist` | Teen2 | Ask parent to connect |
| | `createBookingRequest` | Parent | Request service |
| | `respondToBookingRequest` | Teen1/Parent | Accept/decline |
| **Chat** | `createConversationFromAcceptedRequest` | Participants | Create chat |
| | `sendMessage` | Parent + Provider | Send message |
| **Contact** | `requestContactShare` | Parent | Share contact |
| | `approveContactShare` | Provider/Parent | Approve reveal |

## Security Features

- **PII Detection**: Blocks phone, email, address, social handles
- **Link Blocking**: Prevents URLs with leetspeak bypass detection
- **Rate Limiting**: Per-action limits to prevent abuse
- **Parent-First Contact**: Teen contact requires parent approval
- **Salted PIN Hashing**: SHA-256 with per-user salt
- **Biometric Auth**: Face ID / Touch ID / Fingerprint

## Three Persona Model

| Persona | Role | Can Do | Cannot Do |
|---------|------|--------|-----------|
| **Teen1** | Provider | Post services, receive requests | Cold-DM, share PII |
| **Teen2** | Browser | Browse, shortlist, ask parent | Direct message |
| **Parent** | Coordinator | Create requests, chat, share contact | - |

## Firestore Collections

| Collection | Prefix | Description |
|------------|--------|-------------|
| `users` | `usr_` | User profiles |
| `posts` | `pst_` | Marketplace posts |
| `posts/{id}/comments` | `cm_` | Comments |
| `bookingRequests` | `br_` | Service requests |
| `shortlists` | `sl_` | Teen2 → Parent flow |
| `conversations` | `cv_` | Chat conversations |
| `conversations/{id}/messages` | `msg_` | Messages |
| `contactShares` | `cs_` | Contact sharing |

## Enable Firebase (Production)

1. iOS: Add `ios/GoogleService-Info.plist`
2. Android: Add `android/app/google-services.json`
3. Deploy functions: `firebase deploy --only functions`
4. Deploy rules: `firebase deploy --only firestore:rules`

---

## Next Steps

### Phase 1: Core Completion (Current)
- [ ] Integrate Cloud Functions with iOS/Android clients
- [ ] Add real-time chat UI
- [ ] Implement booking flow UI
- [ ] Add image upload for posts/profiles

### Phase 2: Safety & Trust
- [ ] Parent dashboard for monitoring teen activity
- [ ] Background check integration
- [ ] Review and rating system
- [ ] Dispute resolution flow

### Phase 3: Growth
- [ ] Push notifications (FCM/APNs)
- [ ] Deep linking for post sharing
- [ ] Analytics integration (Firebase Analytics)
- [ ] Payment processing (Stripe Connect)

### Phase 4: Launch
- [ ] Accessibility audit (VoiceOver, TalkBack)
- [ ] Full localization (Spanish, more)
- [ ] Performance optimization
- [ ] App Store / Play Store submission
- [ ] Marketing site and landing page

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

Proprietary. All rights reserved.
