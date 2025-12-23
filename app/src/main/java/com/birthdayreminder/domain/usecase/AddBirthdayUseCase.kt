package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.validation.BirthdayValidator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for adding new birthday entries with validation.
 * Handles input validation and business rules for birthday creation.
 */
@Singleton
class AddBirthdayUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
        private val birthdayValidator: BirthdayValidator,
        private val errorHandler: ErrorHandler,
    ) {
        /**
         * Adds a new birthday after validation.
         *
         * @param name The name of the person (required)
         * @param birthDate The birth date (required, cannot be in future)
         * @param notes Optional notes about the birthday
         * @param notificationsEnabled Whether notifications are enabled for this birthday
         * @param advanceNotificationDays Days before birthday to send notification (0, 1, 3, or 7)
         * @param notificationHour Hour of day to send notifications (0-23)
         * @param notificationMinute Minute of hour to send notifications (0-59)
         * @return Result containing the new birthday ID or validation errors
         */
        suspend fun addBirthday(
            name: String,
            birthDate: LocalDate,
            notes: String? = null,
            notificationsEnabled: Boolean = true,
            advanceNotificationDays: Int = 0,
            notificationHour: Int? = null,
            notificationMinute: Int? = null,
        ): AddBirthdayResult {
            // Validate input using centralized validator
            val validationResult =
                birthdayValidator.validateBirthday(
                    name = name,
                    birthDate = birthDate,
                    notes = notes,
                    advanceNotificationDays = advanceNotificationDays,
                    notificationHour = notificationHour,
                    notificationMinute = notificationMinute,
                )

            if (validationResult.isInvalid) {
                return AddBirthdayResult.ValidationError(validationResult.errorMessages)
            }

            return try {
                val birthday =
                    Birthday(
                        name = birthdayValidator.sanitizeName(name),
                        birthDate = birthDate,
                        notes = birthdayValidator.sanitizeNotes(notes),
                        notificationsEnabled = notificationsEnabled,
                        advanceNotificationDays = advanceNotificationDays,
                        notificationHour = notificationHour,
                        notificationMinute = notificationMinute,
                        createdAt = java.time.Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    )

                val birthdayId = birthdayRepository.addBirthday(birthday)

                // Schedule notifications if enabled
                if (notificationsEnabled) {
                    val savedBirthday = birthday.copy(id = birthdayId)
                    scheduleNotificationUseCase(savedBirthday)
                }

                AddBirthdayResult.Success(birthdayId)
            } catch (e: Exception) {
                val errorMessage = errorHandler.handleDatabaseError(e)
                AddBirthdayResult.DatabaseError(errorMessage)
            }
        }
    }

/**
 * Result of adding a birthday operation.
 */
sealed class AddBirthdayResult {
    data class Success(val birthdayId: Long) : AddBirthdayResult()

    data class ValidationError(val errors: List<String>) : AddBirthdayResult()

    data class DatabaseError(val message: String) : AddBirthdayResult()
}
