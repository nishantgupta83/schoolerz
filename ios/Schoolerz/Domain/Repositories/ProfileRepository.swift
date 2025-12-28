import Foundation

/// Repository protocol for user profile operations
protocol ProfileRepository {
    /// Get the current user's profile
    func getCurrentProfile() async throws -> Profile?

    /// Save or update user profile
    func saveProfile(_ profile: Profile) async throws

    /// Update display name
    func updateDisplayName(_ name: String) async throws

    /// Update school name
    func updateSchoolName(_ name: String?) async throws

    /// Update avatar path
    func updateAvatarPath(_ path: String?) async throws

    /// Update verification status
    func updateVerificationStatus(_ status: VerificationStatus) async throws

    /// Check if profile exists
    func hasProfile() async -> Bool

    /// Delete profile
    func deleteProfile() async throws
}
