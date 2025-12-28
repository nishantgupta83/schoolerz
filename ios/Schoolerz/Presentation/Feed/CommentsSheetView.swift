import SwiftUI

struct CommentsSheetView: View {
    let post: Post
    @State private var comments: [Comment] = []
    @State private var newComment = ""
    @State private var isLoading = true
    @Environment(\.dismiss) private var dismiss

    private let repository: CommentsRepository = Container.shared.commentsRepository

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isLoading {
                    ProgressView().padding()
                } else if comments.isEmpty {
                    ContentUnavailableView("No comments yet", systemImage: "bubble.left")
                } else {
                    List(comments) { comment in
                        CommentRow(comment: comment)
                    }
                    .listStyle(.plain)
                }

                Divider()
                inputBar
            }
            .navigationTitle("Comments")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
            .task { await loadComments() }
        }
        .presentationDetents([.medium, .large])
    }

    private var inputBar: some View {
        HStack(spacing: Tokens.Spacing.s) {
            TextField("Add a comment...", text: $newComment)
                .textFieldStyle(.roundedBorder)
            Button {
                Task { await addComment() }
            } label: {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.title2)
            }
            .disabled(newComment.trimmingCharacters(in: .whitespaces).isEmpty)
        }
        .padding(Tokens.Spacing.m)
    }

    private func loadComments() async {
        isLoading = true
        comments = (try? await repository.fetchComments(for: post.id)) ?? []
        isLoading = false
    }

    private func addComment() async {
        let comment = Comment(
            postId: post.id,
            authorId: AuthService.shared.currentUserId ?? "anonymous",
            authorName: "Current User",
            text: newComment
        )
        try? await repository.addComment(comment)
        comments.append(comment)
        newComment = ""
    }
}

private struct CommentRow: View {
    let comment: Comment

    var body: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.xs) {
            HStack {
                Text(comment.authorName).font(Typography.caption).bold()
                Spacer()
                Text(comment.timeAgo).font(Typography.timestamp)
            }
            Text(comment.text).font(Typography.body)
        }
        .padding(.vertical, Tokens.Spacing.xs)
    }
}
