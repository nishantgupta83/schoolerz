# Schoolerz Native

Teen-parent marketplace MVP - native iOS and Android apps.

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

## Mock Mode (Default)
Both apps run in **mock mode** without Firebase configs:
- No `GoogleService-Info.plist` needed for iOS
- No `google-services.json` needed for Android
- Uses local mock data for development

## Enable Firebase (Production)
1. iOS: Add `ios/GoogleService-Info.plist`
2. Android: Add `android/app/google-services.json`
3. Set environment to "firebase" mode

## Project Structure
- `ios/` - SwiftUI app (iOS 17+)
- `android/` - Jetpack Compose app (API 26+)
- `docs/` - Shared architecture and design docs

## Architecture
See `docs/ARCHITECTURE.md` for detailed system design.

## For ChatGPT Review
Point to specific paths:
- iOS Feed: `ios/Schoolerz/Presentation/Feed/`
- Android Feed: `android/app/src/main/java/com/schoolerz/presentation/feed/`
- Shared Models: `docs/MODEL_CONTRACT.md`
- Design System: `docs/DESIGN_SYSTEM.md`
