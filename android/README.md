# Schoolerz Android

Jetpack Compose-based Android app for the teen-parent marketplace.

## Requirements
- Android Studio Hedgehog+
- JDK 17
- Android SDK 26+ (target 34)

## Run Locally
1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator/device
4. **No Firebase config needed** - runs in mock mode

## Enable Firebase
1. Add `google-services.json` to `app/` folder
2. App auto-detects and uses Firebase

## Architecture
- **Presentation**: Compose UI + Hilt ViewModels
- **Domain**: Data classes and repository interfaces
- **Data**: Mock (default) and Firebase implementations
- **Core**: DI modules, EncryptedSharedPreferences

## Key Files
| Feature | Path |
|---------|------|
| Feed | `app/src/main/java/com/schoolerz/presentation/feed/` |
| Auth | `app/src/main/java/com/schoolerz/presentation/auth/` |
| Settings | `app/src/main/java/com/schoolerz/presentation/settings/` |
| Models | `app/src/main/java/com/schoolerz/domain/model/` |
| Security | `app/src/main/java/com/schoolerz/core/util/` |
