package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/**
 * One line in a settings list: a title, an optional explanation, and an
 * optional trailing control.
 *
 * The approved concept uses flat rows separated by hairlines rather than a
 * stack of cards. Cards read as a dashboard; a settings list reads as a list,
 * and it is a list because the rows are peers - there is no primary item.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
) {
    val content =
        @Composable {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = SaffronTokens.space12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space16),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(SaffronTokens.space4))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                trailing?.invoke()
            }
        }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column {
                content()
                if (showDivider) Hairline()
            }
        }
    } else {
        Column(modifier = modifier) {
            content()
            if (showDivider) Hairline()
        }
    }
}

/**
 * A settings row with a switch.
 *
 * The label, subtitle and control are one 48dp-tall tap target rather than a
 * tappable row beside a separate switch, which is the common source of
 * "the switch did not respond" reports.
 */
@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = true,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        showDivider = showDivider,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                // The row owns the interaction, so the switch must not also be
                // reachable by accessibility services -- otherwise TalkBack
                // announces two controls and swipe order stops matching the
                // visual order.
                modifier = Modifier.clearAndSetSemantics { },
            )
        },
    )
}

/** The hairline between rows in a settings group. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

/**
 * Groups settings rows under a section label.
 *
 * @param title the section name, e.g. "Reminders"
 * @param content the rows, in order
 */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(title = title)
        Spacer(Modifier.height(SaffronTokens.space4))
        content()
    }
}

/**
 * One lead-time choice, e.g. "1 week before".
 *
 * @param label the value in words, because "7" is ambiguous between days and
 *   weeks and between advance and after
 * @param selected whether this is the person's current value
 * @param onClick applies the choice
 */
@Composable
fun LeadTimeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = SaffronTokens.space12, vertical = SaffronTokens.space8),
        )
    }
}
