import FirebaseAuth

/// Handles Firebase authentication (anonymous auth on launch)
@MainActor
final class AuthService {
    static let shared = AuthService()

    private init() {}

    var currentUserId: String? {
        Auth.auth().currentUser?.uid
    }

    var isAuthenticated: Bool {
        Auth.auth().currentUser != nil
    }

    func signInAnonymously() async {
        guard Auth.auth().currentUser == nil else { return }

        do {
            let result = try await Auth.auth().signInAnonymously()
            print("Signed in anonymously: \(result.user.uid)")
        } catch {
            print("Anonymous sign-in failed: \(error.localizedDescription)")
        }
    }

    func signOut() throws {
        try Auth.auth().signOut()
    }
}
