package com.example.safenova.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SafeNovaCinnamonDarkScheme = darkColorScheme(
    primary = SafePurple,
    onPrimary = Color.White,
    primaryContainer = SafeDarkPurple,
    onPrimaryContainer = Purple80,
    secondary = SafePurpleGlow,
    onSecondary = CinnamonObsidian,
    secondaryContainer = CinnamonCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = SafeRose,
    onTertiary = Color.White,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedLight,
    onErrorContainer = Color(0xFFFFB4AB),
    background = CinnamonObsidian,
    onBackground = TextPrimary,
    surface = CinnamonCard,
    onSurface = TextPrimary,
    surfaceVariant = CinnamonCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = CinnamonBorder
)

@Composable
fun SAFENOVATheme(
    darkTheme: Boolean = true, // Strictly enforce single dark purple-black theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = CinnamonPanel.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = CinnamonPanel.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SafeNovaCinnamonDarkScheme,
        typography = Typography,
        content = content
    )
}
