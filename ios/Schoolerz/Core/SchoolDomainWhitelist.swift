import Foundation

/// School domain whitelist for email verification
struct SchoolDomainWhitelist {
    /// List of approved school email domains
    static let approvedDomains: Set<String> = [
        "student.edu",
        "k12.ca.us",
        "lausd.net",
        "sfusd.edu",
        "myschool.edu",
        "highschool.org"
    ]

    /// Validates if an email address has an approved school domain
    /// - Parameter email: The email address to validate
    /// - Returns: True if the email domain is in the approved list
    static func isApprovedDomain(_ email: String) -> Bool {
        guard let domain = extractDomain(from: email) else {
            return false
        }
        return approvedDomains.contains(domain)
    }

    /// Validates basic email format
    /// - Parameter email: The email address to validate
    /// - Returns: True if the email format is valid
    static func isValidEmailFormat(_ email: String) -> Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: email)
    }

    /// Extracts the domain from an email address
    /// - Parameter email: The email address
    /// - Returns: The domain portion (e.g., "school.edu") or nil if invalid
    private static func extractDomain(from email: String) -> String? {
        let components = email.lowercased().components(separatedBy: "@")
        guard components.count == 2 else { return nil }
        return components[1]
    }

    /// Gets a user-friendly domain suggestion message
    /// - Returns: A string listing approved domains
    static func getDomainSuggestionMessage() -> String {
        let sortedDomains = approvedDomains.sorted()
        return "Approved school domains:\n" + sortedDomains.map { "• @\($0)" }.joined(separator: "\n")
    }
}
