package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Saffron screen background.
 *
 * Replaces the old background, which drew a blurred two-blob Canvas gradient
 * over the whole screen and then hard-coded a light-mode background that
 * overrode the theme. Saffron is flat: colour comes from the surface role and
 * hierarchy comes from type, spacing and the container roles.
 */
@Composable
fun SaffronBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

/**
 * Flat surface card. The old glass card had 21 call sites and used a
 * translucent fill plus a gradient border and needed a manual dark-mode branch
 * to stay legible.
 *
 * @param modifier applied to the card
 * @param content card contents
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = SaffronTokens.radiusCard,
        color = containerColor,
        contentColor = contentColor,
    ) {
        content()
    }
}

/**
 * Top app bar with an optional back affordance and trailing actions.
 *
 * The legacy header rendered a 40sp title in the centre of a Box, which
 * collided with both the back button and any trailing action. This lays out as
 * a normal Row so the three regions cannot overlap.
 *
 * @param title screen title
 * @param onBackClick when non-null, shows a back button
 * @param modifier applied to the bar
 * @param actions trailing content, e.g. a settings icon
 */
@Composable
fun SectionHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SaffronTokens.gutter)
                .padding(top = SaffronTokens.space8, bottom = SaffronTokens.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            SaffronIconButton(
                onClick = onBackClick,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
            Spacer(Modifier.width(SaffronTokens.space8))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        if (actions != null) {
            actions()
        }
    }
}

/**
 * A hairline rule between groups inside a card or a form.
 *
 * The concept separates the reminder controls from the reassurance text with
 * one of these. A rule rather than extra spacing, because the two groups are
 * close enough in tone that space alone reads as a mistake.
 *
 * @param modifier applied to the rule
 */
@Composable
fun SurfaceDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * A tappable card: the surface shape, with a row of content inside it.
 *
 * The concept's per-person block is a card the user taps to go deeper, so it
 * needs the card's radius and fill but not the button treatment -- an
 * interactive card should not read as a primary action. Kept separate from
 * [SurfaceCard] so a non-interactive card cannot accidentally grow a click.
 *
 * @param onClick invoked when the card is tapped
 * @param modifier applied to the card
 * @param content the card's contents
 */
@Composable
fun SaffronCardRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = SaffronTokens.radiusCard,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(SaffronTokens.space16),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * A quiet heading that groups the controls beneath it inside scrolling content.
 *
 * [SectionHeader] is a screen title: large, and the first thing read. This is
 * the label above a group of chips, and it has to be quieter than the values it
 * introduces or it competes with the value the user is choosing.
 *
 * @param title the group label
 * @param modifier applied to the text
 */
@Composable
fun SectionSubhead(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

/**
 * Section heading with an optional trailing text action, used inside scrolling
 * content (for example "Next up" / "See all").
 *
 * @param title section title
 * @param modifier applied to the row
 * @param actionLabel optional trailing label
 * @param onActionClick invoked when the trailing label is tapped
 */
@Composable
fun SectionLabel(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SaffronTokens.gutter)
                .padding(top = SaffronTokens.space20, bottom = SaffronTokens.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.heightIn(min = SaffronTokens.minTouchTarget),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Circular icon button at the 44dp touch floor.
 *
 * @param onClick invoked on tap
 * @param icon icon to draw
 * @param contentDescription accessibility label
 * @param modifier applied to the button
 * @param tint icon tint, defaults to onSurfaceVariant
 * @param containerColor background, defaults to surfaceContainer
 */
@Composable
fun SaffronIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(SaffronTokens.iconButton),
        shape = CircleShape,
        color = containerColor,
        contentColor = tint,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(SaffronTokens.space20),
            )
        }
    }
}

/**
 * Pill-shaped selectable chip with a 44dp touch floor.
 *
 * Replaces the old chip, whose selected state was a 20%-alpha primary fill
 * with primary-coloured text — roughly 2.4:1 against its own background.
 *
 * @param selected whether this chip is active
 * @param onClick invoked on tap
 * @param label chip text
 * @param modifier applied to the chip
 */
@Composable
fun SaffronChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .heightIn(min = SaffronTokens.chipHeight)
                .defaultMinSize(minWidth = SaffronTokens.minTouchTarget),
        shape = CircleShape,
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        border =
            if (selected) {
                null
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
    ) {
        Box(
            modifier = Modifier.padding(horizontal = SaffronTokens.space16),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Filled primary action button.
 *
 * @param onClick invoked on tap
 * @param label button text
 * @param modifier applied to the button
 * @param icon optional leading icon
 * @param enabled whether the button is interactive
 */
@Composable
fun SaffronButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    /**
     * Overrides for surfaces that need their own button colour, such as the
     * overdue card, whose accent is not the app's saffron primary. Null keeps
     * the theme default, so no existing caller changes.
     */
    colors: ButtonColors? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = SaffronTokens.minFieldHeight),
        enabled = enabled,
        shape = SaffronTokens.radiusMedium,
        colors =
            colors
                ?: ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        contentPadding = PaddingValues(horizontal = SaffronTokens.space20),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(SaffronTokens.space20))
            Box(Modifier.size(SaffronTokens.space8))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Secondary action button on a container fill.
 *
 * @param onClick invoked on tap
 * @param label button text
 * @param modifier applied to the button
 * @param enabled whether the button is interactive
 */
@Composable
fun SaffronSecondaryButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = SaffronTokens.minFieldHeight),
        enabled = enabled,
        shape = SaffronTokens.radiusMedium,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        contentPadding = PaddingValues(horizontal = SaffronTokens.space20),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(SaffronTokens.space20))
            Box(Modifier.size(SaffronTokens.space8))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Outlined action button, for tertiary actions that should not read as
 * primary.
 *
 * @param onClick invoked on tap
 * @param label button text
 * @param modifier applied to the button
 * @param enabled whether the button is interactive
 */
@Composable
fun SaffronOutlinedButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = SaffronTokens.minFieldHeight),
        enabled = enabled,
        shape = SaffronTokens.radiusMedium,
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Small status pill, e.g. "Turning 33" or a zodiac tag.
 *
 * @param text pill label
 * @param modifier applied to the pill
 * @param containerColor background, defaults to surfaceContainerHigh
 * @param contentColor text colour, defaults to onSurfaceVariant
 */
@Composable
fun SaffronBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = SaffronTokens.space12, vertical = SaffronTokens.space6),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/**
 * Days-until counter in the display serif.
 *
 * The numeral is the fastest thing to read on a person row, so it gets
 * Fraunces at headline scale while everything around it stays in the sans.
 *
 * @param days days remaining
 * @param modifier applied to the block
 * @param caption small text under the numeral, e.g. "days"
 */
@Composable
fun CountdownPill(
    days: Int,
    modifier: Modifier = Modifier,
    caption: String = if (days == 1) "day" else "days",
) {
    Column(
        modifier = modifier.clearAndSetSemantics {},
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = days.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
