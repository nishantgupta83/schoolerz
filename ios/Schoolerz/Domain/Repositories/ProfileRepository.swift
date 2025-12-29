import Foundation

/// Repository protocol for user profiles
protocol ProfileRepository {
    /// Fetches the current user's profile
    func fetchProfile() async throws -> Profile?

    /// Saves or updates a profile
    func saveProfile(_ profile: Profile) async throws

    /// Deletes the current profile
    func deleteProfile() async throws
}
