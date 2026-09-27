package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Saffron design tokens: spacing, radii, sizes and the touch-target floor.
 *
 * Everything in `ui/components/BirfDae` and every screen must take its
 * dimensions from here rather than hard-coding dp literals. The legacy
 * Lumina system carried ~128 raw dp values across the UI, which is how a
 * design system drifts away from its own spacing rhythm.
 */
object SaffronTokens {
    // --- Spacing scale -------------------------------------------------------
    // A 4dp base grid. Use these instead of ad-hoc values.
    val space2: Dp = 2.dp
    val space4: Dp = 4.dp
    val space6: Dp = 6.dp
    val space8: Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp
    val space32: Dp = 32.dp

    /**
     * Standard horizontal screen gutter. Every screen uses this so the
     * left edge lines up across navigation.
     */
    val gutter: Dp = 18.dp

    // --- Radii ---------------------------------------------------------------
    val radiusSmall = RoundedCornerShape(12.dp)
    val radiusMedium = RoundedCornerShape(15.dp)
    val radiusLarge = RoundedCornerShape(17.dp)
    val radiusCard = RoundedCornerShape(20.dp)
    val radiusSheet = RoundedCornerShape(24.dp)

    /**
     * Minimum interactive size. Android accessibility guidance is 48dp;
     * the approved prototype uses 44dp as the floor for compact controls
     * (chips, icon buttons) where 48dp would crowd dense rows.
     */
    val minTouchTarget: Dp = 44.dp
    val minFieldHeight: Dp = 48.dp

    // --- Component sizes -----------------------------------------------------
    val avatarSmall: Dp = 32.dp
    val avatarMedium: Dp = 42.dp
    val avatarLarge: Dp = 56.dp
    val iconButton: Dp = 44.dp
    val chipHeight: Dp = 44.dp
    val navBarHeight: Dp = 72.dp
    val fabSize: Dp = 46.dp
}

/**
 * Returns a stable accent colour for a person, chosen deterministically from
 * their name.
 *
 * The approved palette splits accents across primary (saffron), secondary
 * (rosewood) and tertiary (plum) so that a list of people does not render as
 * a column of identical gold circles. Deriving the index from the name means
 * the same person keeps the same colour on every screen and every launch.
 *
 * @param name person's name, used only as a stable hash input
 * @return a container/on-container colour pair from the theme
 */
@Composable
@ReadOnlyComposable
fun accentFor(name: String): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val buckets =
        listOf(
            scheme.primaryContainer to scheme.onPrimaryContainer,
            scheme.secondaryContainer to scheme.onSecondaryContainer,
            scheme.tertiaryContainer to scheme.onTertiaryContainer,
        )
    val index = (name.hashCode().and(Int.MAX_VALUE)) % buckets.size
    return buckets[index]
}
