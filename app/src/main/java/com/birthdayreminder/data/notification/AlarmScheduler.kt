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

            // List of all supported advance notification days to ensure thorough cancellation
            val SUPPORTED_ADVANCE_DAYS = listOf(1, 3, 7)

            private const val REQUEST_CODE_BASE = 10000

            fun getRequestCode(
                birthdayId: Long,
                advanceDays: Int = 0,
            ): Int {
                // Use the birthdayId directly as the base if it's within Int range,
                // or use a more robust hashing if collisions are a concern at scale.
                return (birthdayId.toInt() * 10) + advanceDays
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

            val notificationHour = birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR
            val notificationMinute = birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE

            var allScheduled = true

            val nextBirthday = getNextBirthdayDate(birthday.birthDate)
            val birthdayNotificationTime = nextBirthday.atTime(notificationHour, notificationMinute)

            if (scheduleExactAlarm(birthday.id, birthdayNotificationTime, TYPE_BIRTHDAY_TODAY, 0)) {
                Timber.d("Scheduled birthday notification for ${birthday.name} on $nextBirthday")
            } else {
                allScheduled = false
                Timber.e("Failed to schedule birthday notification for ${birthday.id}")
            }

            if (birthday.advanceNotificationDays > 0) {
                if (SUPPORTED_ADVANCE_DAYS.contains(birthday.advanceNotificationDays)) {
                    val advanceDate = nextBirthday.minusDays(birthday.advanceNotificationDays.toLong())
                    val advanceNotificationTime = advanceDate.atTime(notificationHour, notificationMinute)

                    if (scheduleExactAlarm(
                            birthday.id,
                            advanceNotificationTime,
                            TYPE_ADVANCE_REMINDER,
                            birthday.advanceNotificationDays,
                        )
                    ) {
                        Timber.d(
                            "Scheduled advance notification for ${birthday.name} " +
                                "${birthday.advanceNotificationDays} days before",
                        )
                    } else {
                        allScheduled = false
                        Timber.e("Failed to schedule advance notification for ${birthday.id}")
                    }
                } else {
                    Timber.w(
                        "Skipping advance notification for ${birthday.name}: " +
                            "${birthday.advanceNotificationDays} days is not supported. " +
                            "Supported values: $SUPPORTED_ADVANCE_DAYS",
                    )
                }
            }

            return allScheduled
        }

        fun cancelNotification(birthdayId: Long) {
            cancelAlarm(birthdayId, TYPE_BIRTHDAY_TODAY, 0)
            SUPPORTED_ADVANCE_DAYS.forEach { days ->
                cancelAlarm(birthdayId, TYPE_ADVANCE_REMINDER, days)
            }
            Timber.d("Cancelled all notifications for birthday $birthdayId")
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
                Timber.w("Notification time is in the past, skipping: $notificationTime")
                return false
            }

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
                    Timber.d("Scheduled exact alarm for birthday $birthdayId at $notificationTime")
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
            if (!birthday.notificationsEnabled) {
                return null
            }

            val notificationHour = birthday.notificationHour ?: DEFAULT_NOTIFICATION_HOUR
            val notificationMinute = birthday.notificationMinute ?: DEFAULT_NOTIFICATION_MINUTE

            val nextBirthday = getNextBirthdayDate(birthday.birthDate)
            val birthdayTime = nextBirthday.atTime(notificationHour, notificationMinute)

            val now = java.time.Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

            if (birthdayTime.isAfter(now)) return birthdayTime

            if (birthday.advanceNotificationDays > 0) {
                val advanceDate = nextBirthday.minusDays(birthday.advanceNotificationDays.toLong())
                val advanceTime = advanceDate.atTime(notificationHour, notificationMinute)
                if (advanceTime.isAfter(now)) return advanceTime
            }

            // If both passed, calculate for next year
            val nextYearBirthday = nextBirthday.plusYears(1)
            return nextYearBirthday.atTime(notificationHour, notificationMinute)
        }
    }
