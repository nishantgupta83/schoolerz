package com.schoolerz.domain.model

import java.util.Date
import java.util.UUID

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val createdAt: Date = Date()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "postId" to postId,
        "authorId" to authorId,
        "authorName" to authorName,
        "text" to text,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any>): Comment? {
            return try {
                Comment(
                    id = map["id"] as String,
                    postId = map["postId"] as String,
                    authorId = map["authorId"] as String,
                    authorName = map["authorName"] as String,
                    text = map["text"] as String,
                    createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                )
            } catch (e: Exception) { null }
        }
    }
}
