package com.schoolerz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.schoolerz.core.util.SecureStorage
import com.schoolerz.presentation.auth.PinLoginScreen
import com.schoolerz.presentation.auth.SetPinScreen
import com.schoolerz.presentation.navigation.AppNavigation
import com.schoolerz.presentation.theme.SchoolerzTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolerzTheme {
                var isUnlocked by remember { mutableStateOf(false) }
                val hasPIN = secureStorage.hasStoredPIN()

                when {
                    isUnlocked -> AppNavigation()
                    hasPIN -> PinLoginScreen(
                        onUnlock = { isUnlocked = true },
                        secureStorage = secureStorage
                    )
                    else -> SetPinScreen(
                        onPinSet = { isUnlocked = true },
                        secureStorage = secureStorage
                    )
                }
            }
        }
    }
}
