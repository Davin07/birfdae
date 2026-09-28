package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.ui.components.birfdae.PersonAvatar
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronChipRow
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionSubhead
import com.birthdayreminder.ui.components.birfdae.SurfaceCard
import com.birthdayreminder.ui.components.birfdae.SurfaceDivider
import com.birthdayreminder.ui.viewmodel.PerPersonReminderViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * One person's reminder: when to tell them, and how far ahead.
 *
 * Matches the concept's single-person screen: a card for the person, then two
 * chip rows, then Save.
 *
 * Changes are held as a draft and written on Save rather than written on every
 * tap. Writing on every tap means a mis-tap is already in the database and
 * there is no way back to the value that was meant; with a Save, the chips are
 * a draft and the button is the decision.
 *
 * @param personId whose reminder to edit
 * @param onNavigateBack returns to the per-person list
 * @param viewModel the screen ViewModel
 */
@Composable
fun PerPersonScreen(
    personId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PerPersonReminderViewModel = hiltViewModel(),
) {
    val people by viewModel.people.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val person = people.firstOrNull { it.id == personId }

    var draftLead by rememberSaveable(personId) { mutableStateOf<Int?>(null) }
    var draftTime by rememberSaveable(personId) { mutableStateOf<LocalTime?>(null) }
    var saved by rememberSaveable(personId) { mutableStateOf(false) }

    // Adopt the stored values once they arrive, so the chips show the truth on
    // open rather than nothing selected until the database responds.
    LaunchedEffect(person?.id, person?.leadTimeDays, person?.reminderTime) {
        if (person != null) {
            draftLead = person.leadTimeDays
            draftTime = person.reminderTime
        }
    }

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(
                title = person?.let { "${it.name}'s reminder" } ?: "Reminder",
                onBackClick = onNavigateBack,
            )

            if (person == null) {
                // Only shown for the moment between opening and the database
                // answering, so it stays quiet rather than explaining itself.
                return@Column
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SaffronTokens.gutter),
                verticalArrangement = Arrangement.spacedBy(SaffronTokens.space16),
            ) {
                Spacer(Modifier.height(SaffronTokens.space8))

                // Who this is, before what to change about them.
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
                    ) {
                        PersonAvatar(name = person.name, imageUri = null, size = 42.dp)
                        Column {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text =
                                    person.birthDate.format(
                                        DateTimeFormatter.ofPattern("d MMMM"),
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                SectionSubhead("Lead time")
                SaffronChipRow(
                    chips = LeadTimeChipLabel.options,
                    selected =
                        LeadTimeChipLabel.optionForDays(draftLead ?: person.leadTimeDays)
                            ?: LeadTimeChipLabel.ON_THE_DAY,
                    labelOf = { it.label },
                    onSelect = {
                        draftLead = it.days
                        saved = false
                    },
                )

                SectionSubhead("Time")
                SaffronChipRow(
                    chips = ReminderTimeChoice.options,
                    // No stored time means the app default applies, which is
                    // 9:00 AM -- so that is the chip shown as chosen. Showing
                    // nothing selected would imply reminders are unset.
                    selected =
                        ReminderTimeChoice.of(draftTime ?: person.reminderTime)
                            ?: ReminderTimeChoice.MORNING,
                    labelOf = { it.label },
                    onSelect = {
                        draftTime = LocalTime.of(it.hour, it.minute)
                        saved = false
                    },
                )

                SurfaceDivider()

                Text(
                    text = "A reminder arrives as a normal notification. Nothing opens on its own.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                errors[person.id]?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (saved) {
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }

                SaffronButton(
                    onClick = {
                        draftLead?.let { viewModel.setLeadTime(person.id, it) }
                        draftTime?.let { viewModel.setReminderTime(person.id, it) }
                        saved = true
                    },
                    label = "Save",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(SaffronTokens.space24))
            }
        }
    }
}

/** The route and argument for [PerPersonScreen]. */
object PerPersonArgs {
    /** The route pattern, with the person's id. */
    const val ROUTE = "per_person/{personId}"

    /**
     * @param personId the person to open
     */
    fun route(personId: Long): String = "per_person/$personId"
}
