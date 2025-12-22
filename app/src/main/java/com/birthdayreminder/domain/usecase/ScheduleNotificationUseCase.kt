package com.birthdayreminder.domain.usecase

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.notification.BirthdayNotificationWorker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class ScheduleNotificationResult {
    object Success : ScheduleNotificationResult()
    object NotificationsDisabled : ScheduleNotificationResult()
    data class Error(val message: String) : ScheduleNotificationResult()
}

class ScheduleNotificationUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        // Default notification time if not specified
        private const val DEFAULT_NOTIFICATION_HOUR = 9 // 9 AM
        private const val DEFAULT_NOTIFICATION_MINUTE = 0
    }
    
    suspend fun scheduleNotification(birthday: Birthday): ScheduleNotificationResult {
        return try {
            if (!birthday.notificationsEnabled) {
                return ScheduleNotificationResult.NotificationsDisabled
            }
            
            // Cancel any existing notifications for this birthday
            cancelNotifications(birthday.id)
            
            // Schedule birthday notification (on the day)
            scheduleBirthdayNotification(birthday)
            
            // Schedule advance notifications if enabled
            if (birthday.advanceNotificationDays > 0) {
                scheduleAdvanceNotification(birthday, birthday.advanceNotificationDays)
            }
            
            ScheduleNotificationResult.Success
        } catch (e: Exception) {
            ScheduleNotificationResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    operator fun invoke(birthday: Birthday) {
        if (!birthday.notificationsEnabled) {
            return
        }
        
        // Cancel any existing notifications for this birthday
        cancelNotifications(birthday.id)
        
        // Schedule birthday notification (on the day)
        scheduleBirthdayNotification(birthday)
        
        // Schedule advance notifications if enabled
        if (birthday.advanceNotificationDays > 0) {
            scheduleAdvanceNotification(birthday, birthday.advanceNotificationDays)
        }
    }
    
    private fun scheduleBirthdayNotification(birthday: Birthday) {
        val nextBirthday = getNextBirthdayDate(birthday.birthDate)
        // Use custom notification time if set, otherwise use default
        val notificationHour = birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR
        val notificationMinute = birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE
        val notificationTime = nextBirthday.atTime(notificationHour, notificationMinute)
        val delay = calculateDelay(notificationTime)
        
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<BirthdayNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putLong(BirthdayNotificationWorker.BIRTHDAY_ID_KEY, birthday.id)
                        .putString(BirthdayNotificationWorker.NOTIFICATION_TYPE_KEY, BirthdayNotificationWorker.TYPE_BIRTHDAY_TODAY)
                        .build()
                )
                .addTag("birthday_${birthday.id}")
                .build()
            
            workManager.enqueueUniqueWork(
                "birthday_notification_${birthday.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
    
    private fun scheduleAdvanceNotification(birthday: Birthday, advanceDays: Int) {
        val nextBirthday = getNextBirthdayDate(birthday.birthDate)
        val advanceNotificationDate = nextBirthday.minusDays(advanceDays.toLong())
        // Use custom notification time if set, otherwise use default
        val notificationHour = birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR
        val notificationMinute = birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE
        val notificationTime = advanceNotificationDate.atTime(notificationHour, notificationMinute)
        val delay = calculateDelay(notificationTime)
        
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<BirthdayNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putLong(BirthdayNotificationWorker.BIRTHDAY_ID_KEY, birthday.id)
                        .putString(BirthdayNotificationWorker.NOTIFICATION_TYPE_KEY, BirthdayNotificationWorker.TYPE_ADVANCE_REMINDER)
                        .putInt(BirthdayNotificationWorker.ADVANCE_DAYS_KEY, advanceDays)
                        .build()
                )
                .addTag("birthday_${birthday.id}")
                .build()
            
            workManager.enqueueUniqueWork(
                "advance_notification_${birthday.id}_${advanceDays}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
    
    private fun getNextBirthdayDate(birthDate: LocalDate): LocalDate {
        val today = java.time.LocalDate.now()
        val thisYearBirthday = birthDate.withYear(today.year)
        
        return if (thisYearBirthday.isAfter(today) || thisYearBirthday.isEqual(today)) {
            thisYearBirthday
        } else {
            thisYearBirthday.plusYears(1)
        }
    }
    
    private fun calculateDelay(notificationTime: LocalDateTime): Long {
        val now = java.time.Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        val zoneId = ZoneId.systemDefault()
        
        val nowMillis = now.atZone(zoneId).toInstant().toEpochMilli()
        val notificationMillis = notificationTime.atZone(zoneId).toInstant().toEpochMilli()
        
        return maxOf(0, notificationMillis - nowMillis)
    }
    
    private fun cancelNotifications(birthdayId: Long) {
        workManager.cancelUniqueWork("birthday_notification_$birthdayId")
        // Cancel advance notifications for all possible days
        listOf(1, 3, 7).forEach { days ->
            workManager.cancelUniqueWork("advance_notification_${birthdayId}_$days")
        }
    }
}