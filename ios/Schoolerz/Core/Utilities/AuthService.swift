import Foundation
#if canImport(FirebaseAuth)
import FirebaseAuth
#endif

/// Handles Firebase authentication (anonymous auth on launch)
@MainActor
final class AuthService {
    static let shared = AuthService()

    private init() {}

    var currentUserId: String? {
        #if canImport(FirebaseAuth)
        if AppMode.current == .firebase {
            return Auth.auth().currentUser?.uid
        }
        #endif
        // Mock mode: return a device-based ID
        return "anonymous-user"
    }

    var isAuthenticated: Bool {
        #if canImport(FirebaseAuth)
        if AppMode.current == .firebase {
            return Auth.auth().currentUser != nil
        }
        #endif
        // Mock mode: always authenticated
        return true
    }

    func signInAnonymously() async {
        #if canImport(FirebaseAuth)
        guard AppMode.current == .firebase else {
            print("Mock mode: Skipping Firebase sign-in")
            return
        }
        guard Auth.auth().currentUser == nil else { return }

        do {
            let result = try await Auth.auth().signInAnonymously()
            print("Signed in anonymously: \(result.user.uid)")
        } catch {
            print("Anonymous sign-in failed: \(error.localizedDescription)")
        }
        #else
        print("Mock mode: Firebase not available, skipping sign-in")
        #endif
    }

    func signOut() throws {
        #if canImport(FirebaseAuth)
        if AppMode.current == .firebase {
            try Auth.auth().signOut()
        }
        #endif
    }
}
