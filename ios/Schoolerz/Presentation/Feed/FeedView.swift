import SwiftUI

struct FeedView: View {
    @State private var viewModel = FeedViewModel()
    @State private var showComposer = false
    @State private var selectedPost: Post?
    @State private var showErrorAlert = false

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: Tokens.Spacing.s) {
                    filterPicker

                    if viewModel.isLoading {
                        ForEach(0..<5, id: \.self) { _ in
                            ShimmerPostCard()
                        }
                    } else if viewModel.filteredPosts.isEmpty {
                        emptyState
                    } else {
                        ForEach(viewModel.filteredPosts) { post in
                            PostCardView(post: post) {
                                selectedPost = post
                            }
                        }
                    }
                }
                .padding(.horizontal, Tokens.Spacing.m)
            }
            .refreshable { await viewModel.refreshPosts() }
            .navigationTitle("Feed")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showComposer = true } label: {
                        Image(systemName: "plus.circle.fill")
                    }
                    .accessibilityLabel("Create new post")
                    .accessibilityHint("Opens composer to create an offer or request")
                }
            }
            .sheet(isPresented: $showComposer) {
                PostComposerView { type, body, neighborhood in
                    await viewModel.createPost(type: type, body: body, neighborhood: neighborhood)
                }
            }
            .sheet(item: $selectedPost) { post in
                CommentsSheetView(post: post)
            }
            .task { await viewModel.fetchPosts() }
            .onChange(of: viewModel.errorMessage) { _, newValue in
                showErrorAlert = newValue != nil
            }
            .alert("Error", isPresented: $showErrorAlert) {
                Button("Retry") {
                    Task { await viewModel.fetchPosts() }
                }
                Button("Cancel", role: .cancel) {
                    viewModel.clearError()
                }
            } message: {
                Text(viewModel.errorMessage ?? "An unknown error occurred")
            }
        }
    }

    private var filterPicker: some View {
        Picker("Filter", selection: $viewModel.selectedFilter) {
            Text("All").tag(nil as PostType?)
            Text("Offers").tag(PostType.offer as PostType?)
            Text("Requests").tag(PostType.request as PostType?)
        }
        .pickerStyle(.segmented)
        .padding(.vertical, Tokens.Spacing.s)
    }

    private var emptyState: some View {
        ContentUnavailableView(
            "No posts yet",
            systemImage: "doc.text",
            description: Text("Be the first to post!")
        )
        .padding(.top, Tokens.Spacing.xl)
    }
}
