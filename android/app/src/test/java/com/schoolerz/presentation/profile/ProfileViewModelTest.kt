package com.schoolerz.presentation.profile

import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ProfileViewModelTest {

    // MARK: - ProfileState Tests

    @Test
    fun `ProfileState default values are correct`() {
        val state = ProfileState()

        assertNull(state.profile)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
        assertFalse(state.isEditing)
        assertEquals("", state.editingDisplayName)
        assertEquals("", state.editingSchoolName)
        assertEquals("", state.editingGrade)
        assertEquals("", state.editingBio)
        assertEquals("", state.editingNeighborhood)
        assertTrue(state.editingServices.isEmpty())
    }

    @Test
    fun `ProfileState loading state`() {
        val state = ProfileState(isLoading = true)

        assertTrue(state.isLoading)
        assertFalse(state.isSaving)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `ProfileState saving state`() {
        val state = ProfileState(isSaving = true)

        assertFalse(state.isLoading)
        assertTrue(state.isSaving)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `ProfileState refreshing state`() {
        val state = ProfileState(isRefreshing = true)

        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertTrue(state.isRefreshing)
    }

    @Test
    fun `ProfileState with error`() {
        val state = ProfileState(error = "Network error")

        assertEquals("Network error", state.error)
    }

    @Test
    fun `ProfileState editing state`() {
        val state = ProfileState(
            isEditing = true,
            editingDisplayName = "John Doe",
            editingSchoolName = "Lincoln High",
            editingGrade = "10th",
            editingBio = "I love helping!",
            editingNeighborhood = "Downtown",
            editingServices = setOf("Tutoring", "Dog Walking")
        )

        assertTrue(state.isEditing)
        assertEquals("John Doe", state.editingDisplayName)
        assertEquals("Lincoln High", state.editingSchoolName)
        assertEquals("10th", state.editingGrade)
        assertEquals("I love helping!", state.editingBio)
        assertEquals("Downtown", state.editingNeighborhood)
        assertEquals(2, state.editingServices.size)
        assertTrue(state.editingServices.contains("Tutoring"))
        assertTrue(state.editingServices.contains("Dog Walking"))
    }

    @Test
    fun `ProfileState with profile`() {
        val profile = Profile(
            id = "user-123",
            displayName = "Jane Smith",
            schoolName = "Roosevelt High",
            grade = "11th",
            bio = "Experienced tutor",
            neighborhood = "Uptown",
            services = listOf("Tutoring", "Babysitting"),
            verificationStatus = VerificationStatus.VERIFIED
        )

        val state = ProfileState(profile = profile)

        assertNotNull(state.profile)
        assertEquals("Jane Smith", state.profile?.displayName)
        assertEquals("Roosevelt High", state.profile?.schoolName)
        assertEquals("11th", state.profile?.grade)
        assertEquals(VerificationStatus.VERIFIED, state.profile?.verificationStatus)
    }

    @Test
    fun `ProfileState copy updates correctly`() {
        val initialState = ProfileState()

        val loadingState = initialState.copy(isLoading = true)
        assertTrue(loadingState.isLoading)
        assertFalse(initialState.isLoading) // Original unchanged

        val errorState = loadingState.copy(isLoading = false, error = "Failed")
        assertFalse(errorState.isLoading)
        assertEquals("Failed", errorState.error)
    }

    @Test
    fun `ProfileState services set operations`() {
        val services = mutableSetOf("Tutoring")
        services.add("Dog Walking")
        services.add("Tutoring") // Duplicate

        val state = ProfileState(editingServices = services)
        assertEquals(2, state.editingServices.size)
    }

    @Test
    fun `ProfileState toggle service add`() {
        val services = mutableSetOf("Tutoring")
        services.add("Dog Walking")

        assertEquals(2, services.size)
        assertTrue(services.contains("Dog Walking"))
    }

    @Test
    fun `ProfileState toggle service remove`() {
        val services = mutableSetOf("Tutoring", "Dog Walking")
        services.remove("Dog Walking")

        assertEquals(1, services.size)
        assertFalse(services.contains("Dog Walking"))
    }

    // MARK: - Profile Model Extended Tests

    @Test
    fun `Profile initials with two names`() {
        val profile = Profile(displayName = "John Smith")
        assertEquals("JS", profile.initials)
    }

    @Test
    fun `Profile initials with single name`() {
        val profile = Profile(displayName = "John")
        assertEquals("J", profile.initials)
    }

    @Test
    fun `Profile initials with three names takes first two`() {
        val profile = Profile(displayName = "John Paul Smith")
        assertEquals("JP", profile.initials)
    }

    @Test
    fun `Profile isVerified when VERIFIED`() {
        val profile = Profile(
            displayName = "John",
            verificationStatus = VerificationStatus.VERIFIED
        )
        assertTrue(profile.isVerified)
    }

    @Test
    fun `Profile isVerified false when UNVERIFIED`() {
        val profile = Profile(
            displayName = "John",
            verificationStatus = VerificationStatus.UNVERIFIED
        )
        assertFalse(profile.isVerified)
    }

    @Test
    fun `Profile isVerified false when EMAIL_PENDING`() {
        val profile = Profile(
            displayName = "John",
            verificationStatus = VerificationStatus.EMAIL_PENDING
        )
        assertFalse(profile.isVerified)
    }

    @Test
    fun `Profile AVAILABLE_SERVICES contains expected services`() {
        val services = Profile.AVAILABLE_SERVICES

        assertTrue(services.contains("Lawn Care"))
        assertTrue(services.contains("Babysitting"))
        assertTrue(services.contains("Dog Walking"))
        assertTrue(services.contains("Tutoring"))
        assertTrue(services.contains("Tech Help"))
        assertTrue(services.contains("Errands"))
        assertTrue(services.contains("Other"))
        assertEquals(7, services.size)
    }

    @Test
    fun `Profile with optional fields null`() {
        val profile = Profile(displayName = "John")

        assertNull(profile.schoolName)
        assertNull(profile.grade)
        assertNull(profile.bio)
        assertNull(profile.neighborhood)
        assertNull(profile.avatarPath)
        assertTrue(profile.services.isEmpty())
    }

    @Test
    fun `Profile with all fields set`() {
        val createdAt = Date()
        val profile = Profile(
            id = "user-123",
            displayName = "John Doe",
            schoolName = "Lincoln High",
            grade = "10th",
            bio = "I love helping!",
            neighborhood = "Downtown",
            services = listOf("Tutoring", "Dog Walking"),
            avatarPath = "/images/avatar.jpg",
            verificationStatus = VerificationStatus.VERIFIED,
            createdAt = createdAt
        )

        assertEquals("user-123", profile.id)
        assertEquals("John Doe", profile.displayName)
        assertEquals("Lincoln High", profile.schoolName)
        assertEquals("10th", profile.grade)
        assertEquals("I love helping!", profile.bio)
        assertEquals("Downtown", profile.neighborhood)
        assertEquals(2, profile.services.size)
        assertEquals("/images/avatar.jpg", profile.avatarPath)
        assertEquals(VerificationStatus.VERIFIED, profile.verificationStatus)
        assertEquals(createdAt, profile.createdAt)
    }

    // MARK: - VerificationStatus Tests

    @Test
    fun `VerificationStatus has correct values`() {
        assertEquals(3, VerificationStatus.values().size)
        assertTrue(VerificationStatus.values().contains(VerificationStatus.UNVERIFIED))
        assertTrue(VerificationStatus.values().contains(VerificationStatus.EMAIL_PENDING))
        assertTrue(VerificationStatus.values().contains(VerificationStatus.VERIFIED))
    }

    @Test
    fun `VerificationStatus ordinal values`() {
        assertEquals(0, VerificationStatus.UNVERIFIED.ordinal)
        assertEquals(1, VerificationStatus.EMAIL_PENDING.ordinal)
        assertEquals(2, VerificationStatus.VERIFIED.ordinal)
    }
}
