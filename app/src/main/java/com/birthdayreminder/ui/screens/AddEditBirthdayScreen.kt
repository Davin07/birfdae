package com.birthdayreminder.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.Relationship
import com.birthdayreminder.domain.util.AgeUtils
import com.birthdayreminder.domain.util.ZodiacUtils
import com.birthdayreminder.ui.card.CardTone
import com.birthdayreminder.ui.components.birfdae.SaffronAvatarPicker
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronChipRow
import com.birthdayreminder.ui.components.birfdae.SaffronTextField
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionSubhead
import com.birthdayreminder.ui.components.birfdae.SurfaceCard
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBirthdayScreen(
    birthdayId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddEditBirthdayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(birthdayId) {
        viewModel.initializeForm(birthdayId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
            viewModel.resetSaveSuccess()
        }
    }

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(
                title = if (uiState.isEditMode) "Edit Birthday" else "Add a birthday",
                onBackClick = {
                    if (uiState.step > 1) {
                        viewModel.previousStep()
                    } else {
                        onNavigateBack()
                    }
                },
            )

            // Content Area
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = SaffronTokens.gutter)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SaffronTokens.space20),
            ) {
                // Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Step ${uiState.step} of 3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text =
                            when (uiState.step) {
                                1 -> "Identity"
                                2 -> "Date"
                                else -> "Personalize"
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Progress Bar
                Box(
                    modifier =
                        Modifier.fillMaxWidth().height(
                            4.dp,
                        ).background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(
                                uiState.step / 3f,
                            ).height(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }

                when (uiState.step) {
                    1 -> Step1Identity(uiState, viewModel)
                    2 -> Step2Date(uiState, viewModel)
                    3 -> Step3Personalization(uiState, viewModel)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Bottom Action Bar
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The button stays disabled until the step is valid, but the
                // reason is always stated above it. A disabled control with no
                // explanation is the most common complaint in form review.
                //
                // A Column, not a Box: as siblings in a Box the message and the
                // button occupied the same place and the message rendered
                // *inside* the button, half-legible on its disabled fill.
                val stepError = viewModel.currentStepError()

                if (stepError != null) {
                    Text(
                        text = stepError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = SaffronTokens.space8),
                    )
                }

                SaffronButton(
                    onClick = {
                        if (uiState.step < 3) {
                            viewModel.nextStep()
                        } else {
                            viewModel.saveBirthday()
                        }
                    },
                    label = if (uiState.step < 3) "Continue" else "Save birthday",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canProceed(uiState),
                )
            }
        }
    }
}

private fun canProceed(uiState: AddEditBirthdayUiState): Boolean {
    return when (uiState.step) {
        1 -> uiState.name.isNotBlank()
        2 -> uiState.birthDate != null
        else -> true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Identity(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel,
) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                }
                viewModel.updateImageUri(uri.toString())
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    ) {
        // Image Picker
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SaffronAvatarPicker(
                imageUri = uiState.imageUri,
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )
            Text(
                text = if (uiState.imageUri == null) "Add a photo" else "Change photo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Inputs
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            SaffronTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = "Name",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
            )

            // Relationship
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RELATIONSHIP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // The same vocabulary Search filters by. The two used to be
                // independent lists, which meant a person saved as "Work" could
                // not be found by the relationship they were tagged with.
                SaffronChipRow(
                    chips = Relationship.all,
                    // A person saved under the old wizard's vocabulary ("Friend",
                    // "Work", "Acquaintance") must still show its chip as chosen,
                    // or the value silently looks unset and saving would clear it.
                    // Null when nothing is stored or the stored value is not
                    // one of the six, so no chip is shown as chosen. Defaulting
                    // to the first would pre-select a relationship the user
                    // never picked, and every saved person would silently
                    // acquire it.
                    selected = Relationship.fromStored(uiState.relationship),
                    labelOf = { it.label },
                    onSelect = { viewModel.updateRelationship(it.stored) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Date(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = uiState.birthDate?.toEpochDay()?.times(24 * 60 * 60 * 1000),
            initialDisplayMode = DisplayMode.Picker,
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= System.currentTimeMillis()
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        return year <= java.time.LocalDate.now().year
                    }
                },
        )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.updateBirthDate(date)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "When is their birthday?",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // The date picker already refuses future years, so this is the
        // "pick a date at all" case. It needs saying out loud, because the
        // Continue button is disabled until a date exists.
        uiState.birthDateError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        // Selected Date Card
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "SELECTED DATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = uiState.birthDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select Date",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Embedded Date Picker
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.height(400.dp)) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier.padding(0.dp),
                    colors =
                        DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
        }

        if (uiState.birthDate != null) {
            val zodiac = ZodiacUtils.getZodiacSign(uiState.birthDate.month, uiState.birthDate.dayOfMonth)
            val age = AgeUtils.calculateUpcomingAge(uiState.birthDate)

            // This is the payoff for entering a year: say plainly what it buys.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = SaffronTokens.radiusCard,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Column(modifier = Modifier.padding(SaffronTokens.space20)) {
                    Text(
                        text = "THIS UNLOCKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(SaffronTokens.space4))
                    Text(
                        text = "Turning $age · $zodiac",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(SaffronTokens.space4))
                    Text(
                        text = "Their age and star sign come from this date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/**
 * The wizard's last step: when to remind, and the tone of the message.
 *
 * Matches the approved concept, which frames this step as "make it worth
 * sending" rather than a notifications form. Two things changed as a result:
 * the reminder is one toggle plus three lead-time chips instead of a time
 * picker and four checkboxes, and the tone is chosen here so the card does not
 * have to ask for it on first open.
 *
 * The time picker was not a gratuitous cut -- it moves to the per-person screen,
 * where a person who cares about 9:00 AM can set it without walking the wizard
 * again. Onboarding should not ask for a preference most people will never
 * change, and asking for it here cost a whole step's worth of attention for a
 * field the user had to accept as-is.
 *
 * @param uiState the current wizard state
 * @param viewModel the wizard's view model
 */
@Composable
fun Step3Personalization(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SaffronTokens.space16),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // The reminder toggle, as a card so it reads as the primary thing this
        // step is about rather than as one row in a form.
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remind me",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = ReminderSummary.of(uiState),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::updateNotificationsEnabled,
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                )
            }
        }

        // Lead time. Hidden rather than disabled when reminders are off: a row
        // of unselectable chips reads as broken, and turning reminders back on
        // should not require a second decision about when.
        if (uiState.notificationsEnabled) {
            SectionSubhead("Remind me earlier")
            SaffronChipRow(
                chips = LeadTime.options,
                selected = LeadTime.of(uiState.advanceNotificationDays),
                labelOf = { it.label },
                onSelect = { viewModel.updateAdvanceNotificationDays(it.days) },
            )
        }

        SectionSubhead("Message tone")
        SaffronChipRow(
            chips = CardTone.entries.toList(),
            selected = uiState.cardTone,
            labelOf = { it.label },
            onSelect = viewModel::updateCardTone,
        )

        // Says out loud what the tone control does not, because the one
        // surprising thing about the card is that re-picking a tone never
        // changes its colours.
        Text(
            text = "Only changes the wording. The colours come from ${uiState.name.ifBlank { "their" }} birth date.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * How long before the birthday to remind someone.
 *
 * Days, not a label: the wizard stores an offset in days and the concept's
 * chips are the only place it is chosen, so the mapping lives here and is
 * tested rather than being spelled out at each call site.
 *
 * @property days the offset stored on the birthday
 * @property label what the chip shows
 */
enum class LeadTime(
    val days: Int,
    val label: String,
) {
    ONE_DAY(days = 1, label = "1 day"),
    THREE_DAYS(days = 3, label = "3 days"),
    ONE_WEEK(days = 7, label = "1 week"),
    ;

    companion object {
        val options: List<LeadTime> = entries.toList()

        /**
         * The option matching a stored offset, falling back to the safest one.
         *
         * A birthday saved before this step existed carries 0, meaning "on the
         * day", which is not a chip. Falling back to 3 days would silently
         * change when someone is notified, so this returns 1: the reminder
         * still arrives, just not further ahead than before.
         *
         * @param days the stored advance offset
         */
        fun of(days: Int): LeadTime = entries.firstOrNull { it.days == days } ?: ONE_DAY
    }
}

/**
 * The line under the "Remind me" toggle, describing what is actually set.
 *
 * @param uiState the current wizard state
 */
object ReminderSummary {
    /**
     * @param uiState the current wizard state
     */
    fun of(uiState: AddEditBirthdayUiState): String {
        if (!uiState.notificationsEnabled) return "Off"
        val time = uiState.notificationTime
        val clock = if (time == null) DEFAULT_CLOCK else ReminderClock.format(time)
        // The label for the value actually stored, not the nearest chip. A
        // 5-day offset has no chip but is storable, and a line claiming
        // "1 day" for it would describe a reminder the user did not ask for.
        // Never the enum instance either: that prints "ONE_DAY".
        val lead = LeadTimeChipLabel.forDays(uiState.advanceNotificationDays)
        return "$clock · $lead before"
    }

    /** The app's default reminder time, when nothing has been chosen. */
    const val DEFAULT_CLOCK: String = "9:00 AM"
}

/**
 * Formats a reminder time the way the concept shows it.
 *
 * 12-hour with a space before the meridiem, so "9:00 AM" and not "9:00AM" or
 * "09:00". The app's users are not all in a 24-hour locale, and the am/pm
 * marker is the version that reads correctly to both.
 */
object ReminderClock {
    /**
     * @param time the time of day
     */
    fun format(time: java.time.LocalTime): String {
        val hour24 = time.hour
        val meridiem = if (hour24 < 12) "AM" else "PM"
        val hour12 =
            when (val h = hour24 % 12) {
                0 -> 12
                else -> h
            }
        return "%d:%02d %s".format(hour12, time.minute, meridiem)
    }
}
