import Foundation

final class MockPostRepository: PostRepository {
    private lazy var posts: [Post] = MockPostRepository.generateMockPosts()

    func fetchPosts() async throws -> [Post] {
        try await Task.sleep(for: .milliseconds(500))
        return posts.sorted { $0.createdAt > $1.createdAt }
    }

    func createPost(_ post: Post) async throws {
        try await Task.sleep(for: .milliseconds(300))
        posts.insert(post, at: 0)
    }

    private static func generateMockPosts() -> [Post] {
        [
            Post(type: .offer, authorId: "1", authorName: "Alex Kim", neighborhood: "Downtown", body: "Available for dog walking after school! 🐕 $10/hour, experienced with all breeds.", likeCount: 12, commentCount: 3),
            Post(type: .request, authorId: "2", authorName: "Sarah Johnson", neighborhood: "Westside", body: "Looking for a teen to help with yard work this weekend. Will pay $15/hour.", likeCount: 5, commentCount: 2),
            Post(type: .offer, authorId: "3", authorName: "Marcus Chen", neighborhood: "Eastside", body: "Math tutoring available! Currently in AP Calculus, can help with algebra through calculus.", likeCount: 24, commentCount: 7),
            Post(type: .request, authorId: "4", authorName: "Emily Rodriguez", neighborhood: "Northgate", body: "Need someone to walk my kids home from school (3pm pickup). Must be 16+.", likeCount: 8, commentCount: 4),
            Post(type: .offer, authorId: "5", authorName: "Jordan Lee", neighborhood: "Downtown", body: "Tech help for seniors! Can assist with phones, tablets, computers. Patient and friendly.", likeCount: 31, commentCount: 9)
        ]
    }
}
