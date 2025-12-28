import Foundation
import FirebaseFirestore

/// Types of posts in the feed
enum PostType: String, Codable, CaseIterable {
    case offer
    case request

    var displayName: String {
        switch self {
        case .offer: return String(localized: "post_type_offer")
        case .request: return String(localized: "post_type_request")
        }
    }

    var color: SwiftUI.Color {
        switch self {
        case .offer: return Colors.offer
        case .request: return Colors.request
        }
    }
}

/// Represents a post in the feed
struct Post: Identifiable, Codable, Equatable {
    let id: String
    let type: PostType
    let authorId: String
    let authorName: String
    let neighborhood: String
    let body: String
    let likeCount: Int
    let commentCount: Int
    let createdAt: Date

    init(
        id: String = UUID().uuidString,
        type: PostType,
        authorId: String,
        authorName: String,
        neighborhood: String,
        body: String,
        likeCount: Int = 0,
        commentCount: Int = 0,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.type = type
        self.authorId = authorId
        self.authorName = authorName
        self.neighborhood = neighborhood
        self.body = body
        self.likeCount = likeCount
        self.commentCount = commentCount
        self.createdAt = createdAt
    }

    // MARK: - Firestore Serialization

    func toFirestoreDict() -> [String: Any] {
        [
            "id": id,
            "type": type.rawValue,
            "authorId": authorId,
            "authorName": authorName,
            "neighborhood": neighborhood,
            "body": body,
            "likeCount": likeCount,
            "commentCount": commentCount,
            "createdAt": Timestamp(date: createdAt)
        ]
    }

    init?(from dict: [String: Any]) {
        guard let id = dict["id"] as? String,
              let typeRaw = dict["type"] as? String,
              let type = PostType(rawValue: typeRaw),
              let authorId = dict["authorId"] as? String,
              let authorName = dict["authorName"] as? String,
              let neighborhood = dict["neighborhood"] as? String,
              let body = dict["body"] as? String else {
            return nil
        }

        self.id = id
        self.type = type
        self.authorId = authorId
        self.authorName = authorName
        self.neighborhood = neighborhood
        self.body = body
        self.likeCount = dict["likeCount"] as? Int ?? 0
        self.commentCount = dict["commentCount"] as? Int ?? 0

        if let timestamp = dict["createdAt"] as? Timestamp {
            self.createdAt = timestamp.dateValue()
        } else if let date = dict["createdAt"] as? Date {
            self.createdAt = date
        } else {
            self.createdAt = Date()
        }
    }
}

// MARK: - Time Ago Extension
extension Post {
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

import SwiftUI
