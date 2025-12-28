import SwiftUI

@Observable
@MainActor
final class FeedViewModel {
    var isLoading = false
    var isRefreshing = false
    var posts: [Post] = []
    var errorMessage: String?
    var selectedFilter: PostType?

    private let repository: PostRepository

    init(repository: PostRepository = Container.shared.postRepository) {
        self.repository = repository
    }

    var filteredPosts: [Post] {
        guard let filter = selectedFilter else { return posts }
        return posts.filter { $0.type == filter }
    }

    func fetchPosts() async {
        isLoading = true
        errorMessage = nil
        do {
            posts = try await repository.fetchPosts()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func refreshPosts() async {
        isRefreshing = true
        do {
            posts = try await repository.fetchPosts()
        } catch {
            errorMessage = error.localizedDescription
        }
        isRefreshing = false
    }

    func createPost(type: PostType, body: String, neighborhood: String) async {
        let post = Post(
            type: type,
            authorId: AuthService.shared.currentUserId ?? "anonymous",
            authorName: "Current User",
            neighborhood: neighborhood,
            body: body
        )
        do {
            try await repository.createPost(post)
            posts.insert(post, at: 0)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func clearError() {
        errorMessage = nil
    }
}
