import SwiftUI
import FirebaseCore

@main
struct SchoolerzApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Only initialize Firebase if in firebase mode (config file exists)
        if AppMode.current.shouldInitializeFirebase {
            FirebaseApp.configure()
            Task {
                await AuthService.shared.signInAnonymously()
            }
        }
        // In mock mode, app runs entirely on local mock data
        return true
    }
}

@MainActor
class AppState: ObservableObject {
    @Published var isUnlocked: Bool = false
    @Published var hasPIN: Bool = false

    init() {
        hasPIN = KeychainManager.shared.hasStoredPIN()
    }
}
