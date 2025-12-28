package com.schoolerz.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.schoolerz.core.util.SecureStorage
import com.schoolerz.presentation.theme.Colors
import com.schoolerz.presentation.theme.Tokens
import kotlinx.coroutines.delay

@Composable
fun PinLoginScreen(onUnlock: () -> Unit, secureStorage: SecureStorage) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLockedOut by remember { mutableStateOf(secureStorage.isLockedOut) }
    var remainingSeconds by remember { mutableStateOf(0L) }

    // Update lockout countdown
    LaunchedEffect(isLockedOut) {
        while (isLockedOut) {
            val remaining = (secureStorage.lockoutUntil - System.currentTimeMillis()) / 1000
            if (remaining <= 0) {
                isLockedOut = false
                remainingSeconds = 0
            } else {
                remainingSeconds = remaining
                delay(1000)
            }
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4 && !isLockedOut) {
            if (secureStorage.verifyPIN(pin)) {
                secureStorage.resetFailedAttempts()
                onUnlock()
            } else {
                secureStorage.recordFailedAttempt()
                isLockedOut = secureStorage.isLockedOut
                error = if (isLockedOut) {
                    "Too many attempts. Try again in 5 minutes."
                } else {
                    val remaining = 5 - secureStorage.failedAttempts
                    "Incorrect PIN. $remaining attempts remaining."
                }
                pin = ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Tokens.Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(60.dp), tint = Colors.Seed)
        Spacer(Modifier.height(Tokens.Spacing.l))
        Text("Enter PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Tokens.Spacing.m))

        if (isLockedOut) {
            Text(
                "Locked out for ${remainingSeconds}s",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(Tokens.Spacing.m))
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { if (!isLockedOut && it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.width(150.dp),
            isError = error != null,
            enabled = !isLockedOut
        )
        error?.let {
            Spacer(Modifier.height(Tokens.Spacing.s))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
