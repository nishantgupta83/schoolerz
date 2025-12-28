package com.schoolerz.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Colors.Seed,
    background = Colors.Background,
    surface = Colors.Surface
)

private val DarkColorScheme = darkColorScheme(
    primary = Colors.Seed,
    background = Colors.BackgroundDark,
    surface = Colors.SurfaceDark
)

@Composable
fun SchoolerzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
