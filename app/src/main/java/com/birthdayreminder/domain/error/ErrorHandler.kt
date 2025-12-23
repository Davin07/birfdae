package com.birthdayreminder.domain.error

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import java.io.IOException
import java.time.DateTimeException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handling utility for the application.
 * Converts technical exceptions into user-friendly error messages.
 */
@Singleton
class ErrorHandler
    @Inject
    constructor() {
        companion object {
            // Database error messages
            const val ERROR_DATABASE_UNAVAILABLE = "Database is temporarily unavailable. Please try again."
            const val ERROR_DATABASE_CONSTRAINT = "This operation violates data constraints. Please check your input."
            const val ERROR_DATABASE_GENERIC = "A database error occurred. Please try again."

            // Network error messages (for future use)
            const val ERROR_NETWORK_UNAVAILABLE = "Network is unavailable. Please check your connection."
            const val ERROR_NETWORK_TIMEOUT = "Request timed out. Please try again."

            // Date calculation error messages
            const val ERROR_DATE_CALCULATION = "Unable to calculate date. Using fallback value."
            const val ERROR_DATE_INVALID = "Invalid date provided. Please check the date format."

            // General error messages
            const val ERROR_UNKNOWN = "An unexpected error occurred. Please try again."
            const val ERROR_OPERATION_FAILED = "Operation failed. Please try again."
        }

        /**
         * Handles database-related exceptions and returns user-friendly error messages.
         *
         * @param exception The exception to handle
         * @return User-friendly error message
         */
        fun handleDatabaseError(exception: Throwable): String {
            return when (exception) {
                is SQLiteConstraintException -> ERROR_DATABASE_CONSTRAINT
                is SQLiteException -> {
                    when {
                        exception.message?.contains("database is locked", ignoreCase = true) == true ->
                            "Database is busy. Please try again in a moment."
                        exception.message?.contains("no such table", ignoreCase = true) == true ->
                            "Database structure error. Please restart the app."
                        exception.message?.contains("disk I/O error", ignoreCase = true) == true ->
                            "Storage error. Please check available space and try again."
                        else -> ERROR_DATABASE_GENERIC
                    }
                }
                is IOException -> "Storage access error. Please check permissions and try again."
                else -> exception.message ?: ERROR_DATABASE_GENERIC
            }
        }

        /**
         * Handles date calculation exceptions and provides fallback behavior.
         *
         * @param exception The exception to handle
         * @return User-friendly error message
         */
        fun handleDateCalculationError(exception: Throwable): String {
            return when (exception) {
                is DateTimeException -> ERROR_DATE_INVALID
                is ArithmeticException -> "Date calculation overflow. Please check the date range."
                else -> ERROR_DATE_CALCULATION
            }
        }

        /**
         * Handles general exceptions and returns appropriate error messages.
         *
         * @param exception The exception to handle
         * @param operation Optional description of the operation that failed
         * @return User-friendly error message
         */
        fun handleGenericError(
            exception: Throwable,
            operation: String? = null,
        ): String {
            val baseMessage =
                when (exception) {
                    is IllegalArgumentException -> "Invalid input provided. Please check your data."
                    is IllegalStateException -> "Operation cannot be performed at this time. Please try again."
                    is SecurityException -> "Permission denied. Please check app permissions."
                    is OutOfMemoryError -> "Not enough memory available. Please close other apps and try again."
                    else -> exception.message ?: ERROR_UNKNOWN
                }

            return if (operation != null) {
                "Failed to $operation: $baseMessage"
            } else {
                baseMessage
            }
        }

        /**
         * Determines if an error is recoverable (user can retry) or not.
         *
         * @param exception The exception to analyze
         * @return True if the operation can be retried, false otherwise
         */
        fun isRecoverableError(exception: Throwable): Boolean {
            return when (exception) {
                is SQLiteException -> {
                    // Database locked or I/O errors are usually temporary
                    exception.message?.contains("database is locked", ignoreCase = true) == true ||
                        exception.message?.contains("disk I/O error", ignoreCase = true) == true
                }
                is IOException -> true // File I/O errors are often temporary
                is OutOfMemoryError -> false // Memory errors require app restart
                is SecurityException -> false // Permission errors need user intervention
                is IllegalArgumentException -> false // Invalid input needs correction
                else -> true // Most other errors can be retried
            }
        }

        /**
         * Creates a standardized error result for operations.
         *
         * @param exception The exception that occurred
         * @param operation Description of the operation that failed
         * @return Standardized error result
         */
        fun createErrorResult(
            exception: Throwable,
            operation: String,
        ): ErrorResult {
            val message =
                when {
                    isDatabaseError(exception) -> handleDatabaseError(exception)
                    isDateCalculationError(exception) -> handleDateCalculationError(exception)
                    else -> handleGenericError(exception, operation)
                }

            return ErrorResult(
                message = message,
                isRecoverable = isRecoverableError(exception),
                originalException = exception,
            )
        }

        /**
         * Checks if an exception is database-related.
         */
        private fun isDatabaseError(exception: Throwable): Boolean {
            return exception is SQLiteException || exception is IOException
        }

        /**
         * Checks if an exception is date calculation-related.
         */
        private fun isDateCalculationError(exception: Throwable): Boolean {
            return exception is DateTimeException || exception is ArithmeticException
        }
    }

/**
 * Standardized error result containing user-friendly information.
 */
data class ErrorResult(
    val message: String,
    val isRecoverable: Boolean,
    val originalException: Throwable? = null,
) {
    /**
     * Returns true if the user should be offered a retry option.
     */
    val canRetry: Boolean get() = isRecoverable

    /**
     * Returns the technical error message for logging purposes.
     */
    val technicalMessage: String get() = originalException?.message ?: message
}
