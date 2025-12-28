package com.schoolerz.core

import android.content.Context

/**
 * Determines which backend the app uses.
 * In mock mode, Firebase is never initialized.
 */
enum class AppMode {
    MOCK,
    FIREBASE;

    companion object {
        /**
         * Determine current app mode based on whether google-services.json exists.
         * If the config file is missing, we run in mock mode.
         */
        fun determine(context: Context): AppMode {
            return try {
                // Check if google-services.json was properly processed
                // by attempting to get the Firebase app ID resource
                val resId = context.resources.getIdentifier(
                    "google_app_id",
                    "string",
                    context.packageName
                )
                if (resId != 0) FIREBASE else MOCK
            } catch (e: Exception) {
                MOCK
            }
        }
    }
}

/**
 * Singleton to hold the current app mode
 */
object AppModeHolder {
    lateinit var current: AppMode
        private set

    fun initialize(context: Context) {
        current = AppMode.determine(context)
    }

    val isInitialized: Boolean
        get() = ::current.isInitialized
}
