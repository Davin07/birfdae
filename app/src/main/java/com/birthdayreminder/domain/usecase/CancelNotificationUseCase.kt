package com.birthdayreminder.domain.usecase

import androidx.work.WorkManager
import com.birthdayreminder.data.notification.NotificationHelper
import javax.inject.Inject

sealed class CancelNotificationResult {
    object Success : CancelNotificationResult()
    data class Error(val message: String) : CancelNotificationResult()
}

class CancelNotificationUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val notificationHelper: NotificationHelper
) {
    
    suspend fun cancelNotification(birthdayId: Long): CancelNotificationResult {
        return try {
            // Cancel scheduled work
            workManager.cancelUniqueWork("birthday_notification_$birthdayId")
            
            // Cancel advance notifications for all possible days
            listOf(1, 3, 7).forEach { days ->
                workManager.cancelUniqueWork("advance_notification_${birthdayId}_$days")
            }
            
            // Cancel any currently displayed notifications
            notificationHelper.cancelBirthdayNotification(birthdayId)
            
            CancelNotificationResult.Success
        } catch (e: Exception) {
            CancelNotificationResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun cancelAllNotifications(): CancelNotificationResult {
        return try {
            workManager.cancelAllWorkByTag("birthday_notifications")
            CancelNotificationResult.Success
        } catch (e: Exception) {
            CancelNotificationResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    operator fun invoke(birthdayId: Long) {
        // Cancel scheduled work
        workManager.cancelUniqueWork("birthday_notification_$birthdayId")
        
        // Cancel advance notifications for all possible days
        listOf(1, 3, 7).forEach { days ->
            workManager.cancelUniqueWork("advance_notification_${birthdayId}_$days")
        }
        
        // Cancel any currently displayed notifications
        notificationHelper.cancelBirthdayNotification(birthdayId)
    }
}