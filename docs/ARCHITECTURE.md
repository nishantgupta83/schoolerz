# System Architecture

## Clean Architecture + MVVM

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│   Views/Screens + ViewModels        │
├─────────────────────────────────────┤
│           Domain Layer              │
│   Models + Repository Protocols     │
├─────────────────────────────────────┤
│            Data Layer               │
│   Firebase/Mock Implementations     │
└─────────────────────────────────────┘
```

## iOS Stack
- SwiftUI + @Observable
- Firebase SDK
- Keychain Services
- LocalAuthentication

## Android Stack
- Jetpack Compose + ViewModel + StateFlow
- Hilt DI
- Firebase SDK
- BiometricPrompt + EncryptedSharedPreferences

## Auth Flow
```
App Launch → Firebase Anonymous Auth (silent)
          → Check PIN exists?
              No  → SetPinView
              Yes → PinLoginView → MainTabView
```

## Data Flow
- Fetch all posts (limit 200, createdAt DESC)
- Pull-to-refresh replaces entire list
- No caching, no realtime (MVP)
- Client-generated UUIDs for idempotent writes
