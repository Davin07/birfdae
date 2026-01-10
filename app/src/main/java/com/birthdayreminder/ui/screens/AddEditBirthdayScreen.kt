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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.util.AgeUtils
import com.birthdayreminder.domain.util.ZodiacUtils
import com.birthdayreminder.ui.components.LuminaAvatarPicker
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaChip
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.components.LuminaHeader
import com.birthdayreminder.ui.components.LuminaTextField
import com.birthdayreminder.ui.components.NotificationTimePicker
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

    LuminaBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LuminaHeader(
                title = if (uiState.isEditMode) "Edit Birthday" else "Add Birthday",
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
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
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
                                else -> "Notify"
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
                        ).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
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
                Button(
                    onClick = {
                        if (uiState.step < 3) {
                            viewModel.nextStep()
                        } else {
                            viewModel.saveBirthday()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canProceed(uiState),
                ) {
                    Text(if (uiState.step < 3) "Next" else "Save")
                }
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
            LuminaAvatarPicker(
                imageUri = uiState.imageUri,
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )
            Text(
                text = "Upload Photo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Inputs
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            LuminaTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = "Name",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
            )

            // Relationship
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RELATIONSHIP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
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
                        LuminaChip(
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
        LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
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
        LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LuminaGlassCard(modifier = Modifier.weight(1f).height(90.dp)) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "ZODIAC",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            zodiac,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                LuminaGlassCard(modifier = Modifier.weight(1f).height(90.dp)) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "TURNING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "$age",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
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
        LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
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
            LuminaTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = "Gift ideas, preferences...",
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
