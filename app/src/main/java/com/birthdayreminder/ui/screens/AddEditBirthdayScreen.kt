package com.birthdayreminder.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.birthdayreminder.domain.util.AgeUtils
import com.birthdayreminder.domain.util.ZodiacUtils
import com.birthdayreminder.ui.components.DatePickerField
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.components.NotificationTimePicker
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayViewModel
import java.time.LocalTime

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Birthday" else "Add Birthday",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.step > 1) {
                            viewModel.previousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (uiState.step < 3) {
                        viewModel.nextStep()
                    } else {
                        viewModel.saveBirthday()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = canProceed(uiState)
            ) {
                Text(if (uiState.step < 3) "Next" else "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Step ${uiState.step} of 3",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            when (uiState.step) {
                1 -> Step1Identity(uiState, viewModel)
                2 -> Step2Date(uiState, viewModel)
                3 -> Step3Personalization(uiState, viewModel)
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
    viewModel: AddEditBirthdayViewModel
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateImageUri(uri.toString())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Image Picker
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.imageUri != null) {
                AsyncImage(
                    model = uiState.imageUri,
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "Add Photo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } }
        )

        // Relationship
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Relationship",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Wrap flow needed for many items
            ) {
                // Using simple Row for now, might need FlowRow if many items
                val relationships = listOf("Family", "Friend", "Work", "Other")
                relationships.forEach { rel ->
                    FilterChip(
                        selected = uiState.relationship == rel,
                        onClick = { viewModel.updateRelationship(rel) },
                        label = { Text(rel) }
                    )
                }
            }
        }
    }
}

@Composable
fun Step2Date(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "When is their birthday?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        DatePickerField(
            selectedDate = uiState.birthDate,
            onDateSelected = { viewModel.updateBirthDate(it) },
            label = "Birth Date",
            isError = uiState.birthDateError != null,
            errorMessage = uiState.birthDateError,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.birthDate != null) {
            val zodiac = ZodiacUtils.getZodiacSign(uiState.birthDate.month, uiState.birthDate.dayOfMonth)
            val age = AgeUtils.calculateUpcomingAge(uiState.birthDate)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LuminaGlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Zodiac", style = MaterialTheme.typography.labelMedium)
                        Text(zodiac, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                LuminaGlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Turning", style = MaterialTheme.typography.labelMedium)
                        Text("$age", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun Step3Personalization(
    uiState: AddEditBirthdayUiState,
    viewModel: AddEditBirthdayViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Notes
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = { viewModel.updateNotes(it) },
            label = { Text("Notes (Optional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Notifications
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium
            )
            
            val options = listOf(
                0 to "On the day",
                1 to "1 day before",
                3 to "3 days before",
                7 to "1 week before"
            )
            
            options.forEach { (days, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
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
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = uiState.notificationOffsets.contains(days),
                        onCheckedChange = null // Handled by Row click
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Time Picker
        NotificationTimePicker(
            hour = uiState.notificationTime?.hour ?: 9,
            minute = uiState.notificationTime?.minute ?: 0,
            onTimeChange = { h, m ->
                viewModel.updateNotificationTime(LocalTime.of(h, m))
            }
        )

        // Preview
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium
            )
            
            LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Birthday Reminder",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "It's ${uiState.name.ifBlank { "Friend" }}'s birthday today!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
