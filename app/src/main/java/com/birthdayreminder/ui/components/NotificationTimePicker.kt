package com.birthdayreminder.ui.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme

/**
 * Component for selecting notification time using Android's built-in time picker dialog.
 *
 * @param hour The selected hour (0-23)
 * @param minute The selected minute (0-59)
 * @param onTimeChange Callback when time is changed
 * @param modifier Modifier for the component
 */
@Composable
fun NotificationTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Notification Time",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = "Set the time of day for birthday notifications",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Time display that opens the time picker when clicked
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    text = "Time",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = formatTime(hour, minute),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        // Hidden TimePickerDialog that appears when needed
        if (showTimePicker) {
            AndroidTimePickerDialog(
                initialHour = hour,
                initialMinute = minute,
                onTimeSet = { newHour, newMinute ->
                    onTimeChange(newHour, newMinute)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false },
                context = context,
            )
        }
    }
}

/**
 * Android TimePickerDialog wrapper
 */
@Composable
private fun AndroidTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSet: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    context: Context,
) {
    val timePickerDialog =
        remember(initialHour, initialMinute) {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    onTimeSet(hourOfDay, minute)
                },
                initialHour,
                initialMinute,
                // Use 24-hour format based on system settings
                android.text.format.DateFormat.is24HourFormat(context),
            )
        }

    DisposableEffect(timePickerDialog) {
        timePickerDialog.setOnDismissListener { onDismiss() }
        timePickerDialog.show()

        onDispose {
            timePickerDialog.dismiss()
        }
    }
}

/**
 * Format time as HH:MM string
 */
private fun formatTime(
    hour: Int,
    minute: Int,
): String {
    return String.format("%02d:%02d", hour, minute)
}

@Preview(showBackground = true)
@Composable
fun notificationTimePickerPreview() {
    BirthdayReminderAppTheme {
        NotificationTimePicker(
            hour = 9,
            minute = 30,
            onTimeChange = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun notificationTimePickerEveningPreview() {
    BirthdayReminderAppTheme {
        NotificationTimePicker(
            hour = 20,
            minute = 0,
            onTimeChange = { _, _ -> },
        )
    }
}
