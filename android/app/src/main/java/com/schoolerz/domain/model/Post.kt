package com.schoolerz.domain.model

import java.util.Date
import java.util.UUID

enum class PostType { OFFER, REQUEST }

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val type: PostType,
    val authorId: String,
    val authorName: String,
    val neighborhood: String,
    val body: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Date = Date()
) {
    val authorInitials: String
        get() = authorName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "type" to type.name.lowercase(),
        "authorId" to authorId,
        "authorName" to authorName,
        "neighborhood" to neighborhood,
        "body" to body,
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any>): Post? {
            return try {
                Post(
                    id = map["id"] as String,
                    type = PostType.valueOf((map["type"] as String).uppercase()),
                    authorId = map["authorId"] as String,
                    authorName = map["authorName"] as String,
                    neighborhood = map["neighborhood"] as String,
                    body = map["body"] as String,
                    likeCount = (map["likeCount"] as? Long)?.toInt() ?: 0,
                    commentCount = (map["commentCount"] as? Long)?.toInt() ?: 0,
                    createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                )
            } catch (e: Exception) { null }
        }
    }
}
