package com.birthdayreminder.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests verifying Material 3 Saffron Design System tokens, color schemes,
 * and typography specifications for Milestone 1.
 */
class ThemeTokensTest {
    @Test
    fun `core seeds match design specifications`() {
        assertEquals(Color(0xFFC98A12), SaffronSeed)
        assertEquals(Color(0xFF8F4B3D), RosewoodSeed)
        assertEquals(Color(0xFF844788), PlumSeed)
    }

    @Test
    fun `error colors are strictly isolated to M3 red values`() {
        // Light error tokens
        assertEquals(Color(0xFFBA1A1A), SaffronLightError)
        assertEquals(Color(0xFFFFFFFF), SaffronLightOnError)
        assertEquals(Color(0xFFFFDAD6), SaffronLightErrorContainer)
        assertEquals(Color(0xFF410002), SaffronLightOnErrorContainer)

        // Dark error tokens
        assertEquals(Color(0xFFFFB4AB), SaffronDarkError)
        assertEquals(Color(0xFF690005), SaffronDarkOnError)
        assertEquals(Color(0xFF93000A), SaffronDarkErrorContainer)
        assertEquals(Color(0xFFFFDAD6), SaffronDarkOnErrorContainer)
    }

    @Test
    fun `saffron light color scheme contains all 36 tokens`() {
        assertEquals(Color(0xFF815600), SaffronLightColorScheme.primary)
        assertEquals(Color(0xFFFFFFFF), SaffronLightColorScheme.onPrimary)
        assertEquals(Color(0xFFFFDDB1), SaffronLightColorScheme.primaryContainer)
        assertEquals(Color(0xFF291800), SaffronLightColorScheme.onPrimaryContainer)
        assertEquals(Color(0xFFFFBA49), SaffronLightColorScheme.inversePrimary)

        assertEquals(Color(0xFF8F4B3D), SaffronLightColorScheme.secondary)
        assertEquals(Color(0xFFFFFFFF), SaffronLightColorScheme.onSecondary)
        assertEquals(Color(0xFFFFDAD3), SaffronLightColorScheme.secondaryContainer)
        assertEquals(Color(0xFF3A0A02), SaffronLightColorScheme.onSecondaryContainer)

        assertEquals(Color(0xFF844788), SaffronLightColorScheme.tertiary)
        assertEquals(Color(0xFFFFFFFF), SaffronLightColorScheme.onTertiary)
        assertEquals(Color(0xFFFFD6FB), SaffronLightColorScheme.tertiaryContainer)
        assertEquals(Color(0xFF36003D), SaffronLightColorScheme.onTertiaryContainer)

        assertEquals(Color(0xFFFFF8F3), SaffronLightColorScheme.background)
        assertEquals(Color(0xFF291800), SaffronLightColorScheme.onBackground)
        assertEquals(Color(0xFFFFF8F3), SaffronLightColorScheme.surface)
        assertEquals(Color(0xFF291800), SaffronLightColorScheme.onSurface)
        assertEquals(Color(0xFFFFDDB1), SaffronLightColorScheme.surfaceVariant)
        assertEquals(Color(0xFF614000), SaffronLightColorScheme.onSurfaceVariant)
        assertEquals(Color(0xFF815600), SaffronLightColorScheme.surfaceTint)

        assertEquals(Color(0xFFFFF8F3), SaffronLightColorScheme.surfaceBright)
        assertEquals(Color(0xFFFFD395), SaffronLightColorScheme.surfaceDim)
        assertEquals(Color(0xFFFFFFFF), SaffronLightColorScheme.surfaceContainerLowest)
        assertEquals(Color(0xFFFFF1E3), SaffronLightColorScheme.surfaceContainerLow)
        assertEquals(Color(0xFFFFEBD3), SaffronLightColorScheme.surfaceContainer)
        assertEquals(Color(0xFFFFE4C2), SaffronLightColorScheme.surfaceContainerHigh)
        assertEquals(Color(0xFFFFDDB1), SaffronLightColorScheme.surfaceContainerHighest)

        assertEquals(Color(0xFFBA1A1A), SaffronLightColorScheme.error)
        assertEquals(Color(0xFFFFFFFF), SaffronLightColorScheme.onError)
        assertEquals(Color(0xFFFFDAD6), SaffronLightColorScheme.errorContainer)
        assertEquals(Color(0xFF410002), SaffronLightColorScheme.onErrorContainer)

        assertEquals(Color(0xFFA16C00), SaffronLightColorScheme.outline)
        assertEquals(Color(0xFFE9C08A), SaffronLightColorScheme.outlineVariant)
        assertEquals(Color(0xFF442B00), SaffronLightColorScheme.inverseSurface)
        assertEquals(Color(0xFFFFEEDB), SaffronLightColorScheme.inverseOnSurface)
        assertEquals(Color(0xFF000000), SaffronLightColorScheme.scrim)
    }

    @Test
    fun `saffron dark color scheme contains all 36 tokens`() {
        assertEquals(Color(0xFFFFBA49), SaffronDarkColorScheme.primary)
        assertEquals(Color(0xFF442B00), SaffronDarkColorScheme.onPrimary)
        assertEquals(Color(0xFF614000), SaffronDarkColorScheme.primaryContainer)
        assertEquals(Color(0xFFFFDDB1), SaffronDarkColorScheme.onPrimaryContainer)
        assertEquals(Color(0xFF815600), SaffronDarkColorScheme.inversePrimary)

        assertEquals(Color(0xFFFFB4A5), SaffronDarkColorScheme.secondary)
        assertEquals(Color(0xFF3A0A02), SaffronDarkColorScheme.onSecondary)
        assertEquals(Color(0xFF6C2D22), SaffronDarkColorScheme.secondaryContainer)
        assertEquals(Color(0xFFFFDAD3), SaffronDarkColorScheme.onSecondaryContainer)

        assertEquals(Color(0xFFF7AEF7), SaffronDarkColorScheme.tertiary)
        assertEquals(Color(0xFF36003D), SaffronDarkColorScheme.onTertiary)
        assertEquals(Color(0xFF6A2F6E), SaffronDarkColorScheme.tertiaryContainer)
        assertEquals(Color(0xFFFFD6FB), SaffronDarkColorScheme.onTertiaryContainer)

        assertEquals(Color(0xFF1E1100), SaffronDarkColorScheme.background)
        assertEquals(Color(0xFFFFDDB1), SaffronDarkColorScheme.onBackground)
        assertEquals(Color(0xFF1E1100), SaffronDarkColorScheme.surface)
        assertEquals(Color(0xFFFFDDB1), SaffronDarkColorScheme.onSurface)
        assertEquals(Color(0xFF614000), SaffronDarkColorScheme.surfaceVariant)
        assertEquals(Color(0xFFFFBA49), SaffronDarkColorScheme.onSurfaceVariant)
        assertEquals(Color(0xFFFFBA49), SaffronDarkColorScheme.surfaceTint)

        assertEquals(Color(0xFF503400), SaffronDarkColorScheme.surfaceBright)
        assertEquals(Color(0xFF1E1100), SaffronDarkColorScheme.surfaceDim)
        assertEquals(Color(0xFF170C00), SaffronDarkColorScheme.surfaceContainerLowest)
        assertEquals(Color(0xFF291800), SaffronDarkColorScheme.surfaceContainerLow)
        assertEquals(Color(0xFF2E1C00), SaffronDarkColorScheme.surfaceContainer)
        assertEquals(Color(0xFF3C2600), SaffronDarkColorScheme.surfaceContainerHigh)
        assertEquals(Color(0xFF4A2F00), SaffronDarkColorScheme.surfaceContainerHighest)

        assertEquals(Color(0xFFFFB4AB), SaffronDarkColorScheme.error)
        assertEquals(Color(0xFF690005), SaffronDarkColorScheme.onError)
        assertEquals(Color(0xFF93000A), SaffronDarkColorScheme.errorContainer)
        assertEquals(Color(0xFFFFDAD6), SaffronDarkColorScheme.onErrorContainer)

        assertEquals(Color(0xFFC28408), SaffronDarkColorScheme.outline)
        assertEquals(Color(0xFF614000), SaffronDarkColorScheme.outlineVariant)
        assertEquals(Color(0xFFFFDDB1), SaffronDarkColorScheme.inverseSurface)
        assertEquals(Color(0xFF442B00), SaffronDarkColorScheme.inverseOnSurface)
        assertEquals(Color(0xFF000000), SaffronDarkColorScheme.scrim)
    }

    @Test
    fun `all 15 typography styles match saffron specifications`() {
        // Display Large
        assertEquals(Fraunces, Typography.displayLarge.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.displayLarge.fontWeight)
        assertEquals(45.sp, Typography.displayLarge.fontSize)
        assertEquals(52.sp, Typography.displayLarge.lineHeight)
        assertEquals((-0.02).em, Typography.displayLarge.letterSpacing)

        // Display Medium
        assertEquals(Fraunces, Typography.displayMedium.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.displayMedium.fontWeight)
        assertEquals(36.sp, Typography.displayMedium.fontSize)
        assertEquals(44.sp, Typography.displayMedium.lineHeight)
        assertEquals((-0.03).em, Typography.displayMedium.letterSpacing)

        // Display Small
        assertEquals(Fraunces, Typography.displaySmall.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.displaySmall.fontWeight)
        assertEquals(30.sp, Typography.displaySmall.fontSize)
        assertEquals(36.sp, Typography.displaySmall.lineHeight)
        assertEquals((-0.02).em, Typography.displaySmall.letterSpacing)

        // Headline Large
        assertEquals(Fraunces, Typography.headlineLarge.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineLarge.fontWeight)
        assertEquals(28.sp, Typography.headlineLarge.fontSize)
        assertEquals(34.sp, Typography.headlineLarge.lineHeight)
        assertEquals((-0.02).em, Typography.headlineLarge.letterSpacing)

        // Headline Medium
        assertEquals(Fraunces, Typography.headlineMedium.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineMedium.fontWeight)
        assertEquals(24.sp, Typography.headlineMedium.fontSize)
        assertEquals(30.sp, Typography.headlineMedium.lineHeight)
        assertEquals((-0.01).em, Typography.headlineMedium.letterSpacing)

        // Headline Small
        assertEquals(Fraunces, Typography.headlineSmall.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineSmall.fontWeight)
        assertEquals(20.sp, Typography.headlineSmall.fontSize)
        assertEquals(26.sp, Typography.headlineSmall.lineHeight)
        assertEquals((-0.02).em, Typography.headlineSmall.letterSpacing)

        // Title Large
        assertEquals(Figtree, Typography.titleLarge.fontFamily)
        assertEquals(FontWeight.Bold, Typography.titleLarge.fontWeight)
        assertEquals(22.sp, Typography.titleLarge.fontSize)
        assertEquals(28.sp, Typography.titleLarge.lineHeight)
        assertEquals(0.0.em, Typography.titleLarge.letterSpacing)

        // Title Medium
        assertEquals(Figtree, Typography.titleMedium.fontFamily)
        assertEquals(FontWeight.Bold, Typography.titleMedium.fontWeight)
        assertEquals(16.sp, Typography.titleMedium.fontSize)
        assertEquals(24.sp, Typography.titleMedium.lineHeight)
        assertEquals(0.01.em, Typography.titleMedium.letterSpacing)

        // Title Small
        assertEquals(Figtree, Typography.titleSmall.fontFamily)
        assertEquals(FontWeight.Bold, Typography.titleSmall.fontWeight)
        assertEquals(14.sp, Typography.titleSmall.fontSize)
        assertEquals(20.sp, Typography.titleSmall.lineHeight)
        assertEquals(0.01.em, Typography.titleSmall.letterSpacing)

        // Body Large
        assertEquals(Figtree, Typography.bodyLarge.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
        assertEquals(16.sp, Typography.bodyLarge.fontSize)
        assertEquals(24.sp, Typography.bodyLarge.lineHeight)
        assertEquals(0.01.em, Typography.bodyLarge.letterSpacing)

        // Body Medium
        assertEquals(Figtree, Typography.bodyMedium.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodyMedium.fontWeight)
        assertEquals(14.sp, Typography.bodyMedium.fontSize)
        assertEquals(20.sp, Typography.bodyMedium.lineHeight)
        assertEquals(0.01.em, Typography.bodyMedium.letterSpacing)

        // Body Small
        assertEquals(Figtree, Typography.bodySmall.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodySmall.fontWeight)
        assertEquals(12.sp, Typography.bodySmall.fontSize)
        assertEquals(16.sp, Typography.bodySmall.lineHeight)
        assertEquals(0.0.em, Typography.bodySmall.letterSpacing)

        // Label Large
        assertEquals(Figtree, Typography.labelLarge.fontFamily)
        assertEquals(FontWeight.Bold, Typography.labelLarge.fontWeight)
        assertEquals(14.sp, Typography.labelLarge.fontSize)
        assertEquals(20.sp, Typography.labelLarge.lineHeight)
        assertEquals(0.01.em, Typography.labelLarge.letterSpacing)

        // Label Medium
        assertEquals(Figtree, Typography.labelMedium.fontFamily)
        assertEquals(FontWeight.Bold, Typography.labelMedium.fontWeight)
        assertEquals(12.sp, Typography.labelMedium.fontSize)
        assertEquals(16.sp, Typography.labelMedium.lineHeight)
        assertEquals(0.01.em, Typography.labelMedium.letterSpacing)

        // Label Small
        assertEquals(Figtree, Typography.labelSmall.fontFamily)
        assertEquals(FontWeight.Bold, Typography.labelSmall.fontWeight)
        assertEquals(11.sp, Typography.labelSmall.fontSize)
        assertEquals(16.sp, Typography.labelSmall.lineHeight)
        assertEquals(0.03.em, Typography.labelSmall.letterSpacing)
    }

    @Test
    fun `font family instances are initialized`() {
        assertNotNull(Fraunces)
        assertNotNull(Figtree)
    }
}
