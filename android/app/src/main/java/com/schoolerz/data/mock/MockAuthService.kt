package com.schoolerz.data.mock

import com.schoolerz.domain.repository.AuthService
import java.util.UUID

/**
 * Mock implementation of AuthService for local development.
 * Uses a consistent device-based ID for the session.
 */
class MockAuthService : AuthService {

    // Generate a consistent ID for this app session
    private val sessionUserId: String = "mock-user-${UUID.randomUUID().toString().take(8)}"

    override fun currentUserId(): String = sessionUserId

    override fun currentUserName(): String = "Current User"
}
