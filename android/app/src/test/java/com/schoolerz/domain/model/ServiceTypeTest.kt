package com.schoolerz.domain.model

import org.junit.Assert.*
import org.junit.Test

class ServiceTypeTest {

    // MARK: - Display Names

    @Test
    fun `ServiceType display names are correct`() {
        assertEquals("Dog Walking", ServiceType.DOG_WALKING.displayName)
        assertEquals("Tutoring", ServiceType.TUTORING.displayName)
        assertEquals("Babysitting", ServiceType.BABYSITTING.displayName)
        assertEquals("Lawn Care", ServiceType.LAWN_CARE.displayName)
        assertEquals("Tech Help", ServiceType.TECH_HELP.displayName)
        assertEquals("Music", ServiceType.MUSIC_LESSONS.displayName)
        assertEquals("Essay", ServiceType.ESSAY_REVIEW.displayName)
        assertEquals("Car Wash", ServiceType.CAR_WASH.displayName)
        assertEquals("Other", ServiceType.OTHER.displayName)
    }

    // MARK: - All Cases

    @Test
    fun `ServiceType has correct number of values`() {
        assertEquals(9, ServiceType.values().size)
    }

    @Test
    fun `ServiceType contains all expected values`() {
        val values = ServiceType.values()

        assertTrue(values.contains(ServiceType.DOG_WALKING))
        assertTrue(values.contains(ServiceType.TUTORING))
        assertTrue(values.contains(ServiceType.BABYSITTING))
        assertTrue(values.contains(ServiceType.LAWN_CARE))
        assertTrue(values.contains(ServiceType.TECH_HELP))
        assertTrue(values.contains(ServiceType.MUSIC_LESSONS))
        assertTrue(values.contains(ServiceType.ESSAY_REVIEW))
        assertTrue(values.contains(ServiceType.CAR_WASH))
        assertTrue(values.contains(ServiceType.OTHER))
    }

    // MARK: - Equality

    @Test
    fun `ServiceType equality works`() {
        assertEquals(ServiceType.DOG_WALKING, ServiceType.DOG_WALKING)
        assertNotEquals(ServiceType.DOG_WALKING, ServiceType.TUTORING)
    }

    // MARK: - valueOf

    @Test
    fun `ServiceType valueOf works correctly`() {
        assertEquals(ServiceType.DOG_WALKING, ServiceType.valueOf("DOG_WALKING"))
        assertEquals(ServiceType.TUTORING, ServiceType.valueOf("TUTORING"))
        assertEquals(ServiceType.BABYSITTING, ServiceType.valueOf("BABYSITTING"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ServiceType valueOf throws for invalid value`() {
        ServiceType.valueOf("INVALID")
    }

    // MARK: - Ordinal

    @Test
    fun `ServiceType ordinal is consistent`() {
        assertEquals(0, ServiceType.DOG_WALKING.ordinal)
        assertEquals(1, ServiceType.TUTORING.ordinal)
        assertEquals(2, ServiceType.BABYSITTING.ordinal)
    }

    // MARK: - Name

    @Test
    fun `ServiceType name is uppercase enum name`() {
        assertEquals("DOG_WALKING", ServiceType.DOG_WALKING.name)
        assertEquals("TUTORING", ServiceType.TUTORING.name)
        assertEquals("BABYSITTING", ServiceType.BABYSITTING.name)
    }

    // MARK: - HashCode

    @Test
    fun `ServiceType hashCode is consistent`() {
        val type1 = ServiceType.DOG_WALKING
        val type2 = ServiceType.DOG_WALKING

        assertEquals(type1.hashCode(), type2.hashCode())
    }

    @Test
    fun `ServiceType can be used in Set`() {
        val set = mutableSetOf<ServiceType>()
        set.add(ServiceType.DOG_WALKING)
        set.add(ServiceType.TUTORING)
        set.add(ServiceType.DOG_WALKING) // Duplicate

        assertEquals(2, set.size)
    }

    @Test
    fun `ServiceType can be used as Map key`() {
        val map = mutableMapOf<ServiceType, String>()
        map[ServiceType.DOG_WALKING] = "Walking dogs"
        map[ServiceType.TUTORING] = "Teaching students"

        assertEquals("Walking dogs", map[ServiceType.DOG_WALKING])
        assertEquals("Teaching students", map[ServiceType.TUTORING])
    }
}
