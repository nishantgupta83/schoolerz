package com.schoolerz.domain.model

import java.util.Date
import java.util.UUID

/**
 * Verification status for user profile
 */
enum class VerificationStatus {
    UNVERIFIED,
    EMAIL_PENDING,
    VERIFIED
}

/**
 * User profile for the Schoolerz marketplace
 */
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val schoolName: String? = null,
    val grade: String? = null,
    val bio: String? = null,
    val neighborhood: String? = null,
    val services: List<String> = emptyList(),
    val avatarPath: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val createdAt: Date = Date()
) {
    /**
     * Get initials from display name
     */
    val initials: String
        get() = displayName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")

    /**
     * Whether the user is verified
     */
    val isVerified: Boolean
        get() = verificationStatus == VerificationStatus.VERIFIED

    companion object {
        /**
         * Available services for teens to offer
         */
        val AVAILABLE_SERVICES = listOf(
            "Lawn Care",
            "Babysitting",
            "Dog Walking",
            "Tutoring",
            "Tech Help",
            "Errands",
            "Other"
        )
    }
}
