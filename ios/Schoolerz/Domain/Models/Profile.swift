import Foundation

/// Verification status for user profiles
enum VerificationStatus: String, Codable, CaseIterable {
    case unverified
    case emailPending
    case verified
}

/// Services that teens can offer
enum ServiceType: String, Codable, CaseIterable {
    case lawnCare = "Lawn Care"
    case babysitting = "Babysitting"
    case dogWalking = "Dog Walking"
    case tutoring = "Tutoring"
    case techHelp = "Tech Help"
    case errands = "Errands"
    case other = "Other"

    var displayName: String {
        rawValue
    }

    var icon: String {
        switch self {
        case .lawnCare: return "leaf.fill"
        case .babysitting: return "figure.and.child.holdinghands"
        case .dogWalking: return "pawprint.fill"
        case .tutoring: return "book.fill"
        case .techHelp: return "laptopcomputer"
        case .errands: return "cart.fill"
        case .other: return "star.fill"
        }
    }
}

/// User profile model
struct Profile: Identifiable, Codable, Equatable {
    let id: String
    var displayName: String
    var schoolName: String?
    var gradeLevel: String?
    var avatarPath: String?
    var email: String?
    var emailVerified: Bool
    var verificationStatus: VerificationStatus
    var services: [ServiceType]
    var bio: String?
    var neighborhood: String?
    let createdAt: Date

    init(
        id: String = UUID().uuidString,
        displayName: String,
        schoolName: String? = nil,
        gradeLevel: String? = nil,
        avatarPath: String? = nil,
        email: String? = nil,
        emailVerified: Bool = false,
        verificationStatus: VerificationStatus = .unverified,
        services: [ServiceType] = [],
        bio: String? = nil,
        neighborhood: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.displayName = displayName
        self.schoolName = schoolName
        self.gradeLevel = gradeLevel
        self.avatarPath = avatarPath
        self.email = email
        self.emailVerified = emailVerified
        self.verificationStatus = verificationStatus
        self.services = services
        self.bio = bio
        self.neighborhood = neighborhood
        self.createdAt = createdAt
    }

    /// Whether the profile is verified
    var isVerified: Bool {
        verificationStatus == .verified
    }

    /// Whether the profile has a verified email
    var hasVerifiedEmail: Bool {
        email != nil && emailVerified
    }

    /// Initials for avatar display
    var initials: String {
        let components = displayName.components(separatedBy: " ")
        let initials = components.compactMap { $0.first }.prefix(2)
        return String(initials).uppercased()
    }
}
