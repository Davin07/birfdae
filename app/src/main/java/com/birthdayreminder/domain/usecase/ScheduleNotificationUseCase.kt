package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.notification.AlarmScheduler
import javax.inject.Inject

sealed class ScheduleNotificationResult {
    object Success : ScheduleNotificationResult()

    object NotificationsDisabled : ScheduleNotificationResult()

    data class Error(val message: String) : ScheduleNotificationResult()

    object ExactAlarmPermissionNotGranted : ScheduleNotificationResult()
}

class ScheduleNotificationUseCase
    @Inject
    constructor(
        private val alarmScheduler: AlarmScheduler,
    ) {
        suspend fun scheduleNotification(birthday: Birthday): ScheduleNotificationResult {
            return try {
                if (!birthday.notificationsEnabled) {
                    return ScheduleNotificationResult.NotificationsDisabled
                }

                if (!alarmScheduler.canScheduleExactAlarms()) {
                    return ScheduleNotificationResult.ExactAlarmPermissionNotGranted
                }

                alarmScheduler.cancelNotification(birthday.id)

                if (alarmScheduler.scheduleNotification(birthday)) {
                    ScheduleNotificationResult.Success
                } else {
                    ScheduleNotificationResult.Error("Failed to schedule notification")
                }
            } catch (e: Exception) {
                ScheduleNotificationResult.Error(e.message ?: "Unknown error occurred")
            }
        }

        fun canScheduleExactAlarms(): Boolean {
            return alarmScheduler.canScheduleExactAlarms()
        }

        suspend operator fun invoke(birthday: Birthday): ScheduleNotificationResult {
            if (!birthday.notificationsEnabled) {
                return ScheduleNotificationResult.NotificationsDisabled
            }

            return scheduleNotification(birthday)
        }
    }
