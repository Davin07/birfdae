package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronCardRow
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.viewmodel.PerPersonReminderRow
import com.birthdayreminder.ui.viewmodel.PerPersonReminderViewModel

/**
 * The list of people whose reminders can be overridden.
 *
 * Each person is one block showing who they are and when they are reminded;
 * tapping a block opens [PerPersonScreen] with the full set of choices. The
 * concept treats a single person as a screen of its own, because eight chips
 * listed beside a name leaves no room to read the name next to the value it
 * controls.
 *
 * @param onNavigateBack returns to Settings
 * @param onOpenPerson opens one person's reminder settings
 * @param viewModel screen ViewModel
 */
@Composable
fun PerPersonReminderScreen(
    onNavigateBack: () -> Unit,
    onOpenPerson: (Long) -> Unit,
    viewModel: PerPersonReminderViewModel = hiltViewModel(),
) {
    val people by viewModel.people.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(
                title = "Per-person reminders",
                onBackClick = onNavigateBack,
            )

            if (people.isEmpty()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(SaffronTokens.gutter),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Nobody to remind yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Add someone and their reminder settings will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = SaffronTokens.space8),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = SaffronTokens.gutter,
                            end = SaffronTokens.gutter,
                            bottom = SaffronTokens.navBarHeight,
                        ),
                    verticalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
                ) {
                    items(people, key = { it.id }) { person ->
                        PersonReminderSummary(
                            person = person,
                            errorMessage = errors[person.id],
                            onClick = { onOpenPerson(person.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * One person in the list, summarising what their reminder currently is.
 *
 * Shows the value, not just the name, because the question this screen answers
 * is "who do I get reminded about, and when". A list of names makes the user
 * open each one to learn anything at all.
 *
 * @param person the person
 * @param errorMessage why their last change failed, if it did
 * @param onClick opens their full reminder settings
 */
@Composable
private fun PersonReminderSummary(
    person: PerPersonReminderRow,
    errorMessage: String?,
    onClick: () -> Unit,
) {
    SaffronCardRow(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = PerPersonSummary.of(person),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SaffronTokens.space2),
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = SaffronTokens.space4),
                )
            }
        }
    }
}

/**
 * What the list line under a person name says.
 *
 * Out of the composable so the wording can be tested, and so the "reminders
 * off" case is stated in one place rather than at each call site.
 */
object PerPersonSummary {
    /**
     * @param person the person being summarised
     */
    fun of(person: PerPersonReminderRow): String {
        if (!person.notificationsEnabled) return "No reminder"
        val time = person.reminderTime?.let(ReminderClock::format) ?: "your default time"
        val lead = LeadTimeChipLabel.forDays(person.leadTimeDays)
        return if (lead == LeadTimeChipLabel.ON_THE_DAY.label) time else "$time · $lead before"
    }
}

/**
 * The lead-time labels, including the "On the day" option the wizard omits.
 *
 * The wizard offers only advance reminders -- that step is about being reminded
 * *earlier* -- but a person can be set to the day itself, and hiding that value
 * here would make an existing setting look unselectable.
 */
enum class LeadTimeChipLabel(
    val days: Int,
    val label: String,
) {
    ON_THE_DAY(days = 0, label = "On the day"),
    ONE_DAY(days = 1, label = "1 day"),
    THREE_DAYS(days = 3, label = "3 days"),
    ONE_WEEK(days = 7, label = "1 week"),
    ;

    companion object {
        val options: List<LeadTimeChipLabel> = entries.toList()

        /**
         * The label for a stored offset.
         *
         * @param days the stored advance offset
         */
        fun forDays(days: Int): String = entries.firstOrNull { it.days == days }?.label ?: "$days days"

        /**
         * The option for a stored offset, or null when the offset is not one
         * of the four.
         *
         * Null rather than a fallback: a birthday saved with an offset the
         * chips do not offer should show nothing selected, which is true, and
         * not silently pretend it is set to one of the visible values.
         *
         * @param days the stored advance offset
         */
        fun optionForDays(days: Int): LeadTimeChipLabel? = entries.firstOrNull { it.days == days }
    }
}

/**
 * The times offered for a reminder.
 *
 * Four fixed choices rather than a time picker, matching the concept. A picker
 * is more precise and slower, and the useful cases are "early", "morning",
 * "midday" and "evening" -- each of which is a time somebody would otherwise
 * have to think up and key in.
 */
enum class ReminderTimeChoice(
    val hour: Int,
    val minute: Int,
    val label: String,
) {
    EARLY(7, 0, "7:00 AM"),
    MORNING(9, 0, "9:00 AM"),
    NOON(12, 0, "12:00 PM"),
    EVENING(20, 0, "8:00 PM"),
    ;

    companion object {
        val options: List<ReminderTimeChoice> = entries.toList()

        /**
         * @param time the stored time, or null when the app default applies
         */
        fun of(time: java.time.LocalTime?): ReminderTimeChoice? =
            time?.let { t -> entries.firstOrNull { it.hour == t.hour && it.minute == t.minute } }
    }
}
