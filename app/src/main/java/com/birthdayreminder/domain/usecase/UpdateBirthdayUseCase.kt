package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.validation.BirthdayValidator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for updating existing birthday entries with validation.
 * Handles input validation and business rules for birthday updates.
 */
@Singleton
class UpdateBirthdayUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
        private val cancelNotificationUseCase: CancelNotificationUseCase,
        private val birthdayValidator: BirthdayValidator,
    ) {
        /**
         * Updates an existing birthday after validation.
         *
         * @param birthdayId The ID of the birthday to update
         * @param name The updated name of the person (required)
         * @param birthDate The updated birth date (required, cannot be in future)
         * @param notes Updated optional notes about the birthday
         * @param notificationsEnabled Whether notifications are enabled for this birthday
         * @param advanceNotificationDays Days before birthday to send notification (0, 1, 3, or 7)
         * @param notificationHour Hour of day to send notifications (0-23)
         * @param notificationMinute Minute of hour to send notifications (0-59)
         * @return Result indicating success or failure with details
         */
        suspend fun updateBirthday(
            birthdayId: Long,
            name: String,
            birthDate: LocalDate,
            notes: String? = null,
            notificationsEnabled: Boolean = true,
            advanceNotificationDays: Int = 0,
            notificationHour: Int? = null,
            notificationMinute: Int? = null,
        ): UpdateBirthdayResult {
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
                return UpdateBirthdayResult.ValidationError(validationResult.errorMessages)
            }

            try {
                // Check permission first if notifications are requested
                if (notificationsEnabled && !scheduleNotificationUseCase.canScheduleExactAlarms()) {
                    return UpdateBirthdayResult.ExactAlarmPermissionNotGranted
                }

                // Check if birthday exists
                val existingBirthday =
                    birthdayRepository.getBirthdayById(birthdayId)
                        ?: return UpdateBirthdayResult.NotFound("Birthday with ID $birthdayId not found")

                // Create updated birthday
                val updatedBirthday =
                    existingBirthday.copy(
                        name = birthdayValidator.sanitizeName(name),
                        birthDate = birthDate,
                        notes = birthdayValidator.sanitizeNotes(notes),
                        notificationsEnabled = notificationsEnabled,
                        advanceNotificationDays = advanceNotificationDays,
                        notificationHour = notificationHour,
                        notificationMinute = notificationMinute,
                    )

                birthdayRepository.updateBirthday(updatedBirthday)

                // Update notifications
                if (updatedBirthday.notificationsEnabled) {
                    val notificationResult = scheduleNotificationUseCase.scheduleNotification(updatedBirthday)
                    if (notificationResult is ScheduleNotificationResult.ExactAlarmPermissionNotGranted) {
                        return UpdateBirthdayResult.ExactAlarmPermissionNotGranted
                    }
                } else {
                    cancelNotificationUseCase(updatedBirthday.id)
                }

                return UpdateBirthdayResult.Success
            } catch (e: Exception) {
                return UpdateBirthdayResult.DatabaseError(e)
            }
        }

        /**
         * Updates only specific fields of a birthday.
         * Useful for partial updates like toggling notifications.
         */
        suspend fun updateBirthdayPartial(
            birthdayId: Long,
            name: String? = null,
            birthDate: LocalDate? = null,
            notes: String? = null,
            notificationsEnabled: Boolean? = null,
            advanceNotificationDays: Int? = null,
            notificationHour: Int? = null,
            notificationMinute: Int? = null,
        ): UpdateBirthdayResult {
            try {
                // Get existing birthday
                val existingBirthday =
                    birthdayRepository.getBirthdayById(birthdayId)
                        ?: return UpdateBirthdayResult.NotFound("Birthday with ID $birthdayId not found")

                // Apply partial updates
                val updatedBirthday =
                    existingBirthday.copy(
                        name = name?.trim() ?: existingBirthday.name,
                        birthDate = birthDate ?: existingBirthday.birthDate,
                        notes = notes?.trim()?.takeIf { it.isNotEmpty() } ?: existingBirthday.notes,
                        notificationsEnabled = notificationsEnabled ?: existingBirthday.notificationsEnabled,
                        advanceNotificationDays = advanceNotificationDays ?: existingBirthday.advanceNotificationDays,
                        notificationHour = notificationHour ?: existingBirthday.notificationHour,
                        notificationMinute = notificationMinute ?: existingBirthday.notificationMinute,
                    )

                // Validate the updated birthday
                val validationResult =
                    birthdayValidator.validateBirthday(
                        name = updatedBirthday.name,
                        birthDate = updatedBirthday.birthDate,
                        notes = updatedBirthday.notes,
                        advanceNotificationDays = updatedBirthday.advanceNotificationDays,
                        notificationHour = updatedBirthday.notificationHour,
                        notificationMinute = updatedBirthday.notificationMinute,
                    )

                if (validationResult.isInvalid) {
                    return UpdateBirthdayResult.ValidationError(validationResult.errorMessages)
                }

                // Check permission first if notifications are requested
                if (updatedBirthday.notificationsEnabled && !scheduleNotificationUseCase.canScheduleExactAlarms()) {
                    return UpdateBirthdayResult.ExactAlarmPermissionNotGranted
                }

                birthdayRepository.updateBirthday(updatedBirthday)

                // Update notifications
                if (updatedBirthday.notificationsEnabled) {
                    val notificationResult = scheduleNotificationUseCase.scheduleNotification(updatedBirthday)
                    if (notificationResult is ScheduleNotificationResult.ExactAlarmPermissionNotGranted) {
                        return UpdateBirthdayResult.ExactAlarmPermissionNotGranted
                    }
                } else {
                    cancelNotificationUseCase(updatedBirthday.id)
                }

                return UpdateBirthdayResult.Success
            } catch (e: Exception) {
                return UpdateBirthdayResult.DatabaseError(e)
            }
        }
    }

/**
 * Result of updating a birthday operation.
 */
sealed class UpdateBirthdayResult {
    object Success : UpdateBirthdayResult()

    data class ValidationError(val errors: List<String>) : UpdateBirthdayResult()

    data class NotFound(val message: String) : UpdateBirthdayResult()

    data class DatabaseError(val exception: Throwable) : UpdateBirthdayResult()

    object ExactAlarmPermissionNotGranted : UpdateBirthdayResult()
}
