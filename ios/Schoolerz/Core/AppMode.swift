import Foundation

/// Determines which backend the app uses
enum AppMode {
    case mock
    case firebase

    /// Current app mode - determined at startup
    static var current: AppMode = {
        // Check if GoogleService-Info.plist exists
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            return .firebase
        }
        return .mock
    }()

    /// Whether Firebase should be initialized
    var shouldInitializeFirebase: Bool {
        self == .firebase
    }
}
