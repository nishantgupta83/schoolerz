package com.schoolerz.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Rate type for pricing services
 */
enum class RateType(val firestoreValue: String, val displaySuffix: String) {
    HOURLY("hourly", "/hour"),
    PER_TASK("per_task", "/task"),
    NEGOTIABLE("negotiable", "");

    /**
     * Format price display based on rate type
     */
    fun formatPrice(amount: Double?, max: Double? = null): String {
        if (this == NEGOTIABLE || amount == null) {
            return if (this == NEGOTIABLE) "Negotiable" else "Price TBD"
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }

        val amountStr = formatter.format(amount)

        return if (max != null && max > amount) {
            val maxStr = formatter.format(max).replace("$", "")
            "$amountStr-$maxStr$displaySuffix"
        } else {
            "$amountStr$displaySuffix"
        }
    }

    companion object {
        fun fromFirestore(value: String?): RateType {
            return entries.find { it.firestoreValue == value } ?: NEGOTIABLE
        }
    }
}

/**
 * Experience level for service providers
 */
enum class ExperienceLevel(val firestoreValue: String, val displayName: String) {
    BEGINNER("beginner", "Beginner"),
    INTERMEDIATE("intermediate", "Intermediate"),
    EXPERIENCED("experienced", "Experienced");

    companion object {
        fun fromFirestore(value: String?): ExperienceLevel? {
            return entries.find { it.firestoreValue == value }
        }
    }
}
