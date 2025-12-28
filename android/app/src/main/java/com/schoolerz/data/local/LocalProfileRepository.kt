package com.schoolerz.data.local

import android.content.Context
import android.content.SharedPreferences
import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import com.schoolerz.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalProfileRepository @Inject constructor(
    @ApplicationContext context: Context
) : ProfileRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override suspend fun getCurrentProfile(): Result<Profile?> = withContext(Dispatchers.IO) {
        delay(100) // Simulate small delay
        try {
            val json = prefs.getString(KEY_PROFILE, null)
                ?: return@withContext Result.success(null)

            val obj = JSONObject(json)

            // Parse services array
            val servicesArray = obj.optJSONArray("services")
            val services = mutableListOf<String>()
            if (servicesArray != null) {
                for (i in 0 until servicesArray.length()) {
                    services.add(servicesArray.getString(i))
                }
            }

            val profile = Profile(
                id = obj.getString("id"),
                displayName = obj.getString("displayName"),
                schoolName = obj.optString("schoolName").takeIf { it.isNotEmpty() },
                grade = obj.optString("grade").takeIf { it.isNotEmpty() },
                bio = obj.optString("bio").takeIf { it.isNotEmpty() },
                neighborhood = obj.optString("neighborhood").takeIf { it.isNotEmpty() },
                services = services,
                avatarPath = obj.optString("avatarPath").takeIf { it.isNotEmpty() },
                verificationStatus = VerificationStatus.valueOf(
                    obj.optString("verificationStatus", "UNVERIFIED")
                ),
                createdAt = Date(obj.getLong("createdAt"))
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(profile: Profile): Result<Unit> = withContext(Dispatchers.IO) {
        delay(100)
        try {
            val json = JSONObject().apply {
                put("id", profile.id)
                put("displayName", profile.displayName)
                profile.schoolName?.let { put("schoolName", it) }
                profile.grade?.let { put("grade", it) }
                profile.bio?.let { put("bio", it) }
                profile.neighborhood?.let { put("neighborhood", it) }

                // Save services as JSON array
                val servicesArray = org.json.JSONArray()
                profile.services.forEach { servicesArray.put(it) }
                put("services", servicesArray)

                profile.avatarPath?.let { put("avatarPath", it) }
                put("verificationStatus", profile.verificationStatus.name)
                put("createdAt", profile.createdAt.time)
            }
            prefs.edit().putString(KEY_PROFILE, json.toString()).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getCurrentProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("No profile found"))
            saveProfile(profile.copy(displayName = name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSchoolName(name: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getCurrentProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("No profile found"))
            saveProfile(profile.copy(schoolName = name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatarPath(path: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getCurrentProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("No profile found"))
            saveProfile(profile.copy(avatarPath = path))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVerificationStatus(status: VerificationStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getCurrentProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("No profile found"))
            saveProfile(profile.copy(verificationStatus = status))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasProfile(): Boolean = withContext(Dispatchers.IO) {
        prefs.contains(KEY_PROFILE)
    }

    override suspend fun deleteProfile(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            prefs.edit().remove(KEY_PROFILE).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val PREFS_NAME = "schoolerz_profile"
        private const val KEY_PROFILE = "profile"
    }
}
