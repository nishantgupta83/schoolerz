import Foundation

/// Local persistence implementation of ProfileRepository using UserDefaults
final class LocalProfileRepository: ProfileRepository {
    private let defaults = UserDefaults.standard
    private let profileKey = "com.schoolerz.profile"

    func fetchProfile() async throws -> Profile? {
        // Simulate small delay for consistency with remote calls
        try await Task.sleep(nanoseconds: 100_000_000)

        guard let data = defaults.data(forKey: profileKey) else {
            return nil
        }

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode(Profile.self, from: data)
    }

    func saveProfile(_ profile: Profile) async throws {
        // Simulate small delay
        try await Task.sleep(nanoseconds: 100_000_000)

        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        let data = try encoder.encode(profile)
        defaults.set(data, forKey: profileKey)
    }

    func deleteProfile() async throws {
        // Simulate small delay
        try await Task.sleep(nanoseconds: 100_000_000)
        defaults.removeObject(forKey: profileKey)
    }
}
