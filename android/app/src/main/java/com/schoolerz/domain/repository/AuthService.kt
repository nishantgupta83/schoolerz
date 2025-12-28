package com.schoolerz.domain.repository

/**
 * Abstraction for authentication service.
 * Allows switching between mock and Firebase implementations.
 */
interface AuthService {
    /**
     * Get the current user's ID.
     * Returns a device-generated ID in mock mode, or Firebase UID in firebase mode.
     */
    fun currentUserId(): String

    /**
     * Get the current user's display name.
     * Returns "Current User" for anonymous/mock users.
     */
    fun currentUserName(): String
}
