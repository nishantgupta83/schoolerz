import SwiftUI

struct PostCardView: View {
    let post: Post
    let onComment: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            HStack(spacing: Tokens.Spacing.s) {
                avatar
                VStack(alignment: .leading, spacing: 2) {
                    Text(post.authorName).font(Typography.title)
                    Text("\(post.neighborhood) • \(post.timeAgo)")
                        .font(Typography.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                typeBadge
            }

            Text(post.body)
                .font(Typography.body)
                .lineLimit(4)

            HStack(spacing: Tokens.Spacing.l) {
                Label("\(post.likeCount)", systemImage: "heart")
                Button { onComment() } label: {
                    Label("\(post.commentCount)", systemImage: "bubble.right")
                }
                Spacer()
                Button { } label: {
                    Image(systemName: "square.and.arrow.up")
                }
            }
            .font(Typography.caption)
            .foregroundStyle(.secondary)
        }
        .padding(Tokens.Spacing.m)
        .cardStyle()
    }

    private var avatar: some View {
        Circle()
            .fill(Colors.seed.opacity(0.2))
            .frame(width: 40, height: 40)
            .overlay {
                Text(post.authorInitials)
                    .font(Typography.caption)
                    .foregroundStyle(Colors.seed)
            }
    }

    private var typeBadge: some View {
        Text(post.type.displayName)
            .font(Typography.chip)
            .padding(.horizontal, Tokens.Spacing.s)
            .padding(.vertical, Tokens.Spacing.xs)
            .background(post.type.color.opacity(0.15))
            .foregroundStyle(post.type.color)
            .clipShape(Capsule())
    }
}
