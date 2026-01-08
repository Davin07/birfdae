package com.birthdayreminder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LuminaPrimary,
    secondary = LuminaAccent,
    tertiary = LuminaPrimaryDark,
    background = LuminaBackgroundDark,
    surface = LuminaSurfaceDark,
    onBackground = LuminaOnBackgroundDark,
    onSurface = LuminaOnSurfaceDark,
    surfaceVariant = LuminaCardGlass,
    onSurfaceVariant = LuminaOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = LuminaPrimaryDark,
    secondary = LuminaAccent,
    tertiary = LuminaPrimary,
    background = LuminaBackgroundLight,
    surface = LuminaSurfaceLight,
    onBackground = LuminaOnBackgroundLight,
    onSurface = LuminaOnSurfaceLight,
    surfaceVariant = LuminaSurfaceLight,
    onSurfaceVariant = LuminaOnSurfaceLight
)

/**
 * Lumina Theme implementation for the Birthday Reminder app.
 * Provides a modern, glassmorphic design based on project references.
 */
@Composable
fun BirthdayReminderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to prioritize Lumina branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}