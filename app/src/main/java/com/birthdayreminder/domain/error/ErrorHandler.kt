package com.birthdayreminder.domain.error

import java.io.IOException
import java.sql.SQLException
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
            /** Error message constants for database-related errors. */
            const val ERROR_DATABASE_UNAVAILABLE = "Database is temporarily unavailable. Please try again."
            const val ERROR_DATABASE_CONSTRAINT = "This operation violates data constraints. Please check your input."
            const val ERROR_DATABASE_GENERIC = "A database error occurred. Please try again."
            const val ERROR_NETWORK_UNAVAILABLE = "Network is unavailable. Please check your connection."
            const val ERROR_NETWORK_TIMEOUT = "Request timed out. Please try again."
            const val ERROR_DATE_CALCULATION = "Unable to calculate date. Using fallback value."
            const val ERROR_DATE_INVALID = "Invalid date provided. Please check the date format."
            const val ERROR_UNKNOWN = "An unexpected error occurred. Please try again."
            const val ERROR_OPERATION_FAILED = "Operation failed. Please try again."
        }

        /**
         * Handles database-related exceptions and returns user-friendly error messages.
         *
         * @param exception The database exception to handle
         * @return A user-friendly error message appropriate for the exception type
         */
        fun handleDatabaseError(exception: Throwable): String {
            return when (exception) {
                is android.database.sqlite.SQLiteConstraintException -> ERROR_DATABASE_CONSTRAINT
                is android.database.sqlite.SQLiteException -> {
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
                is SQLException -> {
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
         * Handles date calculation-related exceptions and returns user-friendly error messages.
         *
         * @param exception The date calculation exception to handle
         * @return A user-friendly error message appropriate for the exception type
         */
        fun handleDateCalculationError(exception: Throwable): String {
            return when (exception) {
                is DateTimeException -> ERROR_DATE_INVALID
                is ArithmeticException -> "Date calculation overflow. Please check the date range."
                else -> ERROR_DATE_CALCULATION
            }
        }

        /**
         * Handles generic exceptions and returns user-friendly error messages.
         * Optionally prepends the operation name to the error message.
         *
         * @param exception The exception to handle
         * @param operation Optional operation name to include in the error message
         * @return A user-friendly error message
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
         * Determines if an exception represents a recoverable error that can be retried.
         *
         * @param exception The exception to evaluate
         * @return True if the error is recoverable and operation can be retried
         */
        fun isRecoverableError(exception: Throwable): Boolean {
            return when (exception) {
                is SQLException -> {
                    exception.message?.contains("database is locked", ignoreCase = true) == true ||
                        exception.message?.contains("disk I/O error", ignoreCase = true) == true
                }
                is IOException -> true
                is DateTimeException -> false
                is OutOfMemoryError -> false
                is SecurityException -> false
                is IllegalArgumentException -> false
                else -> true
            }
        }

        /**
         * Creates an ErrorResult from an exception with appropriate user-friendly messaging.
         *
         * @param exception The exception that occurred
         * @param operation The name of the operation that failed
         * @return An ErrorResult containing user-friendly message and recovery information
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
         * Checks if the exception is a database-related error.
         */
        private fun isDatabaseError(exception: Throwable): Boolean {
            return exception is android.database.sqlite.SQLiteException ||
                exception is SQLException ||
                exception is IOException
        }

        /**
         * Checks if the exception is a date calculation-related error.
         */
        private fun isDateCalculationError(exception: Throwable): Boolean {
            return exception is DateTimeException || exception is ArithmeticException
        }
    }

/**
 * Represents the result of an error occurrence, containing user-friendly messaging
 * and recovery information.
 */
data class ErrorResult(
    val message: String,
    val isRecoverable: Boolean,
    val originalException: Throwable? = null,
) {
    val canRetry: Boolean get() = isRecoverable
    val technicalMessage: String get() = originalException?.message ?: message
}
