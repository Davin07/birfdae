package com.birthdayreminder.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.birthdayreminder.R

/**
 * Custom font families for the Birf Dae Saffron UI.
 *
 * Fraunces: display serif for headlines, display text and the birthday card.
 * Figtree: geometric sans for titles, body copy and labels.
 *
 * Both are static TTFs under `res/font/`, so they render identically offline
 * and `ui-text-google-fonts` cert-pinning is not in the critical path.
 *
 * An earlier draft wrapped these in try/catch with a system-font fallback. That
 * could never have worked as intended: `R.font.*` are compile-time constants,
 * so a missing or renamed resource fails the build rather than throwing at
 * runtime here. A silent fallback would also ship a wrong-looking app instead
 * of a loud failure, so there isn't one.
 */
val Fraunces: FontFamily =
    FontFamily(
        Font(R.font.fraunces_semibold, FontWeight.SemiBold),
    )

val Figtree: FontFamily =
    FontFamily(
        Font(R.font.figtree_regular, FontWeight.Normal),
        Font(R.font.figtree_medium, FontWeight.Medium),
        Font(R.font.figtree_bold, FontWeight.Bold),
    )
