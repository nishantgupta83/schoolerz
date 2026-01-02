import XCTest
@testable import Schoolerz

final class MockPostRepositoryTests: XCTestCase {

    var repository: MockPostRepository!

    override func setUp() {
        super.setUp()
        repository = MockPostRepository()
    }

    override func tearDown() {
        repository = nil
        super.tearDown()
    }

    // MARK: - Fetch Posts Tests

    func testFetchPostsReturnsNonEmptyList() async throws {
        let posts = try await repository.fetchPosts()

        XCTAssertFalse(posts.isEmpty)
    }

    func testFetchPostsReturnsMockData() async throws {
        let posts = try await repository.fetchPosts()

        XCTAssertTrue(posts.count >= 5)
    }

    func testFetchPostsContainsOfferPosts() async throws {
        let posts = try await repository.fetchPosts()
        let offers = posts.filter { $0.type == .offer }

        XCTAssertFalse(offers.isEmpty)
    }

    func testFetchPostsContainsRequestPosts() async throws {
        let posts = try await repository.fetchPosts()
        let requests = posts.filter { $0.type == .request }

        XCTAssertFalse(requests.isEmpty)
    }

    func testFetchPostsHaveRequiredFields() async throws {
        let posts = try await repository.fetchPosts()

        for post in posts {
            XCTAssertFalse(post.id.isEmpty)
            XCTAssertFalse(post.authorId.isEmpty)
            XCTAssertFalse(post.authorName.isEmpty)
            XCTAssertFalse(post.neighborhood.isEmpty)
            XCTAssertFalse(post.body.isEmpty)
        }
    }

    func testFetchPostsAreSortedByDate() async throws {
        let posts = try await repository.fetchPosts()

        if posts.count >= 2 {
            // Posts should be sorted by createdAt descending (newest first)
            for i in 0..<(posts.count - 1) {
                XCTAssertGreaterThanOrEqual(posts[i].createdAt, posts[i + 1].createdAt)
            }
        }
    }

    // MARK: - Create Post Tests

    func testCreatePostAddsToList() async throws {
        let initialPosts = try await repository.fetchPosts()
        let initialCount = initialPosts.count

        let newPost = Post(
            type: .offer,
            authorId: "test-user",
            authorName: "Test User",
            neighborhood: "Test Town",
            body: "Test post body"
        )

        try await repository.createPost(newPost)
        let updatedPosts = try await repository.fetchPosts()

        XCTAssertEqual(updatedPosts.count, initialCount + 1)
    }

    func testCreatePostAppearsFirst() async throws {
        let newPost = Post(
            type: .offer,
            authorId: "new-user",
            authorName: "New User",
            neighborhood: "New Town",
            body: "Brand new post"
        )

        try await repository.createPost(newPost)
        let posts = try await repository.fetchPosts()

        XCTAssertEqual(posts.first?.authorId, "new-user")
        XCTAssertEqual(posts.first?.body, "Brand new post")
    }

    func testCreateMultiplePosts() async throws {
        let initialPosts = try await repository.fetchPosts()
        let initialCount = initialPosts.count

        let post1 = Post(type: .offer, authorId: "user1", authorName: "User One", neighborhood: "Town", body: "Post 1")
        let post2 = Post(type: .request, authorId: "user2", authorName: "User Two", neighborhood: "City", body: "Post 2")

        try await repository.createPost(post1)
        try await repository.createPost(post2)

        let updatedPosts = try await repository.fetchPosts()

        XCTAssertEqual(updatedPosts.count, initialCount + 2)
    }

    func testCreatedPostPreservesAllFields() async throws {
        let newPost = Post(
            type: .offer,
            authorId: "full-user",
            authorName: "Full User",
            neighborhood: "Full Town",
            body: "Complete post",
            likeCount: 5,
            commentCount: 3,
            rateAmount: 15.0,
            rateType: .hourly,
            serviceType: .tutoring
        )

        try await repository.createPost(newPost)
        let posts = try await repository.fetchPosts()
        let createdPost = posts.first { $0.authorId == "full-user" }

        XCTAssertNotNil(createdPost)
        XCTAssertEqual(createdPost?.likeCount, 5)
        XCTAssertEqual(createdPost?.commentCount, 3)
        XCTAssertEqual(createdPost?.rateAmount, 15.0)
        XCTAssertEqual(createdPost?.rateType, .hourly)
        XCTAssertEqual(createdPost?.serviceType, .tutoring)
    }

    // MARK: - Mock Data Quality Tests

    func testMockPostsHavePricing() async throws {
        let posts = try await repository.fetchPosts()
        let postsWithPricing = posts.filter { $0.rateAmount != nil }

        XCTAssertFalse(postsWithPricing.isEmpty)
    }

    func testMockPostsHaveAvailability() async throws {
        let posts = try await repository.fetchPosts()
        let postsWithAvailability = posts.filter { !$0.availableDays.isEmpty }

        XCTAssertFalse(postsWithAvailability.isEmpty)
    }

    func testMockPostsHaveServiceTypes() async throws {
        let posts = try await repository.fetchPosts()
        let postsWithServiceType = posts.filter { $0.serviceType != nil }

        XCTAssertFalse(postsWithServiceType.isEmpty)
    }

    func testMockPostsHaveSkillTags() async throws {
        let posts = try await repository.fetchPosts()
        let postsWithSkillTags = posts.filter { !$0.skillTags.isEmpty }

        XCTAssertFalse(postsWithSkillTags.isEmpty)
    }

    func testMockPostsHaveEngagement() async throws {
        let posts = try await repository.fetchPosts()
        let postsWithEngagement = posts.filter { $0.likeCount > 0 || $0.commentCount > 0 }

        XCTAssertFalse(postsWithEngagement.isEmpty)
    }

    // MARK: - Filtering Tests

    func testFilteringByNeighborhoodWorks() async throws {
        let posts = try await repository.fetchPosts()
        let neighborhoods = Set(posts.map { $0.neighborhood })

        XCTAssertTrue(neighborhoods.count >= 2)
    }

    func testFilteringByServiceTypeWorks() async throws {
        let posts = try await repository.fetchPosts()
        let serviceTypes = Set(posts.compactMap { $0.serviceType })

        XCTAssertTrue(serviceTypes.count >= 2)
    }

    // MARK: - Consistency Tests

    func testMultipleFetchesReturnConsistentData() async throws {
        let posts1 = try await repository.fetchPosts()
        let posts2 = try await repository.fetchPosts()

        XCTAssertEqual(posts1.count, posts2.count)
    }
}
