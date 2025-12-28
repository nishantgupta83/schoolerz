import Foundation

@MainActor
final class Container {
    static let shared = Container()
    private init() {}

    lazy var postRepository: PostRepository = MockPostRepository()
    lazy var commentsRepository: CommentsRepository = MockCommentsRepository()
}
