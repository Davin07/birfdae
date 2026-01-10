package com.birthdayreminder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = LuminaPrimary,
        secondary = LuminaAccent,
        tertiary = LuminaPrimaryDark,
        background = LuminaBackgroundDark,
        surface = LuminaSurfaceDark,
        onBackground = LuminaOnBackgroundDark,
        onSurface = LuminaOnSurfaceDark,
        surfaceVariant = LuminaCardGlass,
        onSurfaceVariant = LuminaOnSurfaceDark,
        outline = Color.White.copy(alpha = 0.1f),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LuminaLightPrimary,
        secondary = LuminaAccent,
        tertiary = LuminaLightTertiary,
        background = LuminaLightBackground,
        surface = LuminaLightSurface,
        onBackground = LuminaLightOnSurface,
        onSurface = LuminaLightOnSurface,
        surfaceVariant = LuminaLightGlass,
        onSurfaceVariant = LuminaLightOnSurface,
        outline = Color.White.copy(alpha = 0.6f), // Glass border color
    )

/**
 * Lumina Theme implementation for the Birthday Reminder app.
 * Provides a modern, glassmorphic design based on project references.
 */
@Composable
fun BirthdayReminderAppTheme(
    darkTheme: Boolean = true, // Forced Dark Mode default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

                if (darkTheme) {
                    DarkColorScheme.copy(
                        primary = dynamic.primary,
                        onPrimary = dynamic.onPrimary,
                        primaryContainer = dynamic.primaryContainer,
                        onPrimaryContainer = dynamic.onPrimaryContainer,
                        secondary = dynamic.secondary,
                        onSecondary = dynamic.onSecondary,
                        secondaryContainer = dynamic.secondaryContainer,
                        onSecondaryContainer = dynamic.onSecondaryContainer,
                        tertiary = dynamic.tertiary,
                        onTertiary = dynamic.onTertiary,
                        tertiaryContainer = dynamic.tertiaryContainer,
                        onTertiaryContainer = dynamic.onTertiaryContainer,
                    )
                } else {
                    LightColorScheme.copy(
                        primary = dynamic.primary,
                        onPrimary = dynamic.onPrimary,
                        primaryContainer = dynamic.primaryContainer,
                        onPrimaryContainer = dynamic.onPrimaryContainer,
                        secondary = dynamic.secondary,
                        onSecondary = dynamic.onSecondary,
                        secondaryContainer = dynamic.secondaryContainer,
                        onSecondaryContainer = dynamic.onSecondaryContainer,
                        tertiary = dynamic.tertiary,
                        onTertiary = dynamic.onTertiary,
                        tertiaryContainer = dynamic.tertiaryContainer,
                        onTertiaryContainer = dynamic.onTertiaryContainer,
                    )
                }
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
