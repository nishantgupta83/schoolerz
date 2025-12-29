import Foundation

/// App mode for switching between mock and Firebase backends
enum AppMode {
    case mock
    case firebase

    /// Determines current mode based on Firebase config existence
    static var current: AppMode = {
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            return .firebase
        }
        return .mock
    }()

    /// Whether Firebase should be initialized
    var shouldInitializeFirebase: Bool {
        self == .firebase
    }

    /// Debug description
    var description: String {
        switch self {
        case .mock: return "Mock Mode"
        case .firebase: return "Firebase Mode"
        }
    }
}
