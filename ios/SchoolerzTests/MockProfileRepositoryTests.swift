import XCTest
@testable import Schoolerz

final class MockProfileRepositoryTests: XCTestCase {

    var repository: MockProfileRepository!

    override func setUp() {
        super.setUp()
        repository = MockProfileRepository()
    }

    override func tearDown() {
        repository = nil
        super.tearDown()
    }

    // MARK: - Fetch Profile Tests

    func testFetchProfileReturnsProfile() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertNotNil(profile)
    }

    func testFetchProfileReturnsInitialMockData() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertEqual(profile?.displayName, "Alex Johnson")
        XCTAssertEqual(profile?.schoolName, "Lincoln High School")
        XCTAssertEqual(profile?.gradeLevel, "11th Grade")
    }

    func testFetchProfileReturnsVerifiedStatus() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertEqual(profile?.verificationStatus, .verified)
    }

    func testFetchProfileReturnsServices() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertNotNil(profile?.services)
        XCTAssertFalse(profile?.services.isEmpty ?? true)
        XCTAssertTrue(profile?.services.contains(.lawnCare) ?? false)
        XCTAssertTrue(profile?.services.contains(.dogWalking) ?? false)
        XCTAssertTrue(profile?.services.contains(.techHelp) ?? false)
    }

    func testFetchProfileReturnsBio() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertNotNil(profile?.bio)
        XCTAssertFalse(profile?.bio?.isEmpty ?? true)
    }

    func testFetchProfileReturnsNeighborhood() async throws {
        let profile = try await repository.fetchProfile()

        XCTAssertEqual(profile?.neighborhood, "Westside")
    }

    // MARK: - Save Profile Tests

    func testSaveProfileUpdatesProfile() async throws {
        let newProfile = Profile(
            id: "new-user",
            displayName: "New User",
            schoolName: "New School"
        )

        try await repository.saveProfile(newProfile)
        let fetchedProfile = try await repository.fetchProfile()

        XCTAssertEqual(fetchedProfile?.displayName, "New User")
        XCTAssertEqual(fetchedProfile?.schoolName, "New School")
    }

    func testSaveProfilePreservesAllFields() async throws {
        let profile = Profile(
            id: "complete-user",
            displayName: "Complete User",
            schoolName: "Complete School",
            gradeLevel: "12th Grade",
            verificationStatus: .pending,
            services: [.tutoring, .babysitting],
            bio: "Complete bio",
            neighborhood: "Complete Neighborhood"
        )

        try await repository.saveProfile(profile)
        let fetchedProfile = try await repository.fetchProfile()

        XCTAssertEqual(fetchedProfile?.id, "complete-user")
        XCTAssertEqual(fetchedProfile?.displayName, "Complete User")
        XCTAssertEqual(fetchedProfile?.schoolName, "Complete School")
        XCTAssertEqual(fetchedProfile?.gradeLevel, "12th Grade")
        XCTAssertEqual(fetchedProfile?.verificationStatus, .pending)
        XCTAssertEqual(fetchedProfile?.services.count, 2)
        XCTAssertEqual(fetchedProfile?.bio, "Complete bio")
        XCTAssertEqual(fetchedProfile?.neighborhood, "Complete Neighborhood")
    }

    func testSaveProfileReplacesExisting() async throws {
        let profile1 = Profile(id: "user1", displayName: "User 1")
        let profile2 = Profile(id: "user2", displayName: "User 2")

        try await repository.saveProfile(profile1)
        try await repository.saveProfile(profile2)

        let fetchedProfile = try await repository.fetchProfile()

        XCTAssertEqual(fetchedProfile?.displayName, "User 2")
    }

    // MARK: - Delete Profile Tests

    func testDeleteProfileRemovesProfile() async throws {
        try await repository.deleteProfile()
        let fetchedProfile = try await repository.fetchProfile()

        XCTAssertNil(fetchedProfile)
    }

    func testSaveProfileAfterDeleteWorks() async throws {
        try await repository.deleteProfile()

        let newProfile = Profile(id: "restored", displayName: "Restored User")
        try await repository.saveProfile(newProfile)

        let fetchedProfile = try await repository.fetchProfile()

        XCTAssertNotNil(fetchedProfile)
        XCTAssertEqual(fetchedProfile?.displayName, "Restored User")
    }

    func testMultipleDeletesAreSafe() async throws {
        try await repository.deleteProfile()
        try await repository.deleteProfile()

        let fetchedProfile = try await repository.fetchProfile()
        XCTAssertNil(fetchedProfile)
    }

    // MARK: - Consistency Tests

    func testMultipleFetchesReturnSameData() async throws {
        let profile1 = try await repository.fetchProfile()
        let profile2 = try await repository.fetchProfile()

        XCTAssertEqual(profile1?.displayName, profile2?.displayName)
        XCTAssertEqual(profile1?.schoolName, profile2?.schoolName)
    }

    func testNewRepositoryInstanceHasSameInitialData() async throws {
        let repo1 = MockProfileRepository()
        let repo2 = MockProfileRepository()

        let profile1 = try await repo1.fetchProfile()
        let profile2 = try await repo2.fetchProfile()

        XCTAssertEqual(profile1?.displayName, profile2?.displayName)
    }
}
