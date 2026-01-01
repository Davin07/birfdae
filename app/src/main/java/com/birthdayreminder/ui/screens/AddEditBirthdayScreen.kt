package com.birthdayreminder.ui.screens

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.error.ErrorResult
import com.birthdayreminder.ui.components.DatePickerField
import com.birthdayreminder.ui.components.NotificationTimePicker
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayViewModel
import java.time.LocalDate

/**
 * Screen for adding new birthdays or editing existing ones.
 * Satisfies requirements 1.1, 1.2, 1.3, 1.4, and 6.5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBirthdayScreen(
    birthdayId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddEditBirthdayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize form when screen loads
    LaunchedEffect(birthdayId) {
        viewModel.initializeForm(birthdayId)
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Birthday" else "Add Birthday",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveBirthday() },
                        enabled = uiState.canSave,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Save")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            AddEditBirthdayForm(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onBirthDateChange = viewModel::updateBirthDate,
                onNotesChange = viewModel::updateNotes,
                onNotificationsToggle = viewModel::toggleNotifications,
                onAdvanceNotificationDaysChange = viewModel::updateAdvanceNotificationDays,
                onNotificationHourChange = viewModel::updateNotificationHour,
                onNotificationMinuteChange = viewModel::updateNotificationMinute,
                onErrorDismiss = viewModel::clearError,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            )
        }
    }
}

/**
 * Form content for adding/editing birthdays.
 */
@Composable
private fun AddEditBirthdayForm(
    uiState: AddEditBirthdayUiState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onNotesChange: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onAdvanceNotificationDaysChange: (Int) -> Unit,
    onNotificationHourChange: (Int) -> Unit,
    onNotificationMinuteChange: (Int) -> Unit,
    onErrorDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Error message display
        uiState.errorMessage?.let { errorMessage ->
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onErrorDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Name input field
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Name *") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Birth date picker
        DatePickerField(
            selectedDate = uiState.birthDate,
            onDateSelected = onBirthDateChange,
            label = "Birth Date *",
            isError = uiState.birthDateError != null,
            errorMessage = uiState.birthDateError,
            modifier = Modifier.fillMaxWidth(),
        )

        // Notes input field
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            label = { Text("Notes (optional)") },
            isError = uiState.notesError != null,
            supportingText = uiState.notesError?.let { { Text(it) } },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        // Notification settings section
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                // Enable notifications toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Get reminded about this birthday",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                    )
                }

                // Notification time picker (only shown if notifications are enabled)
                if (uiState.notificationsEnabled) {
                    Divider()

                    NotificationTimePicker(
                        hour = uiState.notificationHour,
                        minute = uiState.notificationMinute,
                        onTimeChange = { hour, minute ->
                            onNotificationHourChange(hour)
                            onNotificationMinuteChange(minute)
                        },
                    )
                }

                // Advance notification options (only shown if notifications are enabled)
                if (uiState.notificationsEnabled) {
                    Divider()

                    Text(
                        text = "Advance Notification",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = "When would you like to be reminded?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val notificationOptions =
                        listOf(
                            0 to "On the day",
                            1 to "1 day before",
                            3 to "3 days before",
                            7 to "1 week before",
                        )

                    notificationOptions.forEach { (days, label) ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.advanceNotificationDays == days,
                                        onClick = { onAdvanceNotificationDaysChange(days) },
                                        role = Role.RadioButton,
                                    )
                                    .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = uiState.advanceNotificationDays == days,
                                // handled by selectable modifier
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }

        // Required fields note
        Text(
            text = "* Required fields",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Add some bottom padding for better scrolling experience
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun addEditBirthdayScreenPreview() {
    BirthdayReminderAppTheme {
        AddEditBirthdayForm(
            uiState =
                AddEditBirthdayUiState(
                    name = "John Doe",
                    birthDate = LocalDate.of(1990, 5, 15),
                    notes = "Best friend from college",
                    notificationsEnabled = true,
                    advanceNotificationDays = 1,
                    notificationHour = 9,
                    notificationMinute = 30,
                ),
            onNameChange = {},
            onBirthDateChange = {},
            onNotesChange = {},
            onNotificationsToggle = {},
            onAdvanceNotificationDaysChange = {},
            onNotificationHourChange = {},
            onNotificationMinuteChange = {},
            onErrorDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun addEditBirthdayScreenErrorPreview() {
    BirthdayReminderAppTheme {
        AddEditBirthdayForm(
            uiState =
                AddEditBirthdayUiState(
                    name = "",
                    nameError = "Name is required",
                    birthDateError = "Birth date cannot be in the future",
                    errorResult =
                        ErrorResult(
                            message = "Please fix the errors above",
                            isRecoverable = true,
                        ),
                    notificationsEnabled = false,
                ),
            onNameChange = {},
            onBirthDateChange = {},
            onNotesChange = {},
            onNotificationsToggle = {},
            onAdvanceNotificationDaysChange = {},
            onNotificationHourChange = {},
            onNotificationMinuteChange = {},
            onErrorDismiss = {},
        )
    }
}
