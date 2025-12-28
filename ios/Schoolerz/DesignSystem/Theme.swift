import SwiftUI

/// App-wide theme configuration
enum Theme {
    // MARK: - Card Styling
    static func cardStyle() -> some ViewModifier {
        CardModifier()
    }

    // MARK: - Primary Button Style
    static func primaryButton() -> some ButtonStyle {
        PrimaryButtonStyle()
    }

    // MARK: - Secondary Button Style
    static func secondaryButton() -> some ButtonStyle {
        SecondaryButtonStyle()
    }
}

// MARK: - Card Modifier
struct CardModifier: ViewModifier {
    @Environment(\.colorScheme) var colorScheme

    func body(content: Content) -> some View {
        content
            .background(colorScheme == .dark ? Color(.secondarySystemBackground) : .white)
            .clipShape(RoundedRectangle(cornerRadius: Tokens.Radius.medium))
            .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }
}

// MARK: - Primary Button Style
struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(Typography.button)
            .foregroundStyle(.white)
            .padding(.horizontal, Tokens.Spacing.l)
            .padding(.vertical, Tokens.Spacing.m)
            .background(Colors.seed)
            .clipShape(RoundedRectangle(cornerRadius: Tokens.Radius.small))
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.easeInOut(duration: Tokens.Duration.fast), value: configuration.isPressed)
    }
}

// MARK: - Secondary Button Style
struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(Typography.button)
            .foregroundStyle(Colors.seed)
            .padding(.horizontal, Tokens.Spacing.l)
            .padding(.vertical, Tokens.Spacing.m)
            .background(Colors.seed.opacity(0.1))
            .clipShape(RoundedRectangle(cornerRadius: Tokens.Radius.small))
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.easeInOut(duration: Tokens.Duration.fast), value: configuration.isPressed)
    }
}

// MARK: - View Extensions
extension View {
    func cardStyle() -> some View {
        modifier(CardModifier())
    }
}
