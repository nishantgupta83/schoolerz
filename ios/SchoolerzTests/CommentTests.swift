import XCTest
@testable import Schoolerz

final class CommentTests: XCTestCase {

    func testCommentCreationWithDefaults() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John Doe",
            text: "This is a comment"
        )

        XCTAssertFalse(comment.id.isEmpty)
        XCTAssertEqual(comment.authorId, "user123")
        XCTAssertEqual(comment.authorName, "John Doe")
        XCTAssertEqual(comment.text, "This is a comment")
    }

    func testCommentCreationWithAllValues() {
        let createdDate = Date()
        let comment = Comment(
            id: "comment-id",
            authorId: "user456",
            authorName: "Jane Smith",
            text: "Great post!",
            createdAt: createdDate
        )

        XCTAssertEqual(comment.id, "comment-id")
        XCTAssertEqual(comment.authorId, "user456")
        XCTAssertEqual(comment.authorName, "Jane Smith")
        XCTAssertEqual(comment.text, "Great post!")
        XCTAssertEqual(comment.createdAt, createdDate)
    }

    func testCommentAuthorInitialsFromFullName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John Doe",
            text: "Test"
        )

        XCTAssertEqual(comment.authorInitials, "JD")
    }

    func testCommentAuthorInitialsFromSingleName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: "Test"
        )

        XCTAssertEqual(comment.authorInitials, "J")
    }

    func testCommentEquality() {
        let comment1 = Comment(
            id: "same-id",
            authorId: "user123",
            authorName: "John",
            text: "Hello"
        )

        let comment2 = Comment(
            id: "same-id",
            authorId: "user123",
            authorName: "John",
            text: "Hello"
        )

        XCTAssertEqual(comment1, comment2)
    }

    func testCommentTimeAgo() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: "Test"
        )

        // timeAgo should return a non-empty string
        XCTAssertFalse(comment.timeAgo.isEmpty)
    }

    // MARK: - Edge Cases

    func testCommentWithEmptyText() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: ""
        )

        XCTAssertEqual(comment.text, "")
    }

    func testCommentWithVeryLongText() {
        let longText = String(repeating: "A", count: 1000)
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: longText
        )

        XCTAssertEqual(comment.text.count, 1000)
    }

    func testCommentWithSpecialCharacters() {
        let specialText = "Hello! @#$%^&*() \n\t"
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: specialText
        )

        XCTAssertEqual(comment.text, specialText)
    }

    func testCommentAuthorInitialsWithEmptyName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "",
            text: "Test"
        )

        XCTAssertEqual(comment.authorInitials, "")
    }

    func testCommentAuthorInitialsWithLowercaseName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "john doe",
            text: "Test"
        )

        XCTAssertEqual(comment.authorInitials, "JD")
    }

    func testCommentAuthorInitialsWithThreeWordName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John Michael Doe",
            text: "Test"
        )

        XCTAssertEqual(comment.authorInitials, "JM")
    }

    func testCommentWithUnicodeAuthorName() {
        let comment = Comment(
            authorId: "user123",
            authorName: "Jose Garcia",
            text: "Hello"
        )

        XCTAssertEqual(comment.authorName, "Jose Garcia")
    }

    func testCommentHasUniqueIdWhenNotSpecified() {
        let comment1 = Comment(
            authorId: "user123",
            authorName: "John",
            text: "Text 1"
        )
        let comment2 = Comment(
            authorId: "user123",
            authorName: "John",
            text: "Text 2"
        )

        XCTAssertNotEqual(comment1.id, comment2.id)
    }

    func testCommentPostIdIsStored() {
        let comment = Comment(
            postId: "post-123",
            authorId: "user123",
            authorName: "John",
            text: "Test"
        )

        XCTAssertEqual(comment.postId, "post-123")
    }

    func testCommentCreatedAtIsNonNil() {
        let comment = Comment(
            authorId: "user123",
            authorName: "John",
            text: "Test"
        )

        XCTAssertNotNil(comment.createdAt)
    }

    func testMultipleCommentsHaveDifferentIds() {
        var comments: [Comment] = []
        for i in 0..<5 {
            comments.append(Comment(
                authorId: "user\(i)",
                authorName: "User \(i)",
                text: "Comment \(i)"
            ))
        }

        let uniqueIds = Set(comments.map { $0.id })
        XCTAssertEqual(uniqueIds.count, 5)
    }
}
