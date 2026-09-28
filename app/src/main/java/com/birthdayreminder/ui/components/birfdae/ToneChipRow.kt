package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.birthdayreminder.ui.card.CardTone

/**
 * The tone selector for the birthday card's message.
 *
 * Lives in the design system rather than the card screen because the same
 * control will be wanted anywhere a message can be reworded, and because the
 * horizontal scroll plus the re-roll button is a specific interaction that
 * should be built once.
 *
 * The row scrolls because the four tones do not fit a 360dp phone at the
 * 44dp touch-target floor. The trailing partial chip is deliberate: it is the
 * affordance that says there is more to the right.
 */
@Composable
fun ToneChipRow(
    selected: CardTone,
    onSelect: (CardTone) -> Unit,
    onReroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Built on the shared row so the scroll behaviour, spacing and chip styling
    // cannot drift from the other chip rows in the app.
    SaffronChipRow(
        chips = CardTone.entries.toList(),
        selected = selected,
        labelOf = { it.label },
        onSelect = onSelect,
        // Re-roll sits at the end of the set, after the tones, because it acts
        // on the current tone rather than choosing one. It is an action, not an
        // option, so it is passed as a trailing action and never takes the
        // selected state.
        trailingLabel = "Another",
        trailingAction = onReroll,
        modifier = modifier,
    )
}

/**
 * A horizontally scrollable row of single-select chips.
 *
 * Every chip row in the app is this, because every one of them outgrows a 360dp
 * phone at the 44dp touch-target floor: the concept's six relationship chips,
 * four search filters and four tones. Scrolling is preferred over wrapping
 * because a wrapped row changes the height of everything below it, and a
 * filter row that reflows on selection makes the list jump under the finger.
 *
 * The trailing chip is deliberately allowed to sit partly off-screen: that
 * partial edge is the affordance saying there is more to the right. A fade or
 * a "more" button would cost the same space and tell the user less.
 *
 * @param chips the options, in the order they should appear
 * @param selected the currently chosen option
 * @param labelOf what each option shows
 * @param onSelect invoked with the tapped option
 * @param modifier applied to the scrolling row
 */
@Composable
fun <T> SaffronChipRow(
    chips: List<T>,
    // Nullable, because "no choice yet" is a real state on the wizard's
    // relationship row and pre-selecting the first chip would give the user a
    // relationship they never picked.
    selected: T?,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
    trailingAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { chip ->
            SaffronChip(
                selected = chip != null && chip == selected,
                onClick = { onSelect(chip) },
                label = labelOf(chip),
            )
        }

        // An action rather than an option: same chip shape so the row reads as
        // one control, but never selectable, because it does not represent a
        // value the user is choosing.
        if (trailingLabel != null && trailingAction != null) {
            SaffronChip(
                selected = false,
                onClick = trailingAction,
                label = trailingLabel,
            )
        }
    }
}

/**
 * Icon-only re-roll, for the card's own action row.
 *
 * Separate from [ToneChipRow] because on the card it needs a 48dp target and
 * a distinct colour to read as a refresh rather than a tone.
 */
@Composable
fun RerollIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SaffronIconButton(
        onClick = onClick,
        icon = Icons.Filled.Refresh,
        contentDescription = "Try another line",
        modifier = modifier,
    )
}
