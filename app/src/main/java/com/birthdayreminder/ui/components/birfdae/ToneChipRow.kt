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
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CardTone.entries.forEach { tone ->
            SaffronChip(
                selected = tone == selected,
                onClick = { onSelect(tone) },
                label = tone.label,
            )
        }

        // Re-roll sits at the end of the set, after the tones, because it acts
        // on the current tone rather than choosing one.
        SaffronChip(
            selected = false,
            onClick = onReroll,
            label = "Another",
        )
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
