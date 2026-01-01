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
    val createdAt: Date = Date(),

    // Pricing fields
    val rateAmount: Double? = null,
    val rateMax: Double? = null,
    val rateType: RateType = RateType.NEGOTIABLE,

    // Availability
    val availableDays: List<String> = emptyList(),
    val availableTimeStart: String? = null,
    val availableTimeEnd: String? = null,

    // Service & Experience
    val serviceType: ServiceType? = null,
    val experienceLevel: ExperienceLevel? = null,
    val skillTags: List<String> = emptyList()
) {
    val authorInitials: String
        get() = authorName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")

    /** Formatted price string */
    val formattedPrice: String
        get() = rateType.formatPrice(rateAmount, rateMax)

    /** Formatted availability string */
    val formattedAvailability: String?
        get() {
            if (availableDays.isEmpty()) return null
            val days = availableDays.joinToString(", ") { it.take(3) }
            return if (availableTimeStart != null && availableTimeEnd != null) {
                "$days $availableTimeStart - $availableTimeEnd"
            } else {
                days
            }
        }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type.name.lowercase(),
        "authorId" to authorId,
        "authorName" to authorName,
        "neighborhood" to neighborhood,
        "body" to body,
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "createdAt" to createdAt,
        "rateAmount" to rateAmount,
        "rateMax" to rateMax,
        "rateType" to rateType.firestoreValue,
        "availableDays" to availableDays,
        "availableTimeStart" to availableTimeStart,
        "availableTimeEnd" to availableTimeEnd,
        "serviceType" to serviceType?.firestoreValue,
        "experienceLevel" to experienceLevel?.firestoreValue,
        "skillTags" to skillTags
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
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
                    createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                    rateAmount = (map["rateAmount"] as? Number)?.toDouble(),
                    rateMax = (map["rateMax"] as? Number)?.toDouble(),
                    rateType = RateType.fromFirestore(map["rateType"] as? String),
                    availableDays = (map["availableDays"] as? List<String>) ?: emptyList(),
                    availableTimeStart = map["availableTimeStart"] as? String,
                    availableTimeEnd = map["availableTimeEnd"] as? String,
                    serviceType = ServiceType.entries.find { it.firestoreValue == map["serviceType"] },
                    experienceLevel = ExperienceLevel.fromFirestore(map["experienceLevel"] as? String),
                    skillTags = (map["skillTags"] as? List<String>) ?: emptyList()
                )
            } catch (e: Exception) { null }
        }
    }
}
