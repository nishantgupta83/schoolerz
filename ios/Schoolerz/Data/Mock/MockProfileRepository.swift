import Foundation

/// Mock implementation of ProfileRepository for development
final class MockProfileRepository: ProfileRepository {
    private var storedProfile: Profile?

    init() {
        // Initialize with sample data for testing
        storedProfile = Profile(
            displayName: "Alex Johnson",
            schoolName: "Lincoln High School",
            gradeLevel: "11th Grade",
            verificationStatus: .verified,
            services: [.lawnCare, .dogWalking, .techHelp],
            bio: "Hey! I'm a junior who loves helping neighbors. Reliable and friendly!",
            neighborhood: "Westside"
        )
    }

    func fetchProfile() async throws -> Profile? {
        // Simulate network delay
        try await Task.sleep(nanoseconds: 300_000_000)
        return storedProfile
    }

    func saveProfile(_ profile: Profile) async throws {
        // Simulate network delay
        try await Task.sleep(nanoseconds: 200_000_000)
        storedProfile = profile
    }

    func deleteProfile() async throws {
        // Simulate network delay
        try await Task.sleep(nanoseconds: 200_000_000)
        storedProfile = nil
    }
}
