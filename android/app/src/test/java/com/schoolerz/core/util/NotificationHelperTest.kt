package com.schoolerz.core.util

import org.junit.Assert.*
import org.junit.Test

class NotificationHelperTest {

    @Test
    fun `CHANNEL_ID has correct value`() {
        assertEquals("schoolerz_default", NotificationHelper.CHANNEL_ID)
    }

    @Test
    fun `CHANNEL_NAME has correct value`() {
        assertEquals("Schoolerz Notifications", NotificationHelper.CHANNEL_NAME)
    }

    @Test
    fun `CHANNEL_DESCRIPTION has correct value`() {
        assertEquals("General notifications for Schoolerz app", NotificationHelper.CHANNEL_DESCRIPTION)
    }

    @Test
    fun `CHANNEL_ID is not empty`() {
        assertTrue(NotificationHelper.CHANNEL_ID.isNotEmpty())
    }

    @Test
    fun `CHANNEL_NAME is not empty`() {
        assertTrue(NotificationHelper.CHANNEL_NAME.isNotEmpty())
    }

    @Test
    fun `CHANNEL_DESCRIPTION is not empty`() {
        assertTrue(NotificationHelper.CHANNEL_DESCRIPTION.isNotEmpty())
    }

    @Test
    fun `CHANNEL_ID is valid identifier format`() {
        // Channel IDs should be lowercase with underscores
        assertTrue(NotificationHelper.CHANNEL_ID.matches(Regex("[a-z_]+")))
    }

    @Test
    fun `constants are consistent between calls`() {
        val id1 = NotificationHelper.CHANNEL_ID
        val id2 = NotificationHelper.CHANNEL_ID
        assertEquals(id1, id2)
        assertSame(id1, id2)
    }
}
