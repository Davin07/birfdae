package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.notification.AlarmScheduler
import com.birthdayreminder.data.notification.NotificationHelper
import javax.inject.Inject

sealed class CancelNotificationResult {
    object Success : CancelNotificationResult()

    data class Error(val message: String) : CancelNotificationResult()
}

class CancelNotificationUseCase
    @Inject
    constructor(
        private val alarmScheduler: AlarmScheduler,
        private val notificationHelper: NotificationHelper,
    ) {
        suspend fun cancelNotification(birthdayId: Long): CancelNotificationResult {
            return try {
                alarmScheduler.cancelNotification(birthdayId)

                notificationHelper.cancelBirthdayNotification(birthdayId)

                CancelNotificationResult.Success
            } catch (e: Exception) {
                CancelNotificationResult.Error(e.message ?: "Unknown error occurred")
            }
        }

        suspend fun cancelAllNotifications(): CancelNotificationResult {
            return try {
                CancelNotificationResult.Success
            } catch (e: Exception) {
                CancelNotificationResult.Error(e.message ?: "Unknown error occurred")
            }
        }

        suspend operator fun invoke(birthdayId: Long): CancelNotificationResult {
            return cancelNotification(birthdayId)
        }
    }
