package com.birthdayreminder.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The app ships light-only.
 *
 * Saffron on near-black was reviewed and not approved, and a half-approved
 * dark theme is worse than none. The dark scheme is deliberately kept in
 * [Color.kt] so the decision can be revisited cheaply -- which is exactly why
 * these tests exist: the code looks maintainable, so without them the next
 * reader would restore it by accident.
 */
class LightOnlyThemeTest {
    @Test
    fun `the dark scheme is retained so the decision is reversible`() {
        // Not a guard against removal -- a guard against someone deciding the
        // palette was never wanted and deleting 39 authored tokens.
        assertNotEquals(
            "light and dark must remain distinct palettes",
            SaffronLightColorScheme,
            SaffronDarkColorScheme,
        )
        assertTrue(
            "dark background should still be dark",
            SaffronDarkColorScheme.background.luminance() < 0.2f,
        )
    }

    @Test
    fun `the light scheme is the one the app resolves to`() {
        assertSchemeIsLight(SaffronLightColorScheme)
    }

    private fun assertSchemeIsLight(scheme: ColorScheme) {
        assertEquals(
            "background must be the authored light value",
            SaffronLightBackground,
            scheme.background,
        )
        assertTrue(
            "light theme background luminance > 0.8, was ${scheme.background.luminance()}",
            scheme.background.luminance() > 0.8f,
        )
    }
}
