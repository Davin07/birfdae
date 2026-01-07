package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
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

            try {
                // Check permission first if notifications are requested
                if (notificationsEnabled && !scheduleNotificationUseCase.canScheduleExactAlarms()) {
                    return AddBirthdayResult.ExactAlarmPermissionNotGranted
                }

                val birthday =
                    Birthday(
                        name = name,
                        birthDate = birthDate,
                        notes = notes,
                        notificationsEnabled = notificationsEnabled,
                        advanceNotificationDays = advanceNotificationDays,
                        notificationHour = notificationHour,
                        notificationMinute = notificationMinute,
                    )

                val birthdayId = birthdayRepository.addBirthday(birthday)

                // Schedule notifications if enabled
                if (notificationsEnabled) {
                    val savedBirthday = birthday.copy(id = birthdayId)
                    val notificationResult = scheduleNotificationUseCase.scheduleNotification(savedBirthday)
                    if (notificationResult is ScheduleNotificationResult.ExactAlarmPermissionNotGranted) {
                        // This technically shouldn't happen if the check above passes,
                        // but handle it just in case permissions changed in the milliseconds between check and schedule
                        return AddBirthdayResult.ExactAlarmPermissionNotGranted
                    }
                }

                return AddBirthdayResult.Success(birthdayId)
            } catch (e: Exception) {
                return AddBirthdayResult.DatabaseError(e)
            }
        }
    }

/**
 * Result of adding a birthday operation.
 */
sealed class AddBirthdayResult {
    data class Success(val birthdayId: Long) : AddBirthdayResult()

    data class ValidationError(val errors: List<String>) : AddBirthdayResult()

    data class DatabaseError(val exception: Throwable) : AddBirthdayResult()

    object ExactAlarmPermissionNotGranted : AddBirthdayResult()
}
