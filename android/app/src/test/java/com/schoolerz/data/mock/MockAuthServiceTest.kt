package com.schoolerz.data.mock

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockAuthServiceTest {

    private lateinit var authService: MockAuthService

    @Before
    fun setup() {
        authService = MockAuthService()
    }

    @Test
    fun `currentUserId returns non-empty string`() {
        val userId = authService.currentUserId()
        assertTrue(userId.isNotEmpty())
    }

    @Test
    fun `currentUserId starts with mock-user prefix`() {
        val userId = authService.currentUserId()
        assertTrue(userId.startsWith("mock-user-"))
    }

    @Test
    fun `currentUserId returns consistent value for same instance`() {
        val userId1 = authService.currentUserId()
        val userId2 = authService.currentUserId()
        assertEquals(userId1, userId2)
    }

    @Test
    fun `currentUserName returns Current User`() {
        val userName = authService.currentUserName()
        assertEquals("Current User", userName)
    }

    @Test
    fun `different instances have different user IDs`() {
        val authService1 = MockAuthService()
        val authService2 = MockAuthService()

        val userId1 = authService1.currentUserId()
        val userId2 = authService2.currentUserId()

        // Different instances should have different IDs (due to UUID)
        assertNotEquals(userId1, userId2)
    }

    @Test
    fun `currentUserId has correct format`() {
        val userId = authService.currentUserId()
        // Format: mock-user-XXXXXXXX (8 chars from UUID)
        assertTrue(userId.matches(Regex("mock-user-[a-f0-9]{8}")))
    }
}
