package com.schoolerz.domain.repository

import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus

/**
 * Repository interface for user profile operations
 */
interface ProfileRepository {
    /**
     * Get the current user's profile
     */
    suspend fun getCurrentProfile(): Result<Profile?>

    /**
     * Save or update user profile
     */
    suspend fun saveProfile(profile: Profile): Result<Unit>

    /**
     * Update display name
     */
    suspend fun updateDisplayName(name: String): Result<Unit>

    /**
     * Update school name
     */
    suspend fun updateSchoolName(name: String?): Result<Unit>

    /**
     * Update avatar path
     */
    suspend fun updateAvatarPath(path: String?): Result<Unit>

    /**
     * Update verification status
     */
    suspend fun updateVerificationStatus(status: VerificationStatus): Result<Unit>

    /**
     * Check if profile exists
     */
    suspend fun hasProfile(): Boolean

    /**
     * Delete profile
     */
    suspend fun deleteProfile(): Result<Unit>
}
