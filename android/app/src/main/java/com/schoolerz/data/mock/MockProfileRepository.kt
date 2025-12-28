package com.schoolerz.data.mock

import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import com.schoolerz.domain.repository.ProfileRepository
import kotlinx.coroutines.delay
import java.util.Date

/**
 * Mock implementation of ProfileRepository for local development
 */
class MockProfileRepository : ProfileRepository {
    private var profile: Profile? = Profile(
        id = "mock-user-123",
        displayName = "Alex Johnson",
        schoolName = "Lincoln High School",
        grade = "11th Grade",
        bio = "Hey! I'm a junior who loves helping neighbors. Reliable and friendly!",
        neighborhood = "Westside",
        services = listOf("Lawn Care", "Dog Walking", "Tech Help"),
        verificationStatus = VerificationStatus.VERIFIED,
        createdAt = Date(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000) // 90 days ago
    )

    override suspend fun getCurrentProfile(): Result<Profile?> {
        delay(200)
        return Result.success(profile)
    }

    override suspend fun saveProfile(profile: Profile): Result<Unit> {
        delay(200)
        this.profile = profile
        return Result.success(Unit)
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> {
        delay(100)
        profile = profile?.copy(displayName = name)
        return Result.success(Unit)
    }

    override suspend fun updateSchoolName(name: String?): Result<Unit> {
        delay(100)
        profile = profile?.copy(schoolName = name)
        return Result.success(Unit)
    }

    override suspend fun updateAvatarPath(path: String?): Result<Unit> {
        delay(100)
        profile = profile?.copy(avatarPath = path)
        return Result.success(Unit)
    }

    override suspend fun updateVerificationStatus(status: VerificationStatus): Result<Unit> {
        delay(100)
        profile = profile?.copy(verificationStatus = status)
        return Result.success(Unit)
    }

    override suspend fun hasProfile(): Boolean {
        return profile != null
    }

    override suspend fun deleteProfile(): Result<Unit> {
        delay(100)
        profile = null
        return Result.success(Unit)
    }
}
