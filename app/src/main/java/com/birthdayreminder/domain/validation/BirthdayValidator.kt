package com.birthdayreminder.domain.validation

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized validation utility for birthday-related data.
 * Provides comprehensive input validation with detailed error messages.
 */
@Singleton
class BirthdayValidator
    @Inject
    constructor() {
        companion object {
            const val MAX_NAME_LENGTH = 100
            const val MAX_NOTES_LENGTH = 500
            val VALID_ADVANCE_NOTIFICATION_DAYS = listOf(0, 1, 3, 7)
            const val MIN_NOTIFICATION_HOUR = 0
            const val MAX_NOTIFICATION_HOUR = 23
            const val MIN_NOTIFICATION_MINUTE = 0
            const val MAX_NOTIFICATION_MINUTE = 59

            // Error message constants
            const val ERROR_NAME_REQUIRED = "Name is required"
            const val ERROR_NAME_TOO_LONG = "Name must be $MAX_NAME_LENGTH characters or less"
            const val ERROR_NAME_INVALID_CHARACTERS = "Name contains invalid characters"
            const val ERROR_BIRTH_DATE_REQUIRED = "Birth date is required"
            const val ERROR_BIRTH_DATE_FUTURE = "Birth date cannot be in the future"
            const val ERROR_BIRTH_DATE_TOO_OLD = "Birth date cannot be more than 150 years ago"
            const val ERROR_NOTES_TOO_LONG = "Notes must be $MAX_NOTES_LENGTH characters or less"
            const val ERROR_INVALID_ADVANCE_DAYS = "Advance notification days must be 0, 1, 3, or 7"
            const val ERROR_INVALID_NOTIFICATION_HOUR =
                "Notification hour must be between $MIN_NOTIFICATION_HOUR and $MAX_NOTIFICATION_HOUR"
            const val ERROR_INVALID_NOTIFICATION_MINUTE =
                "Notification minute must be between $MIN_NOTIFICATION_MINUTE and $MAX_NOTIFICATION_MINUTE"
        }

        /**
         * Validates a complete birthday entry.
         *
         * @param name The person's name
         * @param birthDate The birth date
         * @param notes Optional notes
         * @param advanceNotificationDays Days before birthday to send notification
         * @param notificationHour Hour of day to send notifications (0-23)
         * @param notificationMinute Minute of hour to send notifications (0-59)
         * @return ValidationResult with success or list of errors
         */
        fun validateBirthday(
            name: String?,
            birthDate: LocalDate?,
            notes: String? = null,
            advanceNotificationDays: Int = 0,
            notificationHour: Int? = null,
            notificationMinute: Int? = null,
        ): ValidationResult {
            val errors = mutableListOf<String>()

            // Validate name
            validateName(name)?.let { errors.add(it) }

            // Validate birth date
            validateBirthDate(birthDate)?.let { errors.add(it) }

            // Validate notes
            validateNotes(notes)?.let { errors.add(it) }

            // Validate advance notification days
            validateAdvanceNotificationDays(advanceNotificationDays)?.let { errors.add(it) }

            // Validate notification hour
            validateNotificationHour(notificationHour)?.let { errors.add(it) }

            // Validate notification minute
            validateNotificationMinute(notificationMinute)?.let { errors.add(it) }

            return if (errors.isEmpty()) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid(errors)
            }
        }

        /**
         * Validates a person's name.
         *
         * @param name The name to validate
         * @return Error message if invalid, null if valid
         */
        fun validateName(name: String?): String? {
            return when {
                name.isNullOrBlank() -> ERROR_NAME_REQUIRED
                name.trim().length > MAX_NAME_LENGTH -> ERROR_NAME_TOO_LONG
                containsInvalidCharacters(name.trim()) -> ERROR_NAME_INVALID_CHARACTERS
                else -> null
            }
        }

        /**
         * Validates a birth date.
         *
         * @param birthDate The birth date to validate
         * @return Error message if invalid, null if valid
         */
        fun validateBirthDate(birthDate: LocalDate?): String? {
            return when {
                birthDate == null -> ERROR_BIRTH_DATE_REQUIRED
                birthDate.isAfter(LocalDate.now()) -> ERROR_BIRTH_DATE_FUTURE
                birthDate.isBefore(LocalDate.now().minusYears(150)) -> ERROR_BIRTH_DATE_TOO_OLD
                else -> null
            }
        }

        /**
         * Validates notes text.
         *
         * @param notes The notes to validate
         * @return Error message if invalid, null if valid
         */
        fun validateNotes(notes: String?): String? {
            return when {
                notes != null && notes.length > MAX_NOTES_LENGTH -> ERROR_NOTES_TOO_LONG
                else -> null
            }
        }

        /**
         * Validates advance notification days setting.
         *
         * @param days The number of days to validate
         * @return Error message if invalid, null if valid
         */
        fun validateAdvanceNotificationDays(days: Int): String? {
            return if (days !in VALID_ADVANCE_NOTIFICATION_DAYS) {
                ERROR_INVALID_ADVANCE_DAYS
            } else {
                null
            }
        }

        /**
         * Validates notification hour setting.
         *
         * @param hour The hour to validate (0-23)
         * @return Error message if invalid, null if valid
         */
        fun validateNotificationHour(hour: Int?): String? {
            return if (hour != null && (hour < MIN_NOTIFICATION_HOUR || hour > MAX_NOTIFICATION_HOUR)) {
                ERROR_INVALID_NOTIFICATION_HOUR
            } else {
                null
            }
        }

        /**
         * Validates notification minute setting.
         *
         * @param minute The minute to validate (0-59)
         * @return Error message if invalid, null if valid
         */
        fun validateNotificationMinute(minute: Int?): String? {
            return if (minute != null && (minute < MIN_NOTIFICATION_MINUTE || minute > MAX_NOTIFICATION_MINUTE)) {
                ERROR_INVALID_NOTIFICATION_MINUTE
            } else {
                null
            }
        }

        /**
         * Checks if a name contains invalid characters.
         * Currently allows letters, numbers, spaces, hyphens, apostrophes, and periods.
         *
         * @param name The name to check
         * @return True if contains invalid characters, false otherwise
         */
        private fun containsInvalidCharacters(name: String): Boolean {
            val validPattern = Regex("^[a-zA-Z0-9\\s\\-'.]+$")
            return !validPattern.matches(name)
        }

        /**
         * Sanitizes a name by trimming whitespace and removing extra spaces.
         *
         * @param name The name to sanitize
         * @return Sanitized name
         */
        fun sanitizeName(name: String): String {
            return name.trim().replace(Regex("\\s+"), " ")
        }

        /**
         * Sanitizes notes by trimming whitespace.
         *
         * @param notes The notes to sanitize
         * @return Sanitized notes or null if empty
         */
        fun sanitizeNotes(notes: String?): String? {
            return notes?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

/**
 * Result of validation operations.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()

    data class Invalid(val errors: List<String>) : ValidationResult()

    /**
     * Returns true if validation passed.
     */
    val isValid: Boolean get() = this is Valid

    /**
     * Returns true if validation failed.
     */
    val isInvalid: Boolean get() = this is Invalid

    /**
     * Gets the error messages if validation failed.
     */
    val errorMessages: List<String>
        get() =
            when (this) {
                is Invalid -> errors
                is Valid -> emptyList()
            }
}
