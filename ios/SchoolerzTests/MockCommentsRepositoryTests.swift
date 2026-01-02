import XCTest
@testable import Schoolerz

final class MockCommentsRepositoryTests: XCTestCase {

    var repository: MockCommentsRepository!

    override func setUp() {
        super.setUp()
        repository = MockCommentsRepository()
    }

    override func tearDown() {
        repository = nil
        super.tearDown()
    }

    // MARK: - Fetch Comments Tests

    func testFetchCommentsReturnsEmptyListForNewPost() async throws {
        let comments = try await repository.fetchComments(for: "new-post-id")

        XCTAssertTrue(comments.isEmpty)
    }

    func testFetchCommentsReturnsEmptyListForNonExistentPost() async throws {
        let comments = try await repository.fetchComments(for: "non-existent-post")

        XCTAssertTrue(comments.isEmpty)
    }

    // MARK: - Add Comment Tests

    func testAddCommentSucceeds() async throws {
        let comment = Comment(
            postId: "post-1",
            authorId: "user-1",
            authorName: "John Doe",
            text: "Great post!"
        )

        try await repository.addComment(comment)
        let comments = try await repository.fetchComments(for: "post-1")

        XCTAssertEqual(comments.count, 1)
        XCTAssertEqual(comments.first?.text, "Great post!")
    }

    func testAddMultipleCommentsOnSamePost() async throws {
        let comment1 = Comment(postId: "post-1", authorId: "user-1", authorName: "User One", text: "First comment")
        let comment2 = Comment(postId: "post-1", authorId: "user-2", authorName: "User Two", text: "Second comment")

        try await repository.addComment(comment1)
        try await repository.addComment(comment2)

        let comments = try await repository.fetchComments(for: "post-1")

        XCTAssertEqual(comments.count, 2)
    }

    func testCommentsOnDifferentPostsAreSeparate() async throws {
        let comment1 = Comment(postId: "post-1", authorId: "user-1", authorName: "User One", text: "Comment on post 1")
        let comment2 = Comment(postId: "post-2", authorId: "user-2", authorName: "User Two", text: "Comment on post 2")

        try await repository.addComment(comment1)
        try await repository.addComment(comment2)

        let commentsPost1 = try await repository.fetchComments(for: "post-1")
        let commentsPost2 = try await repository.fetchComments(for: "post-2")

        XCTAssertEqual(commentsPost1.count, 1)
        XCTAssertEqual(commentsPost2.count, 1)
        XCTAssertEqual(commentsPost1.first?.text, "Comment on post 1")
        XCTAssertEqual(commentsPost2.first?.text, "Comment on post 2")
    }

    // MARK: - Sorting Tests

    func testCommentsAreSortedByCreatedAt() async throws {
        let oldDate = Date(timeIntervalSinceNow: -10000)
        let newDate = Date()

        let oldComment = Comment(
            postId: "post-1",
            authorId: "user-1",
            authorName: "User",
            text: "Old comment",
            createdAt: oldDate
        )
        let newComment = Comment(
            postId: "post-1",
            authorId: "user-2",
            authorName: "User",
            text: "New comment",
            createdAt: newDate
        )

        // Add in reverse order
        try await repository.addComment(newComment)
        try await repository.addComment(oldComment)

        let comments = try await repository.fetchComments(for: "post-1")

        XCTAssertEqual(comments.count, 2)
        XCTAssertEqual(comments[0].text, "Old comment")
        XCTAssertEqual(comments[1].text, "New comment")
    }

    // MARK: - Comment Preservation Tests

    func testCommentPreservesAllFields() async throws {
        let comment = Comment(
            id: "custom-id",
            postId: "post-1",
            authorId: "user-1",
            authorName: "John Doe",
            text: "Test comment"
        )

        try await repository.addComment(comment)
        let comments = try await repository.fetchComments(for: "post-1")
        let fetchedComment = comments.first

        XCTAssertNotNil(fetchedComment)
        XCTAssertEqual(fetchedComment?.id, "custom-id")
        XCTAssertEqual(fetchedComment?.postId, "post-1")
        XCTAssertEqual(fetchedComment?.authorId, "user-1")
        XCTAssertEqual(fetchedComment?.authorName, "John Doe")
        XCTAssertEqual(fetchedComment?.text, "Test comment")
    }

    func testCommentWithSpecialCharacters() async throws {
        let comment = Comment(
            postId: "post-1",
            authorId: "user-1",
            authorName: "Jose Garcia",
            text: "Great post! #awesome @mention"
        )

        try await repository.addComment(comment)
        let comments = try await repository.fetchComments(for: "post-1")

        XCTAssertEqual(comments.first?.authorName, "Jose Garcia")
        XCTAssertEqual(comments.first?.text, "Great post! #awesome @mention")
    }

    // MARK: - Multiple Repository Independence

    func testMultipleRepositoriesAreIndependent() async throws {
        let repo1 = MockCommentsRepository()
        let repo2 = MockCommentsRepository()

        let comment = Comment(
            postId: "post-1",
            authorId: "user-1",
            authorName: "User",
            text: "Comment"
        )

        try await repo1.addComment(comment)

        let comments1 = try await repo1.fetchComments(for: "post-1")
        let comments2 = try await repo2.fetchComments(for: "post-1")

        XCTAssertEqual(comments1.count, 1)
        XCTAssertEqual(comments2.count, 0)
    }

    // MARK: - Bulk Comments

    func testManyCommentsOnSamePost() async throws {
        let postId = "busy-post"

        for i in 0..<10 {
            let comment = Comment(
                postId: postId,
                authorId: "user-\(i)",
                authorName: "User \(i)",
                text: "Comment \(i)"
            )
            try await repository.addComment(comment)
        }

        let comments = try await repository.fetchComments(for: postId)

        XCTAssertEqual(comments.count, 10)
    }
}
