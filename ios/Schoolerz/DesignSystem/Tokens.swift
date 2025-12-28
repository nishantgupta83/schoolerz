import SwiftUI

/// Design tokens for consistent spacing, radii, and animation durations
enum Tokens {
    // MARK: - Spacing
    enum Spacing {
        static let xs: CGFloat = 4
        static let s: CGFloat = 8
        static let m: CGFloat = 16
        static let l: CGFloat = 24
        static let xl: CGFloat = 32
    }

    // MARK: - Corner Radius
    enum Radius {
        static let small: CGFloat = 8
        static let medium: CGFloat = 14
        static let large: CGFloat = 20
    }

    // MARK: - Animation Durations
    enum Duration {
        static let fast: Double = 0.15
        static let normal: Double = 0.25
    }

    // MARK: - Constants
    enum Feed {
        static let fetchLimit = 200
    }
}
