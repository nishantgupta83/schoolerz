package com.schoolerz

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.schoolerz.core.AppMode
import com.schoolerz.core.AppModeHolder
import com.schoolerz.core.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SchoolerzApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create notification channel for local notifications
        NotificationHelper.createNotificationChannel(this)

        // Determine app mode based on config file presence
        AppModeHolder.initialize(this)

        // Only initialize Firebase if in firebase mode
        if (AppModeHolder.current == AppMode.FIREBASE) {
            val app = FirebaseApp.initializeApp(this)
            if (app != null) {
                signInAnonymously()
            } else {
                Log.w(TAG, "Firebase init returned null, running in mock mode")
            }
        } else {
            Log.d(TAG, "Running in mock mode - Firebase not initialized")
        }
    }

    private fun signInAnonymously() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "Signed in anonymously: ${result.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Anonymous sign-in failed", e)
                }
        }
    }

    companion object {
        private const val TAG = "SchoolerzApp"
    }
}
