import Foundation
import FirebaseFirestore

/// Represents a comment on a post
struct Comment: Identifiable, Codable, Equatable {
    let id: String
    let postId: String
    let authorId: String
    let authorName: String
    let text: String
    let createdAt: Date

    init(
        id: String = UUID().uuidString,
        postId: String,
        authorId: String,
        authorName: String,
        text: String,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.postId = postId
        self.authorId = authorId
        self.authorName = authorName
        self.text = text
        self.createdAt = createdAt
    }

    // MARK: - Firestore Serialization

    func toFirestoreDict() -> [String: Any] {
        [
            "id": id,
            "postId": postId,
            "authorId": authorId,
            "authorName": authorName,
            "text": text,
            "createdAt": Timestamp(date: createdAt)
        ]
    }

    init?(from dict: [String: Any]) {
        guard let id = dict["id"] as? String,
              let postId = dict["postId"] as? String,
              let authorId = dict["authorId"] as? String,
              let authorName = dict["authorName"] as? String,
              let text = dict["text"] as? String else {
            return nil
        }

        self.id = id
        self.postId = postId
        self.authorId = authorId
        self.authorName = authorName
        self.text = text

        if let timestamp = dict["createdAt"] as? Timestamp {
            self.createdAt = timestamp.dateValue()
        } else if let date = dict["createdAt"] as? Date {
            self.createdAt = date
        } else {
            self.createdAt = Date()
        }
    }
}

// MARK: - Helper Extensions
extension Comment {
    var timeAgo: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: createdAt, relativeTo: Date())
    }

    var authorInitials: String {
        authorName
            .split(separator: " ")
            .prefix(2)
            .compactMap { $0.first }
            .map { String($0).uppercased() }
            .joined()
    }
}
