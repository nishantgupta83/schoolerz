import SwiftUI

/// Brand and semantic colors
enum Colors {
    // MARK: - Brand Colors
    static let seed = Color(hex: 0x2563EB)
    static let offer = Color(hex: 0x10B981)  // Green for offers
    static let request = Color(hex: 0xF59E0B)  // Amber for requests

    // MARK: - Semantic Colors
    static let textPrimary = Color.primary
    static let textSecondary = Color.secondary
    static let background = Color(.systemBackground)
    static let cardBackground = Color(.secondarySystemBackground)
    static let divider = Color(.separator)

    // MARK: - Category Colors (for future news feature)
    static let categoryColors: [String: Color] = [
        "politics": Color(hex: 0x3B82F6),
        "business": Color(hex: 0x10B981),
        "technology": Color(hex: 0x8B5CF6),
        "sports": Color(hex: 0xEF4444),
        "entertainment": Color(hex: 0xF59E0B),
        "health": Color(hex: 0x06B6D4),
        "science": Color(hex: 0x6366F1),
        "world": Color(hex: 0x84CC16),
        "environment": Color(hex: 0x22C55E),
        "local": Color(hex: 0xEC4899)
    ]
}

// MARK: - Color Extension
extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: alpha
        )
    }
}
