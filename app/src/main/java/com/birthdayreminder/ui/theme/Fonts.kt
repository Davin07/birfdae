package com.birthdayreminder.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.birthdayreminder.R

/**
 * Custom font families for the Birf Dae Saffron UI.
 *
 * Fraunces: Display serif font used for headlines, display text, and celebration cards.
 * Figtree: Modern geometric sans-serif used for titles, body copy, and labels.
 *
 * Sourced as static TTF resources from res/font/ for 100% offline resilience.
 * Wrapped in try-catch falling back gracefully to FontFamily.Serif / FontFamily.Default
 * in case of headless test or resource loading failure.
 */
val Fraunces: FontFamily =
    try {
        FontFamily(
            Font(R.font.fraunces_semibold, FontWeight.SemiBold),
        )
    } catch (e: Throwable) {
        FontFamily.Serif
    }

val Figtree: FontFamily =
    try {
        FontFamily(
            Font(R.font.figtree_regular, FontWeight.Normal),
            Font(R.font.figtree_medium, FontWeight.Medium),
            Font(R.font.figtree_bold, FontWeight.Bold),
        )
    } catch (e: Throwable) {
        FontFamily.Default
    }

// Aliases for compatibility
val FrauncesFamily = Fraunces
val FigtreeFamily = Figtree
