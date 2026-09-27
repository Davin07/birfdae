package com.birthdayreminder.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.util.AgeUtils
import com.birthdayreminder.domain.util.ZodiacUtils
import com.birthdayreminder.ui.components.NotificationTimePicker
import com.birthdayreminder.ui.components.birfdae.SaffronAvatarPicker
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronChip
import com.birthdayreminder.ui.components.birfdae.SaffronTextField
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
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
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
            ) {
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
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val relationships = listOf("Family", "Friend", "Work", "Acquaintance", "Other")
                    relationships.forEach { rel ->
                        SaffronChip(
                            selected = uiState.relationship == rel,
                            onClick = { viewModel.updateRelationship(rel) },
                            label = rel,
                        )
                    }
                }
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

@Composable
fun Step3Personalization(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Notification Preview Card
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Birf Dae",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "now",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎉 Birthday Today!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        if (uiState.birthDate != null) {
                            val age = AgeUtils.calculateUpcomingAge(uiState.birthDate)
                            "${uiState.name.ifBlank { "Friend" }} is turning $age today!"
                        } else {
                            "It's ${uiState.name.ifBlank { "Friend" }}'s birthday today!"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Pin Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Pin to Top",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Show this birthday as the hero card",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = uiState.isPinned,
                onCheckedChange = { viewModel.updateIsPinned(it) },
            )
        }

        // Notification Settings
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            NotificationTimePicker(
                hour = uiState.notificationTime?.hour ?: 9,
                minute = uiState.notificationTime?.minute ?: 0,
                onTimeChange = { h, m ->
                    viewModel.updateNotificationTime(LocalTime.of(h, m))
                },
            )

            val options =
                listOf(
                    0 to "On the day",
                    1 to "1 day before",
                    3 to "3 days before",
                    7 to "1 week before",
                )

            options.forEach { (days, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newOffsets = uiState.notificationOffsets.toMutableList()
                                if (newOffsets.contains(days)) {
                                    newOffsets.remove(days)
                                } else {
                                    newOffsets.add(days)
                                }
                                viewModel.updateNotificationOffsets(newOffsets)
                            }
                            .padding(vertical = 8.dp),
                ) {
                    Checkbox(
                        checked = uiState.notificationOffsets.contains(days),
                        onCheckedChange = null,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Notes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "NOTES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SaffronTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = "Notes",
                placeholder = "Gift ideas, preferences...",
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
