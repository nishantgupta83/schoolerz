package com.schoolerz.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.schoolerz.BuildConfig
import com.schoolerz.core.util.NotificationHelper
import com.schoolerz.presentation.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf("system") }
    var biometricEnabled by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember {
        mutableStateOf(NotificationHelper.areNotificationsEnabled(context))
    }

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        if (isGranted) {
            // Permission granted, show test notification
            NotificationHelper.showNotification(
                context = context,
                title = "Test Notification",
                body = "Notifications are working correctly!"
            )
        }
    }

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

            // Only show Test Notification section in debug builds
            if (BuildConfig.DEBUG) {
                Spacer(Modifier.height(Tokens.Spacing.l))
                Text("Developer Options", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Tokens.Spacing.s))

                Button(
                    onClick = {
                        // Check if permission is needed (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (NotificationHelper.areNotificationsEnabled(context)) {
                                // Permission already granted
                                NotificationHelper.showNotification(
                                    context = context,
                                    title = "Test Notification",
                                    body = "Notifications are working correctly!"
                                )
                            } else {
                                // Request permission
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // No permission needed on older Android versions
                            NotificationHelper.showNotification(
                                context = context,
                                title = "Test Notification",
                                body = "Notifications are working correctly!"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Notification")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                    Spacer(Modifier.height(Tokens.Spacing.xs))
                    Text(
                        text = "Notification permission required (Android 13+)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Tokens.Spacing.l))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Tokens.Spacing.s))
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
