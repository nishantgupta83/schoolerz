import XCTest
@testable import Schoolerz

final class ProfileTests: XCTestCase {

    func testProfileCreationWithRequiredValues() {
        let profile = Profile(
            id: "user123",
            displayName: "John Doe"
        )

        XCTAssertEqual(profile.id, "user123")
        XCTAssertEqual(profile.displayName, "John Doe")
        XCTAssertNil(profile.schoolName)
        XCTAssertFalse(profile.emailVerified)
        XCTAssertEqual(profile.verificationStatus, .unverified)
        XCTAssertTrue(profile.services.isEmpty)
    }

    func testProfileCreationWithAllValues() {
        let services: [ServiceType] = [.tutoring, .babysitting]
        let profile = Profile(
            id: "user456",
            displayName: "Jane Smith",
            schoolName: "Test High School",
            emailVerified: true,
            verificationStatus: .verified,
            services: services,
            bio: "Experienced tutor"
        )

        XCTAssertEqual(profile.id, "user456")
        XCTAssertEqual(profile.displayName, "Jane Smith")
        XCTAssertEqual(profile.schoolName, "Test High School")
        XCTAssertTrue(profile.emailVerified)
        XCTAssertEqual(profile.verificationStatus, .verified)
        XCTAssertEqual(profile.services.count, 2)
        XCTAssertEqual(profile.bio, "Experienced tutor")
    }

    func testVerificationStatusValues() {
        XCTAssertEqual(VerificationStatus.allCases.count, 3)
        XCTAssertTrue(VerificationStatus.allCases.contains(.unverified))
        XCTAssertTrue(VerificationStatus.allCases.contains(.pending))
        XCTAssertTrue(VerificationStatus.allCases.contains(.verified))
    }

    func testServiceTypeValues() {
        XCTAssertEqual(ServiceType.allCases.count, 7)
        XCTAssertTrue(ServiceType.allCases.contains(.lawnCare))
        XCTAssertTrue(ServiceType.allCases.contains(.babysitting))
        XCTAssertTrue(ServiceType.allCases.contains(.dogWalking))
        XCTAssertTrue(ServiceType.allCases.contains(.tutoring))
        XCTAssertTrue(ServiceType.allCases.contains(.techHelp))
        XCTAssertTrue(ServiceType.allCases.contains(.errands))
        XCTAssertTrue(ServiceType.allCases.contains(.other))
    }

    func testServiceTypeDisplayName() {
        XCTAssertEqual(ServiceType.lawnCare.displayName, "Lawn Care")
        XCTAssertEqual(ServiceType.babysitting.displayName, "Babysitting")
        XCTAssertEqual(ServiceType.dogWalking.displayName, "Dog Walking")
        XCTAssertEqual(ServiceType.tutoring.displayName, "Tutoring")
        XCTAssertEqual(ServiceType.techHelp.displayName, "Tech Help")
        XCTAssertEqual(ServiceType.errands.displayName, "Errands")
        XCTAssertEqual(ServiceType.other.displayName, "Other")
    }

    func testProfileEquality() {
        let profile1 = Profile(id: "same-id", displayName: "John")
        let profile2 = Profile(id: "same-id", displayName: "John")

        XCTAssertEqual(profile1, profile2)
    }

    // MARK: - Profile Initials Tests

    func testProfileInitialsWithTwoNames() {
        let profile = Profile(displayName: "John Smith")
        XCTAssertEqual(profile.initials, "JS")
    }

    func testProfileInitialsWithSingleName() {
        let profile = Profile(displayName: "John")
        XCTAssertEqual(profile.initials, "J")
    }

    func testProfileInitialsWithThreeNames() {
        let profile = Profile(displayName: "John Paul Smith")
        XCTAssertEqual(profile.initials, "JP")
    }

    func testProfileInitialsWithLowercaseName() {
        let profile = Profile(displayName: "john smith")
        XCTAssertEqual(profile.initials, "JS")
    }

    func testProfileInitialsWithEmptyName() {
        let profile = Profile(displayName: "")
        XCTAssertEqual(profile.initials, "")
    }

    // MARK: - Verification Status Tests

    func testVerificationStatusDisplayNames() {
        XCTAssertEqual(VerificationStatus.unverified.displayName, "Unverified")
        XCTAssertEqual(VerificationStatus.pending.displayName, "Pending")
        XCTAssertEqual(VerificationStatus.verified.displayName, "Verified")
    }

    func testIsVerifiedWhenVerified() {
        let profile = Profile(
            displayName: "John",
            verificationStatus: .verified
        )
        XCTAssertTrue(profile.isVerified)
    }

    func testIsVerifiedFalseWhenUnverified() {
        let profile = Profile(
            displayName: "John",
            verificationStatus: .unverified
        )
        XCTAssertFalse(profile.isVerified)
    }

    func testIsVerifiedFalseWhenPending() {
        let profile = Profile(
            displayName: "John",
            verificationStatus: .pending
        )
        XCTAssertFalse(profile.isVerified)
    }

    // MARK: - Optional Fields Tests

    func testProfileWithNilSchoolName() {
        let profile = Profile(displayName: "John")
        XCTAssertNil(profile.schoolName)
    }

    func testProfileWithNilBio() {
        let profile = Profile(displayName: "John")
        XCTAssertNil(profile.bio)
    }

    func testProfileWithNilNeighborhood() {
        let profile = Profile(displayName: "John")
        XCTAssertNil(profile.neighborhood)
    }

    func testProfileWithNilAvatarPath() {
        let profile = Profile(displayName: "John")
        XCTAssertNil(profile.avatarPath)
    }

    // MARK: - Services Tests

    func testProfileWithEmptyServices() {
        let profile = Profile(displayName: "John")
        XCTAssertTrue(profile.services.isEmpty)
    }

    func testProfileWithMultipleServices() {
        let profile = Profile(
            displayName: "John",
            services: [.tutoring, .babysitting, .dogWalking]
        )
        XCTAssertEqual(profile.services.count, 3)
    }

    // MARK: - Complete Profile Tests

    func testProfileWithAllFields() {
        let createdAt = Date()
        let profile = Profile(
            id: "full-user",
            displayName: "Full User",
            schoolName: "Full School",
            gradeLevel: "12th Grade",
            avatarPath: "/images/avatar.jpg",
            verificationStatus: .verified,
            services: [.tutoring, .babysitting],
            bio: "Complete bio",
            neighborhood: "Downtown",
            createdAt: createdAt
        )

        XCTAssertEqual(profile.id, "full-user")
        XCTAssertEqual(profile.displayName, "Full User")
        XCTAssertEqual(profile.schoolName, "Full School")
        XCTAssertEqual(profile.gradeLevel, "12th Grade")
        XCTAssertEqual(profile.avatarPath, "/images/avatar.jpg")
        XCTAssertEqual(profile.verificationStatus, .verified)
        XCTAssertEqual(profile.services.count, 2)
        XCTAssertEqual(profile.bio, "Complete bio")
        XCTAssertEqual(profile.neighborhood, "Downtown")
        XCTAssertEqual(profile.createdAt, createdAt)
    }

    func testProfileHasDefaultCreatedAt() {
        let profile = Profile(displayName: "John")
        XCTAssertNotNil(profile.createdAt)
    }

    func testProfileHasDefaultId() {
        let profile = Profile(displayName: "John")
        XCTAssertFalse(profile.id.isEmpty)
    }
}
