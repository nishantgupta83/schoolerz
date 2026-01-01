package com.schoolerz.data.mock

import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockProfileRepositoryTest {

    private lateinit var repository: MockProfileRepository

    @Before
    fun setup() {
        repository = MockProfileRepository()
    }

    @Test
    fun `getCurrentProfile returns success with profile`() = runTest {
        val result = repository.getCurrentProfile()

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `getCurrentProfile returns profile with mock data`() = runTest {
        val result = repository.getCurrentProfile()
        val profile = result.getOrNull()

        assertNotNull(profile)
        assertEquals("Alex Johnson", profile?.displayName)
        assertEquals("Lincoln High School", profile?.schoolName)
        assertEquals("11th Grade", profile?.grade)
    }

    @Test
    fun `hasProfile returns true initially`() = runTest {
        val hasProfile = repository.hasProfile()
        assertTrue(hasProfile)
    }

    @Test
    fun `saveProfile updates the profile`() = runTest {
        val newProfile = Profile(
            id = "new-user",
            displayName = "New User",
            schoolName = "New School"
        )

        val result = repository.saveProfile(newProfile)
        assertTrue(result.isSuccess)

        val fetchResult = repository.getCurrentProfile()
        val savedProfile = fetchResult.getOrNull()

        assertEquals("New User", savedProfile?.displayName)
        assertEquals("New School", savedProfile?.schoolName)
    }

    @Test
    fun `updateDisplayName updates only name`() = runTest {
        val result = repository.updateDisplayName("Updated Name")
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals("Updated Name", profile?.displayName)
        assertEquals("Lincoln High School", profile?.schoolName) // Others unchanged
    }

    @Test
    fun `updateSchoolName updates only school`() = runTest {
        val result = repository.updateSchoolName("New High School")
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals("New High School", profile?.schoolName)
        assertEquals("Alex Johnson", profile?.displayName) // Others unchanged
    }

    @Test
    fun `updateSchoolName handles null`() = runTest {
        val result = repository.updateSchoolName(null)
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertNull(profile?.schoolName)
    }

    @Test
    fun `updateAvatarPath updates path`() = runTest {
        val result = repository.updateAvatarPath("/images/avatar.jpg")
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals("/images/avatar.jpg", profile?.avatarPath)
    }

    @Test
    fun `updateAvatarPath handles null`() = runTest {
        // First set a path
        repository.updateAvatarPath("/images/avatar.jpg")
        // Then clear it
        val result = repository.updateAvatarPath(null)
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertNull(profile?.avatarPath)
    }

    @Test
    fun `updateVerificationStatus updates status`() = runTest {
        val result = repository.updateVerificationStatus(VerificationStatus.EMAIL_PENDING)
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals(VerificationStatus.EMAIL_PENDING, profile?.verificationStatus)
    }

    @Test
    fun `updateVerificationStatus to UNVERIFIED`() = runTest {
        val result = repository.updateVerificationStatus(VerificationStatus.UNVERIFIED)
        assertTrue(result.isSuccess)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals(VerificationStatus.UNVERIFIED, profile?.verificationStatus)
    }

    @Test
    fun `deleteProfile removes profile`() = runTest {
        val result = repository.deleteProfile()
        assertTrue(result.isSuccess)

        val hasProfile = repository.hasProfile()
        assertFalse(hasProfile)
    }

    @Test
    fun `getCurrentProfile returns null after delete`() = runTest {
        repository.deleteProfile()

        val result = repository.getCurrentProfile()
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `saveProfile after delete works`() = runTest {
        repository.deleteProfile()

        val newProfile = Profile(
            id = "restored-user",
            displayName = "Restored User"
        )
        repository.saveProfile(newProfile)

        val hasProfile = repository.hasProfile()
        assertTrue(hasProfile)

        val profile = repository.getCurrentProfile().getOrNull()
        assertEquals("Restored User", profile?.displayName)
    }

    @Test
    fun `initial profile has services`() = runTest {
        val profile = repository.getCurrentProfile().getOrNull()

        assertNotNull(profile?.services)
        assertTrue(profile?.services?.isNotEmpty() == true)
        assertTrue(profile?.services?.contains("Lawn Care") == true)
    }

    @Test
    fun `initial profile is verified`() = runTest {
        val profile = repository.getCurrentProfile().getOrNull()

        assertEquals(VerificationStatus.VERIFIED, profile?.verificationStatus)
        assertTrue(profile?.isVerified == true)
    }

    @Test
    fun `initial profile has createdAt date`() = runTest {
        val profile = repository.getCurrentProfile().getOrNull()

        assertNotNull(profile?.createdAt)
    }

    @Test
    fun `multiple updates are preserved`() = runTest {
        repository.updateDisplayName("Name 1")
        repository.updateSchoolName("School 1")
        repository.updateVerificationStatus(VerificationStatus.EMAIL_PENDING)

        val profile = repository.getCurrentProfile().getOrNull()

        assertEquals("Name 1", profile?.displayName)
        assertEquals("School 1", profile?.schoolName)
        assertEquals(VerificationStatus.EMAIL_PENDING, profile?.verificationStatus)
    }

    @Test
    fun `new repository instance has same initial data`() = runTest {
        val repo1 = MockProfileRepository()
        val repo2 = MockProfileRepository()

        val profile1 = repo1.getCurrentProfile().getOrNull()
        val profile2 = repo2.getCurrentProfile().getOrNull()

        assertEquals(profile1?.displayName, profile2?.displayName)
        assertEquals(profile1?.schoolName, profile2?.schoolName)
    }

    @Test
    fun `profile bio is preserved`() = runTest {
        val profile = repository.getCurrentProfile().getOrNull()

        assertNotNull(profile?.bio)
        assertTrue(profile?.bio?.isNotEmpty() == true)
    }

    @Test
    fun `profile neighborhood is preserved`() = runTest {
        val profile = repository.getCurrentProfile().getOrNull()

        assertEquals("Westside", profile?.neighborhood)
    }
}
