package com.birthdayreminder.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The two colours the overdue state paints with.
 *
 * Deliberately not ColorScheme roles. The plan calls overdue "warm-urgent, not
 * alarming red" (§5.4) and the only warm-urgent slot in the Material scheme is
 * the error family, which would make a missed birthday read as a fault. Tertiary
 * is the plum the tonal palette generated, which puts a lilac card on a saffron
 * screen. Neither is right, so overdue carries its own pair.
 *
 * @property container the card background
 * @property onContainer the text and button colour on it
 */
@Immutable
data class OverdueColors(
    val container: Color,
    val onContainer: Color,
)

val SaffronLightOverdue =
    OverdueColors(
        container = SaffronLightOverdueContainer,
        onContainer = SaffronLightOnOverdueContainer,
    )

val SaffronDarkOverdue =
    OverdueColors(
        container = SaffronDarkOverdueContainer,
        onContainer = SaffronDarkOnOverdueContainer,
    )

/**
 * The overdue colours for the active theme.
 *
 * Defaults to the light pair so a preview or a test that does not install the
 * theme still gets readable text rather than an uninitialised colour.
 */
val LocalOverdueColors = staticCompositionLocalOf { SaffronLightOverdue }
