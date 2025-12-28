import Foundation

/// Protocol for comment data operations
protocol CommentsRepository {
    /// Fetch comments for a specific post, ordered by createdAt asc
    func fetchComments(for postId: String) async throws -> [Comment]

    /// Add a comment to a post (uses set with client-generated ID for idempotency)
    func addComment(_ comment: Comment) async throws
}
