import Foundation
import Security
import CryptoKit

/// Manages secure storage of sensitive data using iOS Keychain
final class KeychainManager {
    static let shared = KeychainManager()

    private let serviceName = "com.schoolerz.app"
    private let defaults = UserDefaults.standard

    private enum Keys {
        static let pinHash = "pin_hash"
        static let pinSalt = "pin_salt"
        static let biometricEnabled = "biometric_enabled"
        static let failedAttempts = "pin_failed_attempts"
        static let lockoutUntil = "pin_lockout_until"
    }

    private enum Constants {
        static let maxAttempts = 5
        static let lockoutDuration: TimeInterval = 5 * 60 // 5 minutes
    }

    private init() {}

    // MARK: - PIN Management

    func hasStoredPIN() -> Bool {
        return read(key: Keys.pinHash) != nil
    }

    func storePIN(_ pin: String) throws {
        let salt = generateSalt()
        let hash = hashPINWithSalt(pin, salt: salt)
        try save(key: Keys.pinSalt, data: salt)
        try save(key: Keys.pinHash, data: hash)
        resetFailedAttempts()
    }

    func verifyPIN(_ pin: String) -> Bool {
        guard let storedSalt = read(key: Keys.pinSalt),
              let storedHash = read(key: Keys.pinHash) else { return false }
        let inputHash = hashPINWithSalt(pin, salt: storedSalt)
        return storedHash == inputHash
    }

    func deletePIN() throws {
        try delete(key: Keys.pinHash)
        try delete(key: Keys.pinSalt)
        resetFailedAttempts()
    }

    // MARK: - Rate Limiting

    var failedAttempts: Int {
        get { defaults.integer(forKey: Keys.failedAttempts) }
        set { defaults.set(newValue, forKey: Keys.failedAttempts) }
    }

    var lockoutUntil: Date {
        get { Date(timeIntervalSince1970: defaults.double(forKey: Keys.lockoutUntil)) }
        set { defaults.set(newValue.timeIntervalSince1970, forKey: Keys.lockoutUntil) }
    }

    var isLockedOut: Bool {
        Date() < lockoutUntil
    }

    var remainingLockoutSeconds: Int {
        max(0, Int(lockoutUntil.timeIntervalSince(Date())))
    }

    func recordFailedAttempt() {
        failedAttempts += 1
        if failedAttempts >= Constants.maxAttempts {
            lockoutUntil = Date().addingTimeInterval(Constants.lockoutDuration)
            failedAttempts = 0
        }
    }

    func resetFailedAttempts() {
        failedAttempts = 0
        lockoutUntil = Date.distantPast
    }

    // MARK: - Biometric Settings

    var isBiometricEnabled: Bool {
        get { read(key: Keys.biometricEnabled) == "true" }
        set { try? save(key: Keys.biometricEnabled, data: newValue ? "true" : "false") }
    }

    // MARK: - Private Helpers

    private func generateSalt() -> String {
        var bytes = [UInt8](repeating: 0, count: 16)
        _ = SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes)
        return bytes.map { String(format: "%02x", $0) }.joined()
    }

    private func hashPINWithSalt(_ pin: String, salt: String) -> String {
        let input = salt + pin
        let data = Data(input.utf8)
        let hash = SHA256.hash(data: data)
        return hash.map { String(format: "%02x", $0) }.joined()
    }

    private func save(key: String, data: String) throws {
        let dataBytes = Data(data.utf8)

        // Delete existing item first
        try? delete(key: key)

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key,
            kSecValueData as String: dataBytes,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ]

        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else {
            throw KeychainError.saveFailed(status)
        }
    }

    private func read(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let string = String(data: data, encoding: .utf8) else {
            return nil
        }

        return string
    }

    private func delete(key: String) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key
        ]

        let status = SecItemDelete(query as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw KeychainError.deleteFailed(status)
        }
    }
}

enum KeychainError: Error {
    case saveFailed(OSStatus)
    case deleteFailed(OSStatus)
}
