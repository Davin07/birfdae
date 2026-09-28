package com.birthdayreminder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val SaffronLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = SaffronLightPrimary,
        onPrimary = SaffronLightOnPrimary,
        primaryContainer = SaffronLightPrimaryContainer,
        onPrimaryContainer = SaffronLightOnPrimaryContainer,
        inversePrimary = SaffronLightInversePrimary,
        secondary = SaffronLightSecondary,
        onSecondary = SaffronLightOnSecondary,
        secondaryContainer = SaffronLightSecondaryContainer,
        onSecondaryContainer = SaffronLightOnSecondaryContainer,
        tertiary = SaffronLightTertiary,
        onTertiary = SaffronLightOnTertiary,
        tertiaryContainer = SaffronLightTertiaryContainer,
        onTertiaryContainer = SaffronLightOnTertiaryContainer,
        background = SaffronLightBackground,
        onBackground = SaffronLightOnBackground,
        surface = SaffronLightSurface,
        onSurface = SaffronLightOnSurface,
        surfaceVariant = SaffronLightSurfaceVariant,
        onSurfaceVariant = SaffronLightOnSurfaceVariant,
        surfaceTint = SaffronLightSurfaceTint,
        surfaceBright = SaffronLightSurfaceBright,
        surfaceDim = SaffronLightSurfaceDim,
        surfaceContainerLowest = SaffronLightSurfaceContainerLowest,
        surfaceContainerLow = SaffronLightSurfaceContainerLow,
        surfaceContainer = SaffronLightSurfaceContainer,
        surfaceContainerHigh = SaffronLightSurfaceContainerHigh,
        surfaceContainerHighest = SaffronLightSurfaceContainerHighest,
        error = SaffronLightError,
        onError = SaffronLightOnError,
        errorContainer = SaffronLightErrorContainer,
        onErrorContainer = SaffronLightOnErrorContainer,
        outline = SaffronLightOutline,
        outlineVariant = SaffronLightOutlineVariant,
        inverseSurface = SaffronLightInverseSurface,
        inverseOnSurface = SaffronLightInverseOnSurface,
        scrim = SaffronLightScrim,
    )

val SaffronDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = SaffronDarkPrimary,
        onPrimary = SaffronDarkOnPrimary,
        primaryContainer = SaffronDarkPrimaryContainer,
        onPrimaryContainer = SaffronDarkOnPrimaryContainer,
        inversePrimary = SaffronDarkInversePrimary,
        secondary = SaffronDarkSecondary,
        onSecondary = SaffronDarkOnSecondary,
        secondaryContainer = SaffronDarkSecondaryContainer,
        onSecondaryContainer = SaffronDarkOnSecondaryContainer,
        tertiary = SaffronDarkTertiary,
        onTertiary = SaffronDarkOnTertiary,
        tertiaryContainer = SaffronDarkTertiaryContainer,
        onTertiaryContainer = SaffronDarkOnTertiaryContainer,
        background = SaffronDarkBackground,
        onBackground = SaffronDarkOnBackground,
        surface = SaffronDarkSurface,
        onSurface = SaffronDarkOnSurface,
        surfaceVariant = SaffronDarkSurfaceVariant,
        onSurfaceVariant = SaffronDarkOnSurfaceVariant,
        surfaceTint = SaffronDarkSurfaceTint,
        surfaceBright = SaffronDarkSurfaceBright,
        surfaceDim = SaffronDarkSurfaceDim,
        surfaceContainerLowest = SaffronDarkSurfaceContainerLowest,
        surfaceContainerLow = SaffronDarkSurfaceContainerLow,
        surfaceContainer = SaffronDarkSurfaceContainer,
        surfaceContainerHigh = SaffronDarkSurfaceContainerHigh,
        surfaceContainerHighest = SaffronDarkSurfaceContainerHighest,
        error = SaffronDarkError,
        onError = SaffronDarkOnError,
        errorContainer = SaffronDarkErrorContainer,
        onErrorContainer = SaffronDarkOnErrorContainer,
        outline = SaffronDarkOutline,
        outlineVariant = SaffronDarkOutlineVariant,
        inverseSurface = SaffronDarkInverseSurface,
        inverseOnSurface = SaffronDarkInverseOnSurface,
        scrim = SaffronDarkScrim,
    )

// Aliases for compatibility
val LightColorScheme = SaffronLightColorScheme
val DarkColorScheme = SaffronDarkColorScheme

/**
 * Material 3 Saffron Theme for the Birthday Reminder app.
 * Provides Light and Dark color schemes derived from seed #C98A12.
 * Preserves status bar and navigation bar coloring and window insets controller setup.
 */
@Composable
fun BirthdayReminderAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                val baseScheme = if (darkTheme) SaffronDarkColorScheme else SaffronLightColorScheme

                baseScheme.copy(
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
            darkTheme -> SaffronDarkColorScheme
            else -> SaffronLightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()

                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Overdue has its own warm-urgent pair, keyed off darkTheme rather than a
    // ColorScheme equality check, which would break the moment either scheme
    // gained a role.
    CompositionLocalProvider(
        LocalOverdueColors provides if (darkTheme) SaffronDarkOverdue else SaffronLightOverdue,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
