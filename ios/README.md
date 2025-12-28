# Schoolerz iOS

SwiftUI-based iOS app for the teen-parent marketplace.

## Requirements
- Xcode 15+
- iOS 17+
- Swift 5.9+

## Run Locally
1. Open `Schoolerz.xcodeproj`
2. Build and run (Cmd+R)
3. **No Firebase config needed** - runs in mock mode

## Enable Firebase
1. Add `GoogleService-Info.plist` to project root
2. Ensure file is added to Xcode target
3. App auto-detects and uses Firebase

## Architecture
- **Presentation**: SwiftUI views + @Observable ViewModels
- **Domain**: Models and repository protocols
- **Data**: Mock (default) and Firebase implementations
- **Core**: DI container, Keychain, Biometric auth

## Key Files
| Feature | Path |
|---------|------|
| Feed | `Schoolerz/Presentation/Feed/` |
| Auth | `Schoolerz/Presentation/Auth/` |
| Settings | `Schoolerz/Presentation/Settings/` |
| Models | `Schoolerz/Domain/Models/` |
| Security | `Schoolerz/Core/Utilities/` |
