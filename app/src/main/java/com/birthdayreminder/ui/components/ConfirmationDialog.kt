package com.birthdayreminder.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme

/**
 * Reusable confirmation dialog component for deletions and other destructive actions
 * Satisfies requirement 1.5 (confirmation before deletion)
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = confirmButtonText,
                    color =
                        if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        },
    )
}

/**
 * Specialized confirmation dialog for birthday deletion
 * Satisfies requirement 1.5 (confirmation before deletion)
 */
@Composable
fun DeleteBirthdayDialog(
    birthdayName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = "Delete Birthday",
        message = "Are you sure you want to delete $birthdayName's birthday? This action cannot be undone.",
        confirmButtonText = "Delete",
        dismissButtonText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDestructive = true,
    )
}

/**
 * Generic confirmation dialog for unsaved changes
 */
@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Unsaved Changes",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. What would you like to do?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = "Discard",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun confirmationDialogPreview() {
    BirthdayReminderAppTheme {
        DeleteBirthdayDialog(
            birthdayName = "John Doe",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun unsavedChangesDialogPreview() {
    BirthdayReminderAppTheme {
        UnsavedChangesDialog(
            onSave = {},
            onDiscard = {},
            onCancel = {},
        )
    }
}
