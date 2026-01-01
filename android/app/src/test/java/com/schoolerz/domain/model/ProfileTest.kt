package com.schoolerz.domain.model

import org.junit.Assert.*
import org.junit.Test

class ProfileTest {

    @Test
    fun `profile creation with required values`() {
        val profile = Profile(
            id = "user123",
            displayName = "John Doe"
        )

        assertEquals("user123", profile.id)
        assertEquals("John Doe", profile.displayName)
        assertNull(profile.schoolName)
        assertNull(profile.grade)
        assertNull(profile.bio)
        assertEquals(VerificationStatus.UNVERIFIED, profile.verificationStatus)
        assertTrue(profile.services.isEmpty())
    }

    @Test
    fun `profile creation with all values`() {
        val services = listOf("Tutoring", "Babysitting")
        val profile = Profile(
            id = "user456",
            displayName = "Jane Smith",
            schoolName = "Test High School",
            grade = "10th",
            bio = "Experienced tutor",
            neighborhood = "Downtown",
            services = services,
            verificationStatus = VerificationStatus.VERIFIED
        )

        assertEquals("user456", profile.id)
        assertEquals("Jane Smith", profile.displayName)
        assertEquals("Test High School", profile.schoolName)
        assertEquals("10th", profile.grade)
        assertEquals("Experienced tutor", profile.bio)
        assertEquals("Downtown", profile.neighborhood)
        assertEquals(VerificationStatus.VERIFIED, profile.verificationStatus)
        assertEquals(2, profile.services.size)
    }

    @Test
    fun `profile initials from full name`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John Doe"
        )

        assertEquals("JD", profile.initials)
    }

    @Test
    fun `profile initials from single name`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John"
        )

        assertEquals("J", profile.initials)
    }

    @Test
    fun `profile initials from three word name`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John Michael Doe"
        )

        assertEquals("JM", profile.initials) // Only takes first 2
    }

    @Test
    fun `isVerified returns true for VERIFIED status`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John",
            verificationStatus = VerificationStatus.VERIFIED
        )

        assertTrue(profile.isVerified)
    }

    @Test
    fun `isVerified returns false for UNVERIFIED status`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John",
            verificationStatus = VerificationStatus.UNVERIFIED
        )

        assertFalse(profile.isVerified)
    }

    @Test
    fun `isVerified returns false for EMAIL_PENDING status`() {
        val profile = Profile(
            id = "test-id",
            displayName = "John",
            verificationStatus = VerificationStatus.EMAIL_PENDING
        )

        assertFalse(profile.isVerified)
    }

    @Test
    fun `VerificationStatus enum values`() {
        assertEquals(3, VerificationStatus.values().size)
        assertTrue(VerificationStatus.values().contains(VerificationStatus.UNVERIFIED))
        assertTrue(VerificationStatus.values().contains(VerificationStatus.EMAIL_PENDING))
        assertTrue(VerificationStatus.values().contains(VerificationStatus.VERIFIED))
    }

    @Test
    fun `AVAILABLE_SERVICES contains expected services`() {
        val services = Profile.AVAILABLE_SERVICES

        assertEquals(7, services.size)
        assertTrue(services.contains("Lawn Care"))
        assertTrue(services.contains("Babysitting"))
        assertTrue(services.contains("Dog Walking"))
        assertTrue(services.contains("Tutoring"))
        assertTrue(services.contains("Tech Help"))
        assertTrue(services.contains("Errands"))
        assertTrue(services.contains("Other"))
    }

    @Test
    fun `profile equality`() {
        val profile1 = Profile(id = "same-id", displayName = "John")
        val profile2 = Profile(id = "same-id", displayName = "John")

        assertEquals(profile1.id, profile2.id)
        assertEquals(profile1.displayName, profile2.displayName)
    }
}
