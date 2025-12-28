import Foundation

final class MockCommentsRepository: CommentsRepository {
    private var comments: [String: [Comment]] = [:]

    func fetchComments(for postId: String) async throws -> [Comment] {
        try await Task.sleep(for: .milliseconds(300))
        return comments[postId]?.sorted { $0.createdAt < $1.createdAt } ?? []
    }

    func addComment(_ comment: Comment) async throws {
        try await Task.sleep(for: .milliseconds(200))
        comments[comment.postId, default: []].append(comment)
    }
}
