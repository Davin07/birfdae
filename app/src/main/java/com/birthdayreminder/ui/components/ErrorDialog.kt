package com.birthdayreminder.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.birthdayreminder.domain.error.ErrorResult

/**
 * Centralized error dialog component for displaying errors with retry/cancel options.
 * Provides consistent error handling across the application.
 *
 * @param error The error result containing message and recoverability information
 * @param onRetry Optional callback when user wants to retry the operation
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun ErrorDialog(
    error: ErrorResult,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            if (error.canRetry && onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (error.canRetry && onRetry != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

/**
 * Simplified error dialog for displaying error messages without retry option.
 *
 * @param message The error message to display
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun SimpleErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Error",
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
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}
