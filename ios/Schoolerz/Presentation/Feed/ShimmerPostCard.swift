import SwiftUI

struct ShimmerPostCard: View {
    @State private var phase: CGFloat = 0

    var body: some View {
        VStack(alignment: .leading, spacing: Tokens.Spacing.s) {
            HStack(spacing: Tokens.Spacing.s) {
                Circle().frame(width: 40, height: 40)
                VStack(alignment: .leading, spacing: 4) {
                    RoundedRectangle(cornerRadius: 4).frame(width: 120, height: 14)
                    RoundedRectangle(cornerRadius: 4).frame(width: 80, height: 10)
                }
                Spacer()
                RoundedRectangle(cornerRadius: 8).frame(width: 60, height: 24)
            }
            RoundedRectangle(cornerRadius: 4).frame(height: 14)
            RoundedRectangle(cornerRadius: 4).frame(width: 200, height: 14)
            HStack {
                RoundedRectangle(cornerRadius: 4).frame(width: 40, height: 12)
                RoundedRectangle(cornerRadius: 4).frame(width: 40, height: 12)
                Spacer()
            }
        }
        .padding(Tokens.Spacing.m)
        .foregroundStyle(.gray.opacity(0.3))
        .overlay {
            shimmerOverlay
        }
        .cardStyle()
    }

    private var shimmerOverlay: some View {
        GeometryReader { geo in
            LinearGradient(
                colors: [.clear, .white.opacity(0.4), .clear],
                startPoint: .leading,
                endPoint: .trailing
            )
            .frame(width: geo.size.width * 0.5)
            .offset(x: -geo.size.width * 0.25 + geo.size.width * 1.5 * phase)
            .mask(RoundedRectangle(cornerRadius: Tokens.Radius.medium))
        }
        .onAppear {
            withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                phase = 1
            }
        }
    }
}
