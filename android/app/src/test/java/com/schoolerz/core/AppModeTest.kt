package com.schoolerz.core

import org.junit.Assert.*
import org.junit.Test

class AppModeTest {

    @Test
    fun `AppMode has MOCK value`() {
        assertEquals(AppMode.MOCK, AppMode.valueOf("MOCK"))
    }

    @Test
    fun `AppMode has FIREBASE value`() {
        assertEquals(AppMode.FIREBASE, AppMode.valueOf("FIREBASE"))
    }

    @Test
    fun `AppMode has exactly two values`() {
        assertEquals(2, AppMode.values().size)
    }

    @Test
    fun `AppMode values contain MOCK and FIREBASE`() {
        val values = AppMode.values()
        assertTrue(values.contains(AppMode.MOCK))
        assertTrue(values.contains(AppMode.FIREBASE))
    }

    @Test
    fun `AppMode ordinals are correct`() {
        assertEquals(0, AppMode.MOCK.ordinal)
        assertEquals(1, AppMode.FIREBASE.ordinal)
    }

    @Test
    fun `AppMode name returns correct string`() {
        assertEquals("MOCK", AppMode.MOCK.name)
        assertEquals("FIREBASE", AppMode.FIREBASE.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AppMode valueOf throws for invalid value`() {
        AppMode.valueOf("INVALID")
    }

    @Test
    fun `AppMode equality works`() {
        assertEquals(AppMode.MOCK, AppMode.MOCK)
        assertNotEquals(AppMode.MOCK, AppMode.FIREBASE)
    }

    @Test
    fun `AppMode can be used in when expression`() {
        val mode = AppMode.MOCK
        val result = when (mode) {
            AppMode.MOCK -> "mock"
            AppMode.FIREBASE -> "firebase"
        }
        assertEquals("mock", result)
    }

    @Test
    fun `AppMode can be used in set`() {
        val modes = setOf(AppMode.MOCK, AppMode.FIREBASE, AppMode.MOCK)
        assertEquals(2, modes.size)
    }
}
