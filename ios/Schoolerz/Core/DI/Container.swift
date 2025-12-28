import Foundation

@MainActor
final class Container {
    static let shared = Container()
    private init() {}

    // Repository selection based on AppMode
    // In mock mode: always use mock repositories
    // In firebase mode: use Firebase repositories (when implemented)

    lazy var postRepository: PostRepository = {
        switch AppMode.current {
        case .mock:
            return MockPostRepository()
        case .firebase:
            // TODO: Return FirebasePostRepository when implemented
            return MockPostRepository()
        }
    }()

    lazy var commentsRepository: CommentsRepository = {
        switch AppMode.current {
        case .mock:
            return MockCommentsRepository()
        case .firebase:
            // TODO: Return FirebaseCommentsRepository when implemented
            return MockCommentsRepository()
        }
    }()

    lazy var profileRepository: ProfileRepository = {
        switch AppMode.current {
        case .mock:
            return MockProfileRepository()
        case .firebase:
            // TODO: Return FirebaseProfileRepository when implemented
            return MockProfileRepository()
        }
    }()
}
