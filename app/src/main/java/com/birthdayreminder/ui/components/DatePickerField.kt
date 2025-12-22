package com.birthdayreminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Date picker component for birthday input
 * Satisfies requirement 1.1 (birthday input)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String = "Birthday",
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) ?: "",
            onValueChange = { }, // Read-only field
            label = { Text(label) },
            readOnly = true,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
    
    if (showDatePicker) {
        BirthdayDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { date ->
                onDateSelected(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Date picker dialog specifically for birthdays (past dates only)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
            ?: System.currentTimeMillis()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(date)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Birthday",
                    modifier = Modifier.padding(16.dp)
                )
            },
            dateValidator = { utcTimeMillis ->
                // Only allow past dates and today for birthdays
                utcTimeMillis <= System.currentTimeMillis()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DatePickerFieldPreview() {
    BirthdayReminderAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal state
            DatePickerField(
                selectedDate = LocalDate.of(1990, 5, 15),
                onDateSelected = {},
                label = "Birthday"
            )
            
            // Error state
            DatePickerField(
                selectedDate = null,
                onDateSelected = {},
                label = "Birthday",
                isError = true,
                errorMessage = "Please select a birthday"
            )
        }
    }
}