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

@Composable
fun SetPinScreen(onPinSet: () -> Unit, secureStorage: SecureStorage) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pin, confirmPin) {
        when {
            step == 1 && pin.length == 4 -> step = 2
            step == 2 && confirmPin.length == 4 -> {
                if (pin == confirmPin) {
                    secureStorage.storePIN(pin)
                    onPinSet()
                } else {
                    error = "PINs don't match"
                    pin = ""; confirmPin = ""; step = 1
                }
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
        Text(if (step == 1) "Create a PIN" else "Confirm PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Tokens.Spacing.s))
        Text("Use a 4-digit PIN to secure your app", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(Tokens.Spacing.m))
        OutlinedTextField(
            value = if (step == 1) pin else confirmPin,
            onValueChange = { v ->
                if (v.length <= 4 && v.all { it.isDigit() }) {
                    if (step == 1) pin = v else confirmPin = v
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.width(150.dp),
            isError = error != null
        )
        error?.let {
            Spacer(Modifier.height(Tokens.Spacing.s))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
