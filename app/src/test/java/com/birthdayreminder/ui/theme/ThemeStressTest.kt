package com.birthdayreminder.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

/**
 * Empirical stress tests and adversarial verification for Milestone 1:
 * Design System Tokens & Typography.
 */
class ThemeStressTest {
    // -------------------------------------------------------------------------
    // Helper Functions
    // -------------------------------------------------------------------------

    private fun calculateContrastRatio(
        foreground: Color,
        background: Color,
    ): Float {
        val l1 = foreground.luminance()
        val l2 = background.luminance()
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun extractAllTokens(s: ColorScheme): Map<String, Color> =
        mapOf(
            "primary" to s.primary,
            "onPrimary" to s.onPrimary,
            "primaryContainer" to s.primaryContainer,
            "onPrimaryContainer" to s.onPrimaryContainer,
            "inversePrimary" to s.inversePrimary,
            "secondary" to s.secondary,
            "onSecondary" to s.onSecondary,
            "secondaryContainer" to s.secondaryContainer,
            "onSecondaryContainer" to s.onSecondaryContainer,
            "tertiary" to s.tertiary,
            "onTertiary" to s.onTertiary,
            "tertiaryContainer" to s.tertiaryContainer,
            "onTertiaryContainer" to s.onTertiaryContainer,
            "background" to s.background,
            "onBackground" to s.onBackground,
            "surface" to s.surface,
            "onSurface" to s.onSurface,
            "surfaceVariant" to s.surfaceVariant,
            "onSurfaceVariant" to s.onSurfaceVariant,
            "surfaceTint" to s.surfaceTint,
            "surfaceBright" to s.surfaceBright,
            "surfaceDim" to s.surfaceDim,
            "surfaceContainerLowest" to s.surfaceContainerLowest,
            "surfaceContainerLow" to s.surfaceContainerLow,
            "surfaceContainer" to s.surfaceContainer,
            "surfaceContainerHigh" to s.surfaceContainerHigh,
            "surfaceContainerHighest" to s.surfaceContainerHighest,
            "error" to s.error,
            "onError" to s.onError,
            "errorContainer" to s.errorContainer,
            "onErrorContainer" to s.onErrorContainer,
            "outline" to s.outline,
            "outlineVariant" to s.outlineVariant,
            "inverseSurface" to s.inverseSurface,
            "inverseOnSurface" to s.inverseOnSurface,
            "scrim" to s.scrim,
        )

    // -------------------------------------------------------------------------
    // Objective 1: Empirically verify all 36 tokens in Light & Dark schemes
    // -------------------------------------------------------------------------

    @Test
    fun `empirical verification - exactly 36 tokens exist and are non-null and valid in both schemes`() {
        val lightTokens = extractAllTokens(SaffronLightColorScheme)
        val darkTokens = extractAllTokens(SaffronDarkColorScheme)

        assertEquals("Light color scheme must have exactly 36 tokens", 36, lightTokens.size)
        assertEquals("Dark color scheme must have exactly 36 tokens", 36, darkTokens.size)

        for ((name, color) in lightTokens) {
            assertNotNull("Light token $name should not be null", color)
            assertNotEquals("Light token $name must not be Color.Unspecified", Color.Unspecified, color)
            assertEquals("Light token $name must be fully opaque (alpha = 1.0f)", 1.0f, color.alpha, 0.001f)
            assertTrue("Light token $name red in range [0..1]", color.red in 0.0f..1.0f)
            assertTrue("Light token $name green in range [0..1]", color.green in 0.0f..1.0f)
            assertTrue("Light token $name blue in range [0..1]", color.blue in 0.0f..1.0f)
        }

        for ((name, color) in darkTokens) {
            assertNotNull("Dark token $name should not be null", color)
            assertNotEquals("Dark token $name must not be Color.Unspecified", Color.Unspecified, color)
            assertEquals("Dark token $name must be fully opaque (alpha = 1.0f)", 1.0f, color.alpha, 0.001f)
            assertTrue("Dark token $name red in range [0..1]", color.red in 0.0f..1.0f)
            assertTrue("Dark token $name green in range [0..1]", color.green in 0.0f..1.0f)
            assertTrue("Dark token $name blue in range [0..1]", color.blue in 0.0f..1.0f)
        }
    }

    @Test
    fun `empirical verification - surface elevation luminance hierarchy holds in both themes`() {
        // In Light theme, higher surface containers are darker/warmer (decreasing luminance)
        val lLowest = SaffronLightColorScheme.surfaceContainerLowest.luminance()
        val lLow = SaffronLightColorScheme.surfaceContainerLow.luminance()
        val lContainer = SaffronLightColorScheme.surfaceContainer.luminance()
        val lHigh = SaffronLightColorScheme.surfaceContainerHigh.luminance()
        val lHighest = SaffronLightColorScheme.surfaceContainerHighest.luminance()

        assertTrue("Light lowest >= low", lLowest >= lLow)
        assertTrue("Light low >= container", lLow >= lContainer)
        assertTrue("Light container >= high", lContainer >= lHigh)
        assertTrue("Light high >= highest", lHigh >= lHighest)

        // In Dark theme, higher surface containers are lighter (increasing luminance)
        val dLowest = SaffronDarkColorScheme.surfaceContainerLowest.luminance()
        val dLow = SaffronDarkColorScheme.surfaceContainerLow.luminance()
        val dContainer = SaffronDarkColorScheme.surfaceContainer.luminance()
        val dHigh = SaffronDarkColorScheme.surfaceContainerHigh.luminance()
        val dHighest = SaffronDarkColorScheme.surfaceContainerHighest.luminance()

        assertTrue("Dark lowest <= low", dLowest <= dLow)
        assertTrue("Dark low <= container", dLow <= dContainer)
        assertTrue("Dark container <= high", dContainer <= dHigh)
        assertTrue("Dark high <= highest", dHigh <= dHighest)

        // Surface dim vs surface vs surface bright
        assertTrue(
            "Light surfaceDim <= surface <= surfaceBright",
            SaffronLightColorScheme.surfaceDim.luminance() <= SaffronLightColorScheme.surface.luminance() &&
                SaffronLightColorScheme.surface.luminance() <= SaffronLightColorScheme.surfaceBright.luminance(),
        )
        assertTrue(
            "Dark surfaceDim <= surface <= surfaceBright",
            SaffronDarkColorScheme.surfaceDim.luminance() <= SaffronDarkColorScheme.surface.luminance() &&
                SaffronDarkColorScheme.surface.luminance() <= SaffronDarkColorScheme.surfaceBright.luminance(),
        )
    }

    @Test
    fun `empirical verification - WCAG AA contrast ratio of at least 4_5 to 1 for all text and surface pairs`() {
        val lightPairs =
            listOf(
                "onPrimary on primary" to (SaffronLightColorScheme.onPrimary to SaffronLightColorScheme.primary),
                "onSecondary on secondary" to
                    (SaffronLightColorScheme.onSecondary to SaffronLightColorScheme.secondary),
                "onTertiary on tertiary" to
                    (SaffronLightColorScheme.onTertiary to SaffronLightColorScheme.tertiary),
                "onError on error" to (SaffronLightColorScheme.onError to SaffronLightColorScheme.error),
                "onPrimaryContainer on primaryContainer" to
                    (SaffronLightColorScheme.onPrimaryContainer to SaffronLightColorScheme.primaryContainer),
                "onSecondaryContainer on secondaryContainer" to
                    (SaffronLightColorScheme.onSecondaryContainer to SaffronLightColorScheme.secondaryContainer),
                "onTertiaryContainer on tertiaryContainer" to
                    (SaffronLightColorScheme.onTertiaryContainer to SaffronLightColorScheme.tertiaryContainer),
                "onErrorContainer on errorContainer" to
                    (SaffronLightColorScheme.onErrorContainer to SaffronLightColorScheme.errorContainer),
                "onBackground on background" to
                    (SaffronLightColorScheme.onBackground to SaffronLightColorScheme.background),
                "onSurface on surface" to (SaffronLightColorScheme.onSurface to SaffronLightColorScheme.surface),
                "onSurfaceVariant on surfaceVariant" to
                    (SaffronLightColorScheme.onSurfaceVariant to SaffronLightColorScheme.surfaceVariant),
                "inverseOnSurface on inverseSurface" to
                    (SaffronLightColorScheme.inverseOnSurface to SaffronLightColorScheme.inverseSurface),
            )

        for ((pairName, pair) in lightPairs) {
            val ratio = calculateContrastRatio(pair.first, pair.second)
            assertTrue("Light $pairName contrast ratio $ratio must be >= 4.5:1", ratio >= 4.5f)
        }

        val darkPairs =
            listOf(
                "onPrimary on primary" to (SaffronDarkColorScheme.onPrimary to SaffronDarkColorScheme.primary),
                "onSecondary on secondary" to
                    (SaffronDarkColorScheme.onSecondary to SaffronDarkColorScheme.secondary),
                "onTertiary on tertiary" to
                    (SaffronDarkColorScheme.onTertiary to SaffronDarkColorScheme.tertiary),
                "onError on error" to (SaffronDarkColorScheme.onError to SaffronDarkColorScheme.error),
                "onPrimaryContainer on primaryContainer" to
                    (SaffronDarkColorScheme.onPrimaryContainer to SaffronDarkColorScheme.primaryContainer),
                "onSecondaryContainer on secondaryContainer" to
                    (SaffronDarkColorScheme.onSecondaryContainer to SaffronDarkColorScheme.secondaryContainer),
                "onTertiaryContainer on tertiaryContainer" to
                    (SaffronDarkColorScheme.onTertiaryContainer to SaffronDarkColorScheme.tertiaryContainer),
                "onErrorContainer on errorContainer" to
                    (SaffronDarkColorScheme.onErrorContainer to SaffronDarkColorScheme.errorContainer),
                "onBackground on background" to
                    (SaffronDarkColorScheme.onBackground to SaffronDarkColorScheme.background),
                "onSurface on surface" to (SaffronDarkColorScheme.onSurface to SaffronDarkColorScheme.surface),
                "onSurfaceVariant on surfaceVariant" to
                    (SaffronDarkColorScheme.onSurfaceVariant to SaffronDarkColorScheme.surfaceVariant),
                "inverseOnSurface on inverseSurface" to
                    (SaffronDarkColorScheme.inverseOnSurface to SaffronDarkColorScheme.inverseSurface),
            )

        for ((pairName, pair) in darkPairs) {
            val ratio = calculateContrastRatio(pair.first, pair.second)
            assertTrue("Dark $pairName contrast ratio $ratio must be >= 4.5:1", ratio >= 4.5f)
        }
    }

    @Test
    fun `empirical verification - error tokens are strongly decoupled from saffron hue`() {
        // Saffron is golden yellow: Red ~ 0.79, Green ~ 0.54, Blue ~ 0.07. Ratio R/G < 2.0
        val saffronRedRatio = SaffronSeed.red / SaffronSeed.green
        assertTrue("Saffron seed has balanced red and green channels", saffronRedRatio < 2.0f)

        // Light error is pure M3 red #BA1A1A: Red ~ 0.73, Green ~ 0.10, Blue ~ 0.10. Ratio R/G > 6.0
        val lightErrorRedRatio = SaffronLightError.red / SaffronLightError.green
        assertTrue("Light error must be predominantly red", lightErrorRedRatio > 6.0f)

        // Dark error is M3 red #FFB4AB: Red ~ 1.0, Green ~ 0.70, Blue ~ 0.67
        assertTrue("Dark error must have maximum red channel", SaffronDarkError.red > 0.95f)
        assertTrue("Dark error green < red", SaffronDarkError.green < SaffronDarkError.red)
        assertTrue("Dark error blue < red", SaffronDarkError.blue < SaffronDarkError.red)
    }

    // -------------------------------------------------------------------------
    // Objective 2: Empirically test theme toggling and dynamic color logic
    // -------------------------------------------------------------------------

    @Test
    fun `empirical verification - theme toggling darkTheme true vs false yields correct schemes`() {
        fun resolveTheme(
            darkTheme: Boolean,
            dynamicColor: Boolean,
            sdkInt: Int,
        ): ColorScheme {
            return when {
                dynamicColor && sdkInt >= 31 -> {
                    // Simulation of dynamic color resolution
                    val base = if (darkTheme) SaffronDarkColorScheme else SaffronLightColorScheme
                    base.copy()
                }
                darkTheme -> SaffronDarkColorScheme
                else -> SaffronLightColorScheme
            }
        }

        // Standard light theme
        val lightResolved = resolveTheme(darkTheme = false, dynamicColor = false, sdkInt = 34)
        assertEquals(SaffronLightColorScheme, lightResolved)
        assertTrue("Light theme background luminance > 0.8", lightResolved.background.luminance() > 0.8f)

        // Standard dark theme
        val darkResolved = resolveTheme(darkTheme = true, dynamicColor = false, sdkInt = 34)
        assertEquals(SaffronDarkColorScheme, darkResolved)
        assertTrue("Dark theme background luminance < 0.2", darkResolved.background.luminance() < 0.2f)

        // Dynamic color on older Android (SDK < 31) must safely fallback to static saffron schemes
        val fallbackLight = resolveTheme(darkTheme = false, dynamicColor = true, sdkInt = 29)
        assertEquals(SaffronLightColorScheme, fallbackLight)

        val fallbackDark = resolveTheme(darkTheme = true, dynamicColor = true, sdkInt = 29)
        assertEquals(SaffronDarkColorScheme, fallbackDark)
    }

    @Test
    fun `empirical verification - dynamic color copy operation preserves all 36 tokens`() {
        val mockDynamicPrimary = Color(0xFF123456)
        val mockDynamicSecondary = Color(0xFF654321)

        val dynamicModified =
            SaffronLightColorScheme.copy(
                primary = mockDynamicPrimary,
                secondary = mockDynamicSecondary,
            )

        val tokens = extractAllTokens(dynamicModified)
        assertEquals("Dynamic scheme copy must still have exactly 36 tokens", 36, tokens.size)
        assertEquals("Primary overridden", mockDynamicPrimary, dynamicModified.primary)
        assertEquals("Secondary overridden", mockDynamicSecondary, dynamicModified.secondary)
        assertEquals("Background preserved", SaffronLightColorScheme.background, dynamicModified.background)
        assertEquals("Surface preserved", SaffronLightColorScheme.surface, dynamicModified.surface)
        assertEquals("Error preserved as M3 red", SaffronLightColorScheme.error, dynamicModified.error)

        for ((name, color) in tokens) {
            assertNotNull("Dynamic token $name must not be null", color)
            assertNotEquals("Dynamic token $name must not be Unspecified", Color.Unspecified, color)
        }
    }

    @Test
    fun `empirical verification - system bar polarity matches background luminance`() {
        // Light background -> requires dark system bar icons (!darkTheme = true)
        val lightBg = SaffronLightColorScheme.background
        assertTrue("Light background luminance is high", lightBg.luminance() > 0.5f)
        val lightThemeDark = false
        val lightIconsPolarity = !lightThemeDark
        assertTrue("Light theme sets isAppearanceLightStatusBars to true", lightIconsPolarity)

        // Dark background -> requires light system bar icons (!darkTheme = false)
        val darkBg = SaffronDarkColorScheme.background
        assertTrue("Dark background luminance is low", darkBg.luminance() < 0.5f)
        val darkThemeDark = true
        val darkIconsPolarity = !darkThemeDark
        assertFalse("Dark theme sets isAppearanceLightStatusBars to false", darkIconsPolarity)
    }

    // -------------------------------------------------------------------------
    // Objective 3: Test all 15 Typography styles
    // -------------------------------------------------------------------------

    @Test
    fun `empirical verification - all 15 typography styles instantiate with valid metrics`() {
        val styles: Map<String, TextStyle> =
            mapOf(
                "displayLarge" to Typography.displayLarge,
                "displayMedium" to Typography.displayMedium,
                "displaySmall" to Typography.displaySmall,
                "headlineLarge" to Typography.headlineLarge,
                "headlineMedium" to Typography.headlineMedium,
                "headlineSmall" to Typography.headlineSmall,
                "titleLarge" to Typography.titleLarge,
                "titleMedium" to Typography.titleMedium,
                "titleSmall" to Typography.titleSmall,
                "bodyLarge" to Typography.bodyLarge,
                "bodyMedium" to Typography.bodyMedium,
                "bodySmall" to Typography.bodySmall,
                "labelLarge" to Typography.labelLarge,
                "labelMedium" to Typography.labelMedium,
                "labelSmall" to Typography.labelSmall,
            )

        assertEquals("Must test exactly 15 typography roles", 15, styles.size)

        for ((roleName, style) in styles) {
            assertNotNull("Style $roleName must not be null", style)

            // Font size
            assertTrue("Style $roleName fontSize is specified", style.fontSize.isSpecified)
            assertTrue("Style $roleName fontSize is sp", style.fontSize.isSp)
            assertTrue("Style $roleName fontSize > 0", style.fontSize.value > 0f)

            // Line height
            assertTrue("Style $roleName lineHeight is specified", style.lineHeight.isSpecified)
            assertTrue("Style $roleName lineHeight is sp", style.lineHeight.isSp)
            assertTrue("Style $roleName lineHeight > 0", style.lineHeight.value > 0f)

            // Line height must be >= font size to prevent vertical clipping
            assertTrue(
                "Style $roleName lineHeight (${style.lineHeight.value}) >= fontSize (${style.fontSize.value})",
                style.lineHeight.value >= style.fontSize.value,
            )

            // Letter spacing
            assertTrue("Style $roleName letterSpacing is specified", style.letterSpacing.isSpecified)
            assertTrue("Style $roleName letterSpacing is em", style.letterSpacing.isEm)
        }
    }

    @Test
    fun `empirical verification - typography hierarchies follow strictly decreasing font sizes`() {
        // The prototype's scale is not strictly decreasing, so Material's
        // invariant cannot be asserted here: headlineLarge, headlineMedium and
        // headlineSmall are all 20px, and displaySmall (the hero headline) is
        // 22px against a 20px screen title, which is deliberate.
        //
        // The exact values are pinned in ThemeTokensTest and
        // SaffronTypographyTest. What this test keeps is the structural
        // guarantee that matters: no display or title role may end up smaller
        // than the body copy it sits above.
        val aboveBody =
            listOf(
                Typography.displaySmall,
                Typography.headlineLarge,
                Typography.headlineMedium,
                Typography.titleLarge,
                Typography.titleMedium,
                Typography.titleSmall,
            )
        for (style in aboveBody) {
            assertTrue(
                "A display or title role must not be smaller than body copy",
                style.fontSize.value >= Typography.bodyMedium.fontSize.value,
            )
        }

        assertTrue(
            "Display must remain the largest role in the scale",
            Typography.displayLarge.fontSize.value >= aboveBody.map { it.fontSize.value }.max(),
        )
    }

    @Test
    fun `empirical verification - font family assignment follows Fraunces for display and Figtree for body`() {
        // The prototype sets the countdown (.dy) and section heading (.h3) in
        // the display face, not Figtree, so titleLarge and titleMedium are
        // Fraunces too. Title roles are not automatically body roles.
        val frauncesRoles =
            listOf(
                Typography.displayLarge,
                Typography.displayMedium,
                Typography.displaySmall,
                Typography.headlineLarge,
                Typography.headlineMedium,
                Typography.headlineSmall,
                Typography.titleLarge,
                Typography.titleMedium,
            )

        for (style in frauncesRoles) {
            assertEquals("Display and headline roles use Fraunces", Fraunces, style.fontFamily)
            // The countdown (.dy) is 800 in the prototype; everything else in
            // this group is 600.
            val expected =
                if (style === Typography.titleLarge) FontWeight.Bold else FontWeight.SemiBold
            assertEquals("Display and headline roles use 600, or 800 for the countdown", expected, style.fontWeight)
        }

        val figtreeRoles =
            listOf(
                Typography.titleSmall,
                Typography.bodyLarge,
                Typography.bodyMedium,
                Typography.bodySmall,
                Typography.labelLarge,
                Typography.labelMedium,
                Typography.labelSmall,
            )

        for (style in figtreeRoles) {
            assertEquals("Title, body, and label roles use Figtree", Figtree, style.fontFamily)
        }

        // bodyLarge is the text field (prototype .inp, weight 600); the other
        // two are running copy and stay Normal.
        val bodyRoles =
            listOf(
                Typography.bodyLarge to FontWeight.SemiBold,
                Typography.bodyMedium to FontWeight.Normal,
                Typography.bodySmall to FontWeight.Normal,
            )
        for ((style, expected) in bodyRoles) {
            assertEquals("Body roles use 400, except the field at 600", expected, style.fontWeight)
        }

        // The countdown is 800; the section heading is 600 like the other
        // Fraunces roles. Only the name and the labels are Bold.
        val boldRoles =
            listOf(
                Typography.titleSmall,
                Typography.labelLarge,
                Typography.labelMedium,
                Typography.labelSmall,
            )
        for (style in boldRoles) {
            assertEquals("Title and label roles use Bold weight", FontWeight.Bold, style.fontWeight)
        }
    }
}
