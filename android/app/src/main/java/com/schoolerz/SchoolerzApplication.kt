package com.schoolerz

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SchoolerzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        signInAnonymously()
    }

    private fun signInAnonymously() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // Firebase auth is already async - no need for CoroutineScope
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
