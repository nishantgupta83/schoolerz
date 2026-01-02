import XCTest
@testable import Schoolerz

final class PostTests: XCTestCase {

    func testPostCreationWithDefaults() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John Doe",
            neighborhood: "Downtown",
            body: "Test post body"
        )

        XCTAssertFalse(post.id.isEmpty)
        XCTAssertEqual(post.type, .offer)
        XCTAssertEqual(post.authorId, "user123")
        XCTAssertEqual(post.authorName, "John Doe")
        XCTAssertEqual(post.neighborhood, "Downtown")
        XCTAssertEqual(post.body, "Test post body")
        XCTAssertEqual(post.likeCount, 0)
        XCTAssertEqual(post.commentCount, 0)
    }

    func testPostCreationWithAllValues() {
        let createdDate = Date()
        let post = Post(
            id: "custom-id",
            type: .request,
            authorId: "user456",
            authorName: "Jane Smith",
            neighborhood: "Uptown",
            body: "Looking for help",
            likeCount: 10,
            commentCount: 5,
            createdAt: createdDate
        )

        XCTAssertEqual(post.id, "custom-id")
        XCTAssertEqual(post.type, .request)
        XCTAssertEqual(post.likeCount, 10)
        XCTAssertEqual(post.commentCount, 5)
        XCTAssertEqual(post.createdAt, createdDate)
    }

    func testAuthorInitialsFromFullName() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John Doe",
            neighborhood: "Downtown",
            body: "Test"
        )

        XCTAssertEqual(post.authorInitials, "JD")
    }

    func testAuthorInitialsFromSingleName() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test"
        )

        XCTAssertEqual(post.authorInitials, "J")
    }

    func testAuthorInitialsFromThreeWordName() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John Michael Doe",
            neighborhood: "Downtown",
            body: "Test"
        )

        // Takes first 2 initials
        XCTAssertEqual(post.authorInitials, "JM")
    }

    func testPostTypeDisplayName() {
        XCTAssertEqual(PostType.offer.displayName, "Offer")
        XCTAssertEqual(PostType.request.displayName, "Request")
    }

    func testPostTypeColor() {
        // Colors should be non-nil
        XCTAssertNotNil(PostType.offer.color)
        XCTAssertNotNil(PostType.request.color)
    }

    func testPostEquality() {
        let post1 = Post(
            id: "same-id",
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Town",
            body: "Hello"
        )

        let post2 = Post(
            id: "same-id",
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Town",
            body: "Hello"
        )

        XCTAssertEqual(post1, post2)
    }

    func testPostTimeAgo() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Town",
            body: "Test"
        )

        // timeAgo should return a non-empty string
        XCTAssertFalse(post.timeAgo.isEmpty)
    }

    func testPostTypeCaseIterable() {
        let allCases = PostType.allCases
        XCTAssertEqual(allCases.count, 2)
        XCTAssertTrue(allCases.contains(.offer))
        XCTAssertTrue(allCases.contains(.request))
    }

    // MARK: - Pricing Tests

    func testFormattedPriceWithHourlyRate() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            rateAmount: 15,
            rateType: .hourly
        )

        XCTAssertEqual(post.formattedPrice, "$15/hour")
    }

    func testFormattedPriceWithPerTaskRate() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            rateAmount: 30,
            rateType: .perTask
        )

        XCTAssertEqual(post.formattedPrice, "$30/task")
    }

    func testFormattedPriceWithNegotiableRate() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            rateType: .negotiable
        )

        XCTAssertEqual(post.formattedPrice, "Negotiable")
    }

    func testFormattedPriceWithRateRange() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            rateAmount: 15,
            rateMax: 25,
            rateType: .hourly
        )

        XCTAssertEqual(post.formattedPrice, "$15-25/hour")
    }

    // MARK: - Availability Tests

    func testFormattedAvailabilityWithDaysAndTime() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            availableDays: ["Monday", "Wednesday", "Friday"],
            availableTimeStart: "3:00 PM",
            availableTimeEnd: "6:00 PM"
        )

        let availability = post.formattedAvailability
        XCTAssertNotNil(availability)
        XCTAssertTrue(availability?.contains("Mon") ?? false)
        XCTAssertTrue(availability?.contains("Wed") ?? false)
        XCTAssertTrue(availability?.contains("Fri") ?? false)
    }

    func testFormattedAvailabilityWithDaysOnly() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            availableDays: ["Saturday", "Sunday"]
        )

        let availability = post.formattedAvailability
        XCTAssertNotNil(availability)
        XCTAssertTrue(availability?.contains("Sat") ?? false)
        XCTAssertTrue(availability?.contains("Sun") ?? false)
    }

    func testFormattedAvailabilityNilWhenNoDays() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test"
        )

        XCTAssertNil(post.formattedAvailability)
    }

    // MARK: - Service Type and Experience Tests

    func testPostWithServiceType() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Dog walking services",
            serviceType: .dogWalking
        )

        XCTAssertEqual(post.serviceType, .dogWalking)
    }

    func testPostWithExperienceLevel() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            experienceLevel: .experienced
        )

        XCTAssertEqual(post.experienceLevel, .experienced)
    }

    func testPostWithSkillTags() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Test",
            skillTags: ["CPR Certified", "Patient", "Bilingual"]
        )

        XCTAssertEqual(post.skillTags.count, 3)
        XCTAssertTrue(post.skillTags.contains("CPR Certified"))
        XCTAssertTrue(post.skillTags.contains("Patient"))
        XCTAssertTrue(post.skillTags.contains("Bilingual"))
    }

    // MARK: - Edge Cases

    func testAuthorInitialsWithEmptyName() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "",
            neighborhood: "Downtown",
            body: "Test"
        )

        XCTAssertEqual(post.authorInitials, "")
    }

    func testAuthorInitialsWithLowercaseName() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "john doe",
            neighborhood: "Downtown",
            body: "Test"
        )

        XCTAssertEqual(post.authorInitials, "JD")
    }

    func testPostWithEmptyBody() {
        let post = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: ""
        )

        XCTAssertEqual(post.body, "")
    }

    func testPostCopyCreatesNewInstance() {
        let original = Post(
            type: .offer,
            authorId: "user123",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Original"
        )

        var copy = original
        copy = Post(
            id: original.id,
            type: original.type,
            authorId: original.authorId,
            authorName: original.authorName,
            neighborhood: original.neighborhood,
            body: "Modified"
        )

        XCTAssertEqual(original.body, "Original")
        XCTAssertEqual(copy.body, "Modified")
    }

    func testPostWithAllFields() {
        let createdAt = Date()
        let post = Post(
            id: "full-id",
            type: .offer,
            authorId: "full-user",
            authorName: "Full User",
            neighborhood: "Full Town",
            body: "Complete post",
            likeCount: 10,
            commentCount: 5,
            createdAt: createdAt,
            rateAmount: 20,
            rateMax: 30,
            rateType: .hourly,
            availableDays: ["Monday", "Tuesday"],
            availableTimeStart: "9:00 AM",
            availableTimeEnd: "5:00 PM",
            serviceType: .tutoring,
            experienceLevel: .intermediate,
            skillTags: ["Math", "Science"]
        )

        XCTAssertEqual(post.id, "full-id")
        XCTAssertEqual(post.type, .offer)
        XCTAssertEqual(post.authorId, "full-user")
        XCTAssertEqual(post.likeCount, 10)
        XCTAssertEqual(post.commentCount, 5)
        XCTAssertEqual(post.rateAmount, 20)
        XCTAssertEqual(post.rateMax, 30)
        XCTAssertEqual(post.skillTags.count, 2)
    }
}
