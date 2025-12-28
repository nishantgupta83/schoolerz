import SwiftUI

/// Typography styles matching the design system contract
enum Typography {
    // MARK: - Headline (22pt, bold)
    static let headline = Font.system(size: 22, weight: .bold)

    // MARK: - Title (18pt, semibold)
    static let title = Font.system(size: 18, weight: .semibold)

    // MARK: - Body (16pt, regular)
    static let body = Font.system(size: 16, weight: .regular)

    // MARK: - Body Large (17pt, regular)
    static let bodyLarge = Font.system(size: 17, weight: .regular)

    // MARK: - Caption (12pt, medium)
    static let caption = Font.system(size: 12, weight: .medium)

    // MARK: - Small (13pt, regular)
    static let small = Font.system(size: 13, weight: .regular)

    // MARK: - Button (16pt, semibold)
    static let button = Font.system(size: 16, weight: .semibold)

    // MARK: - Chip/Badge (13pt, semibold)
    static let chip = Font.system(size: 13, weight: .semibold)

    // MARK: - Timestamp (12pt, regular)
    static let timestamp = Font.system(size: 12, weight: .regular)
}

// MARK: - View Modifiers for Typography
extension View {
    func headlineStyle() -> some View {
        self.font(Typography.headline)
    }

    func titleStyle() -> some View {
        self.font(Typography.title)
    }

    func bodyStyle() -> some View {
        self.font(Typography.body)
    }

    func captionStyle() -> some View {
        self.font(Typography.caption)
            .foregroundStyle(.secondary)
    }

    func timestampStyle() -> some View {
        self.font(Typography.timestamp)
            .foregroundStyle(.secondary)
    }
}
