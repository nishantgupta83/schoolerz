import SwiftUI
import UserNotifications
#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
struct SchoolerzApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .task {
                    // Set notification delegate to enable foreground notifications
                    UNUserNotificationCenter.current().delegate = NotificationManager.shared
                }
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Only initialize Firebase if in firebase mode and config exists
        if AppMode.current.shouldInitializeFirebase {
            #if canImport(FirebaseCore)
            FirebaseApp.configure()
            Task {
                await AuthService.shared.signInAnonymously()
            }
            #endif
        }
        print("App launched in \(AppMode.current.description)")
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
