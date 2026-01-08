package com.birthdayreminder.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate

class BirthdayNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    companion object {
        const val BIRTHDAY_ID_KEY = "birthday_id"
        const val NOTIFICATION_TYPE_KEY = "notification_type"
        const val ADVANCE_DAYS_KEY = "advance_days"

        const val TYPE_BIRTHDAY_TODAY = "birthday_today"
        const val TYPE_ADVANCE_REMINDER = "advance_reminder"
    }

    override suspend fun doWork(): Result {
        return try {
            val birthdayId = inputData.getLong(BIRTHDAY_ID_KEY, -1L)
            val notificationType = inputData.getString(NOTIFICATION_TYPE_KEY)

            if (birthdayId == -1L || notificationType == null) {
                return Result.failure()
            }

            // Get dependencies through Hilt's entry point
            val hiltEntryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    BirthdayNotificationWorkerEntryPoint::class.java,
                )

            val birthdayRepository = hiltEntryPoint.birthdayRepository()
            val calculateCountdownUseCase = hiltEntryPoint.calculateCountdownUseCase()
            val notificationHelper = hiltEntryPoint.notificationHelper()

            // Get the birthday from repository
            val birthdays = birthdayRepository.getAllBirthdays().first()
            val birthday =
                birthdays.find { it.id == birthdayId }
                    ?: return Result.failure()

            // Check if notifications are enabled for this birthday
            if (!birthday.notificationsEnabled) {
                return Result.success()
            }

            when (notificationType) {
                TYPE_BIRTHDAY_TODAY -> {
                    // Check if it's actually the birthday today
                    val daysUntil = calculateCountdownUseCase.getDaysUntilBirthday(birthday.birthDate)
                    if (daysUntil == 0) {
                        val age = calculateAge(birthday.birthDate)
                        notificationHelper.showBirthdayNotification(
                            birthdayId = birthday.id,
                            personName = birthday.name,
                            age = age,
                        )
                    }
                }

                TYPE_ADVANCE_REMINDER -> {
                    val advanceDays = inputData.getInt(ADVANCE_DAYS_KEY, 0)
                    val daysUntil = calculateCountdownUseCase.getDaysUntilBirthday(birthday.birthDate)

                    // Check if we should send the advance notification
                    if (daysUntil == advanceDays && advanceDays > 0) {
                        notificationHelper.showAdvanceBirthdayNotification(
                            birthdayId = birthday.id,
                            personName = birthday.name,
                            daysUntil = advanceDays,
                        )
                    }
                }
                else -> {
                    // Unknown notification type
                    return Result.failure()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in BirthdayNotificationWorker")
            Result.retry()
        }
    }

    private fun calculateAge(birthDate: LocalDate): Int {
        val today = LocalDate.now()
        var age = today.year - birthDate.year

        // Adjust if birthday hasn't occurred this year yet
        if (today.monthValue < birthDate.monthValue ||
            (today.monthValue == birthDate.monthValue && today.dayOfMonth < birthDate.dayOfMonth)
        ) {
            age--
        }

        return age
    }
}
