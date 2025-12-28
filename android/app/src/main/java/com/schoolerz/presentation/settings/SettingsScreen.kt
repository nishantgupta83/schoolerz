package com.schoolerz.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.schoolerz.presentation.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var themeMode by remember { mutableStateOf("system") }
    var biometricEnabled by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(Tokens.Spacing.m)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Tokens.Spacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)) {
                FilterChip(selected = themeMode == "light", onClick = { themeMode = "light" }, label = { Text("Light") })
                FilterChip(selected = themeMode == "dark", onClick = { themeMode = "dark" }, label = { Text("Dark") })
                FilterChip(selected = themeMode == "system", onClick = { themeMode = "system" }, label = { Text("System") })
            }

            Spacer(Modifier.height(Tokens.Spacing.l))
            Text("Security", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Tokens.Spacing.s))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Use Biometrics")
                Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
            }

            Spacer(Modifier.height(Tokens.Spacing.l))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Tokens.Spacing.s))
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
