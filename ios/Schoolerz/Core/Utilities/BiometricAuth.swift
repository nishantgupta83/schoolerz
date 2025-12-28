import LocalAuthentication

/// Handles Face ID / Touch ID authentication
final class BiometricAuth {
    static let shared = BiometricAuth()

    private init() {}

    enum BiometricType {
        case none
        case touchID
        case faceID
    }

    /// Returns the available biometric type. Creates fresh LAContext each time
    /// to ensure accurate state (LAContext should not be reused).
    var biometricType: BiometricType {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return .none
        }

        switch context.biometryType {
        case .touchID:
            return .touchID
        case .faceID:
            return .faceID
        case .opticID:
            return .faceID // Treat Vision Pro as Face ID equivalent
        case .none:
            return .none
        @unknown default:
            return .none
        }
    }

    var canUseBiometrics: Bool {
        biometricType != .none
    }

    func authenticate(reason: String) async -> Bool {
        let context = LAContext()

        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return false
        }

        do {
            return try await context.evaluatePolicy(
                .deviceOwnerAuthenticationWithBiometrics,
                localizedReason: reason
            )
        } catch {
            return false
        }
    }
}
