import SwiftUI

struct TokenDemoView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Tokens.Spacing.l) {
                typographySection
                colorsSection
                spacingSection
                radiusSection
                postCardDemo
            }
            .padding(Tokens.Spacing.m)
        }
        .navigationTitle("Design Tokens")
    }

    private var typographySection: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            Text("Typography").font(Typography.headline)
            Text("Headline (22pt)").font(Typography.headline)
            Text("Title (18pt)").font(Typography.title)
            Text("Body (16pt)").font(Typography.body)
            Text("Caption (12pt)").font(Typography.caption)
        }
    }

    private var colorsSection: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            Text("Colors").font(Typography.headline)
            HStack {
                colorSwatch("Seed", Colors.seed)
                colorSwatch("Offer", Colors.offer)
                colorSwatch("Request", Colors.request)
            }
        }
    }

    private func colorSwatch(_ name: String, _ color: Color) -> some View {
        VStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(color)
                .frame(width: 60, height: 60)
            Text(name).font(Typography.caption)
        }
    }

    private var spacingSection: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            Text("Spacing").font(Typography.headline)
            HStack {
                ForEach([("xs", Tokens.Spacing.xs), ("s", Tokens.Spacing.s), ("m", Tokens.Spacing.m), ("l", Tokens.Spacing.l)], id: \.0) { name, size in
                    VStack {
                        Rectangle().fill(Colors.seed).frame(width: size, height: 40)
                        Text(name).font(Typography.caption)
                    }
                }
            }
        }
    }

    private var radiusSection: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            Text("Corner Radius").font(Typography.headline)
            HStack {
                ForEach([("S", Tokens.Radius.small), ("M", Tokens.Radius.medium), ("L", Tokens.Radius.large)], id: \.0) { name, radius in
                    VStack {
                        RoundedRectangle(cornerRadius: radius)
                            .stroke(Colors.seed, lineWidth: 2)
                            .frame(width: 50, height: 50)
                        Text(name).font(Typography.caption)
                    }
                }
            }
        }
    }

    private var postCardDemo: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            Text("PostCard Demo").font(Typography.headline)
            PostCardView(
                post: Post(type: .offer, authorId: "1", authorName: "Demo User", neighborhood: "Downtown", body: "Sample offer post for design reference", likeCount: 5, commentCount: 2),
                onComment: {}
            )
            PostCardView(
                post: Post(type: .request, authorId: "2", authorName: "Parent User", neighborhood: "Westside", body: "Sample request post for design reference", likeCount: 3, commentCount: 1),
                onComment: {}
            )
        }
    }
}
