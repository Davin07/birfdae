package com.birthdayreminder.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.birthdayreminder.data.local.entity.Birthday
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            const val DEFAULT_NOTIFICATION_HOUR = 9
            const val DEFAULT_NOTIFICATION_MINUTE = 0

            const val ACTION_BIRTHDAY_NOTIFICATION = "com.birthdayreminder.ACTION_BIRTHDAY_NOTIFICATION"
            const val EXTRA_BIRTHDAY_ID = "birthday_id"
            const val EXTRA_NOTIFICATION_TYPE = "notification_type"
            const val EXTRA_ADVANCE_DAYS = "advance_days"

            const val TYPE_BIRTHDAY_TODAY = "birthday_today"
            const val TYPE_ADVANCE_REMINDER = "advance_reminder"

            // Legacy support
            val SUPPORTED_ADVANCE_DAYS = listOf(1, 3, 7)

            private const val REQUEST_CODE_BASE = 10000

            fun getRequestCode(
                birthdayId: Long,
                advanceDays: Int = 0,
            ): Int {
                return (birthdayId.hashCode() * 100) + (advanceDays % 100)
            }
        }

        private val alarmManager: AlarmManager by lazy {
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }

        fun scheduleNotification(birthday: Birthday): Boolean {
            if (!birthday.notificationsEnabled) {
                Timber.d("Notifications disabled for birthday ${birthday.id}")
                return false
            }

            // Determine notification time (prefer new field, fallback to legacy/default)
            val notificationTime =
                birthday.notificationTime
                    ?: LocalTime.of(
                        birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR,
                        birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE,
                    )

            // Determine offsets (prefer new field, fallback to legacy)
            val offsets =
                if (birthday.notificationOffsets.isNotEmpty()) {
                    birthday.notificationOffsets
                } else {
                    val list = mutableListOf<Int>()
                    // Always schedule 'on day' (0) if using legacy fallback?
                    // Previous logic always scheduled TYPE_BIRTHDAY_TODAY (0).
                    list.add(0)
                    if (birthday.advanceNotificationDays > 0) {
                        list.add(birthday.advanceNotificationDays)
                    }
                    list
                }

            var allScheduled = true
            val nextBirthday = getNextBirthdayDate(birthday.birthDate)

            offsets.forEach { offsetDays ->
                val triggerDate = nextBirthday.minusDays(offsetDays.toLong())
                val triggerTime = triggerDate.atTime(notificationTime)

                val type = if (offsetDays == 0) TYPE_BIRTHDAY_TODAY else TYPE_ADVANCE_REMINDER

                if (scheduleExactAlarm(birthday.id, triggerTime, type, offsetDays)) {
                    Timber.d("Scheduled notification for ${birthday.name} (offset: $offsetDays) at $triggerTime")
                } else {
                    allScheduled = false
                    Timber.e("Failed to schedule notification for ${birthday.id} (offset: $offsetDays)")
                }
            }

            return allScheduled
        }

        fun cancelNotification(birthday: Birthday) {
            // Cancel based on current offsets
            val offsets =
                if (birthday.notificationOffsets.isNotEmpty()) {
                    birthday.notificationOffsets
                } else {
                    val list = mutableListOf(0)
                    if (birthday.advanceNotificationDays > 0) {
                        list.add(birthday.advanceNotificationDays)
                    }
                    // Also include standard legacy offsets to be safe when clearing
                    list.addAll(SUPPORTED_ADVANCE_DAYS)
                    list.distinct()
                }

            offsets.forEach { days ->
                val type = if (days == 0) TYPE_BIRTHDAY_TODAY else TYPE_ADVANCE_REMINDER
                cancelAlarm(birthday.id, type, days)
            }
            Timber.d("Cancelled notifications for birthday ${birthday.id}")
        }

        // Overload for ID-only cancellation (clears common offsets)
        fun cancelNotification(birthdayId: Long) {
            (listOf(0) + SUPPORTED_ADVANCE_DAYS).forEach { days ->
                val type = if (days == 0) TYPE_BIRTHDAY_TODAY else TYPE_ADVANCE_REMINDER
                cancelAlarm(birthdayId, type, days)
            }
        }

        private fun scheduleExactAlarm(
            birthdayId: Long,
            notificationTime: LocalDateTime,
            notificationType: String,
            advanceDays: Int,
        ): Boolean {
            val zoneId = ZoneId.systemDefault()
            val triggerMillis = notificationTime.atZone(zoneId).toInstant().toEpochMilli()

            if (triggerMillis <= System.currentTimeMillis()) {
                // If the computed time is in the past, it means the birthday/reminder for *this year* has passed.
                // We should schedule for *next year*.
                // BUT, getNextBirthdayDate usually returns next future date?
                // logic in getNextBirthdayDate: returns birthdayThisYear if >= today.
                // If birthday is today, triggerMillis might be past if time passed.
                // If time passed today, we should probably schedule for next year.
                // Re-calculating for next year:

                // However, recursively calling might be complex.
                // Simplest is to check: if (triggerMillis <= now) add 1 year.
                val nextYearTrigger = notificationTime.plusYears(1)
                val nextTriggerMillis = nextYearTrigger.atZone(zoneId).toInstant().toEpochMilli()

                // Recursive call or just set?
                // Let's just update triggerMillis logic
                // But wait, getNextBirthdayDate handles date. notificationTime handles time.
                // If getNextBirthdayDate returns TODAY, and notification time (9am) passed (now 10am).
                // We should schedule for next year.

                return scheduleExactAlarmInternal(birthdayId, nextTriggerMillis, notificationType, advanceDays)
            }

            return scheduleExactAlarmInternal(birthdayId, triggerMillis, notificationType, advanceDays)
        }

        private fun scheduleExactAlarmInternal(
            birthdayId: Long,
            triggerMillis: Long,
            notificationType: String,
            advanceDays: Int,
        ): Boolean {
            val requestCode = getRequestCode(birthdayId, advanceDays)
            val intent = createNotificationIntent(birthdayId, notificationType, advanceDays)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            return try {
                if (canScheduleExactAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent,
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent,
                        )
                    }
                    Timber.d("Scheduled exact alarm for birthday $birthdayId (offset $advanceDays)")
                    true
                } else {
                    Timber.w("Cannot schedule exact alarms, permission not granted")
                    false
                }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException when scheduling exact alarm")
                false
            }
        }

        private fun cancelAlarm(
            birthdayId: Long,
            notificationType: String,
            advanceDays: Int,
        ) {
            val requestCode = getRequestCode(birthdayId, advanceDays)
            val intent = createNotificationIntent(birthdayId, notificationType, advanceDays)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            alarmManager.cancel(pendingIntent)
        }

        private fun createNotificationIntent(
            birthdayId: Long,
            notificationType: String,
            advanceDays: Int,
        ): Intent {
            return Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_BIRTHDAY_NOTIFICATION
                putExtra(EXTRA_BIRTHDAY_ID, birthdayId)
                putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
                putExtra(EXTRA_ADVANCE_DAYS, advanceDays)
            }
        }

        private fun getNextBirthdayDate(birthDate: LocalDate): LocalDate {
            // Handle Feb 29 in non-leap years logic
            val today = java.time.LocalDate.now()
            val targetYear = today.year

            val birthdayThisYear =
                try {
                    birthDate.withYear(targetYear)
                } catch (e: java.time.DateTimeException) {
                    if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29) {
                        java.time.LocalDate.of(targetYear, 2, 28)
                    } else {
                        throw e
                    }
                }

            return if (birthdayThisYear.isAfter(today) || birthdayThisYear.isEqual(today)) {
                birthdayThisYear
            } else {
                val nextYear = targetYear + 1
                try {
                    birthDate.withYear(nextYear)
                } catch (e: java.time.DateTimeException) {
                    if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29) {
                        java.time.LocalDate.of(nextYear, 2, 28)
                    } else {
                        throw e
                    }
                }
            }
        }

        fun canScheduleExactAlarms(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        }

        fun getNextNotificationTime(birthday: Birthday): LocalDateTime? {
            // Updated to support multiple offsets?
            // This function is likely used for UI display "Next notification at...".
            // If multiple, return the soonest one?

            if (!birthday.notificationsEnabled) {
                return null
            }

            val notificationTime =
                birthday.notificationTime
                    ?: LocalTime.of(
                        birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR,
                        birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE,
                    )

            val offsets =
                if (birthday.notificationOffsets.isNotEmpty()) {
                    birthday.notificationOffsets
                } else {
                    val list = mutableListOf(0)
                    if (birthday.advanceNotificationDays > 0) list.add(birthday.advanceNotificationDays)
                    list
                }

            val nextBirthday = getNextBirthdayDate(birthday.birthDate)
            val now = java.time.Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

            // Calculate all potential triggers for next birthday
            val triggers =
                offsets.map { offset ->
                    val date = nextBirthday.minusDays(offset.toLong())
                    date.atTime(notificationTime)
                }

            // Find first one in future
            val futureTriggers = triggers.filter { it.isAfter(now) }.sorted()

            if (futureTriggers.isNotEmpty()) return futureTriggers.first()

            // If all passed for this year's birthday, check next year
            val nextYearBirthday = nextBirthday.plusYears(1)
            val nextYearTriggers =
                offsets.map { offset ->
                    val date = nextYearBirthday.minusDays(offset.toLong())
                    date.atTime(notificationTime)
                }

            return nextYearTriggers.sorted().firstOrNull()
        }
    }
