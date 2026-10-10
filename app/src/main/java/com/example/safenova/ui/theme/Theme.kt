package com.example.safenova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SafeNovaDarkColorScheme = darkColorScheme(
    primary = SafePurple,
    onPrimary = Color.White,
    primaryContainer = SafeDarkPurple,
    onPrimaryContainer = Purple80,
    secondary = Pink80,
    onSecondary = Color.Black,
    tertiary = SafeRose,
    error = AlertRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = CinnamonSurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = Color(0xFF9CA3AF)
)

@Composable
fun SAFENOVATheme(
    darkTheme: Boolean = true, // Force Dark Mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SafeNovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
