package com.schoolerz.domain.model

import org.junit.Assert.*
import org.junit.Test

class RateTypeTest {

    // MARK: - Format Price Tests

    @Test
    fun `HOURLY formats single amount correctly`() {
        val formatted = RateType.HOURLY.formatPrice(15.0)
        assertEquals("$15/hour", formatted)
    }

    @Test
    fun `HOURLY formats range correctly`() {
        val formatted = RateType.HOURLY.formatPrice(15.0, 25.0)
        assertEquals("$15-25/hour", formatted)
    }

    @Test
    fun `PER_TASK formats single amount correctly`() {
        val formatted = RateType.PER_TASK.formatPrice(30.0)
        assertEquals("$30/task", formatted)
    }

    @Test
    fun `PER_TASK formats range correctly`() {
        val formatted = RateType.PER_TASK.formatPrice(30.0, 50.0)
        assertEquals("$30-50/task", formatted)
    }

    @Test
    fun `NEGOTIABLE returns Negotiable`() {
        val formatted = RateType.NEGOTIABLE.formatPrice(null)
        assertEquals("Negotiable", formatted)
    }

    @Test
    fun `NEGOTIABLE ignores amount`() {
        val formatted = RateType.NEGOTIABLE.formatPrice(20.0)
        assertEquals("Negotiable", formatted)
    }

    @Test
    fun `null amount returns Price TBD`() {
        val formatted = RateType.HOURLY.formatPrice(null)
        assertEquals("Price TBD", formatted)
    }

    @Test
    fun `zero amount formats correctly`() {
        val formatted = RateType.HOURLY.formatPrice(0.0)
        assertEquals("$0/hour", formatted)
    }

    // MARK: - Enum Values

    @Test
    fun `RateType has correct number of values`() {
        assertEquals(3, RateType.values().size)
    }

    @Test
    fun `RateType contains expected values`() {
        val values = RateType.values()
        assertTrue(values.contains(RateType.HOURLY))
        assertTrue(values.contains(RateType.PER_TASK))
        assertTrue(values.contains(RateType.NEGOTIABLE))
    }

    // MARK: - Display Suffix

    @Test
    fun `RateType display suffix is correct`() {
        assertEquals("/hour", RateType.HOURLY.displaySuffix)
        assertEquals("/task", RateType.PER_TASK.displaySuffix)
        assertEquals("", RateType.NEGOTIABLE.displaySuffix)
    }

    @Test
    fun `RateType firestore values are correct`() {
        assertEquals("hourly", RateType.HOURLY.firestoreValue)
        assertEquals("per_task", RateType.PER_TASK.firestoreValue)
        assertEquals("negotiable", RateType.NEGOTIABLE.firestoreValue)
    }

    @Test
    fun `RateType fromFirestore returns correct type`() {
        assertEquals(RateType.HOURLY, RateType.fromFirestore("hourly"))
        assertEquals(RateType.PER_TASK, RateType.fromFirestore("per_task"))
        assertEquals(RateType.NEGOTIABLE, RateType.fromFirestore("negotiable"))
    }

    @Test
    fun `RateType fromFirestore returns NEGOTIABLE for unknown value`() {
        assertEquals(RateType.NEGOTIABLE, RateType.fromFirestore("unknown"))
        assertEquals(RateType.NEGOTIABLE, RateType.fromFirestore(null))
    }
}

class ExperienceLevelTest {

    @Test
    fun `ExperienceLevel display names are correct`() {
        assertEquals("Beginner", ExperienceLevel.BEGINNER.displayName)
        assertEquals("Intermediate", ExperienceLevel.INTERMEDIATE.displayName)
        assertEquals("Experienced", ExperienceLevel.EXPERIENCED.displayName)
    }

    @Test
    fun `ExperienceLevel firestore values are correct`() {
        assertEquals("beginner", ExperienceLevel.BEGINNER.firestoreValue)
        assertEquals("intermediate", ExperienceLevel.INTERMEDIATE.firestoreValue)
        assertEquals("experienced", ExperienceLevel.EXPERIENCED.firestoreValue)
    }

    @Test
    fun `ExperienceLevel fromFirestore returns correct level`() {
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromFirestore("beginner"))
        assertEquals(ExperienceLevel.INTERMEDIATE, ExperienceLevel.fromFirestore("intermediate"))
        assertEquals(ExperienceLevel.EXPERIENCED, ExperienceLevel.fromFirestore("experienced"))
    }

    @Test
    fun `ExperienceLevel fromFirestore returns null for unknown value`() {
        assertNull(ExperienceLevel.fromFirestore("unknown"))
        assertNull(ExperienceLevel.fromFirestore(null))
    }

    @Test
    fun `ExperienceLevel has correct number of values`() {
        assertEquals(3, ExperienceLevel.values().size)
    }

    @Test
    fun `ExperienceLevel contains expected values`() {
        val values = ExperienceLevel.values()
        assertTrue(values.contains(ExperienceLevel.BEGINNER))
        assertTrue(values.contains(ExperienceLevel.INTERMEDIATE))
        assertTrue(values.contains(ExperienceLevel.EXPERIENCED))
    }
}
