import Foundation

/// Protocol for post data operations
protocol PostRepository {
    /// Fetch posts ordered by createdAt desc, limited to FETCH_LIMIT
    func fetchPosts() async throws -> [Post]

    /// Create a new post (uses set with client-generated ID for idempotency)
    func createPost(_ post: Post) async throws
}
