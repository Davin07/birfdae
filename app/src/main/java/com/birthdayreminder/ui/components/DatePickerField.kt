package com.birthdayreminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) ?: "",
            // Read-only field
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                    )
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
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
            onDismiss = { showDatePicker = false },
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
    onDismiss: () -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                    ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
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
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Birthday",
                    modifier = Modifier.padding(16.dp),
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun datePickerFieldPreview() {
    BirthdayReminderAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Normal state
            DatePickerField(
                selectedDate = LocalDate.of(1990, 5, 15),
                onDateSelected = {},
                label = "Birthday",
            )

            // Error state
            DatePickerField(
                selectedDate = null,
                onDateSelected = {},
                label = "Birthday",
                isError = true,
                errorMessage = "Please select a birthday",
            )
        }
    }
}