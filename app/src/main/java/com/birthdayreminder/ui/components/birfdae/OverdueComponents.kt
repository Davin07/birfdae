package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.birthdayreminder.domain.model.OverdueBirthday

/**
 * A missed birthday, with a decision attached.
 *
 * Both actions are deliberately required. "Send a belated wish" opens the card,
 * and "Not this year" records that the user is done with it for now. There is
 * no dismiss, because a dismiss that does not persist brings the same prompt
 * back on the next launch.
 *
 * The tone is warm rather than alarming. A forgotten birthday is not an error,
 * and the app has no business making it feel like one.
 *
 * @param overdue the missed birthday
 * @param onSendBelatedWish open the card for this person
 * @param onNotThisYear record the skip for the current year
 * @param modifier applied to the card
 */
@Composable
fun OverdueBirthdayCard(
    overdue: OverdueBirthday,
    onSendBelatedWish: () -> Unit,
    onNotThisYear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(modifier = Modifier.padding(SaffronTokens.space16)) {
            Text(
                text = overdueHeadline(overdue.daysOverdue),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(Modifier.height(SaffronTokens.space4))

            Text(
                text = overdue.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(SaffronTokens.space4))

            Text(
                text = overdueSubtitle(overdue),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            Spacer(Modifier.height(SaffronTokens.space16))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
            ) {
                SaffronButton(
                    onClick = onSendBelatedWish,
                    label = "Send a belated wish",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(SaffronTokens.space4))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Outlined rather than filled: declining is a valid answer,
                // but it is not the thing the card is asking you to do.
                SaffronOutlinedButton(
                    onClick = onNotThisYear,
                    label = "Not this year",
                )
            }
        }
    }
}

/**
 * The line under a missed birthday's name: the age they turned and when.
 *
 * @param overdue the missed birthday
 * @return a single line, e.g. "Turned 41 · 12 August"
 */
fun overdueSubtitle(overdue: OverdueBirthday): String {
    val month = overdue.occurredOn.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "Turned ${overdue.ageTurned} · ${overdue.occurredOn.dayOfMonth} $month"
}

/**
 * The headline above a missed birthday's name.
 *
 * @param daysOverdue how many days past the date it is
 * @return a phrase, not a countdown
 */
fun overdueHeadline(daysOverdue: Int): String =
    when (daysOverdue) {
        0 -> "TODAY"
        1 -> "YESTERDAY"
        in 2..6 -> "$daysOverdue DAYS AGO"
        else -> "OVERDUE"
    }
