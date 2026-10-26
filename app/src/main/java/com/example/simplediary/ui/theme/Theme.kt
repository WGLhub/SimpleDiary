package com.example.simplediary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.simplediary.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF426A5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4EBD9),
    onPrimaryContainer = Color(0xFF082019),
    secondary = Color(0xFF6B5D3E),
    secondaryContainer = Color(0xFFF4E2BB),
    background = Color(0xFFFFFDF7),
    surface = Color(0xFFFFFDF7),
    surfaceVariant = Color(0xFFE6EAE6),
    onSurface = Color(0xFF181D1B),
    onSurfaceVariant = Color(0xFF414845),
    outline = Color(0xFF717976),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8D5C0),
    onPrimary = Color(0xFF123328),
    primaryContainer = Color(0xFF2A4A3E),
    onPrimaryContainer = Color(0xFFC4EBD9),
    secondary = Color(0xFFD9C69D),
    secondaryContainer = Color(0xFF51462C),
    background = Color(0xFF111412),
    surface = Color(0xFF111412),
    surfaceVariant = Color(0xFF404944),
    onSurface = Color(0xFFE0E3DF),
    onSurfaceVariant = Color(0xFFC0C9C3),
    outline = Color(0xFF8A938E),
    error = Color(0xFFFFB4AB),
)

@Composable
fun SimpleDiaryTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}

