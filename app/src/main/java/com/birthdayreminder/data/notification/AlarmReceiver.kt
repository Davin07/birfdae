package com.birthdayreminder.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_BIRTHDAY_NOTIFICATION = "com.birthdayreminder.ACTION_BIRTHDAY_NOTIFICATION"
    }

    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        if (context == null) return

        if (intent?.action != ACTION_BIRTHDAY_NOTIFICATION) {
            Timber.d("AlarmReceiver received unexpected action: ${intent?.action}")
            return
        }

        val birthdayId = intent.getLongExtra(AlarmScheduler.EXTRA_BIRTHDAY_ID, -1L)
        val notificationType = intent.getStringExtra(AlarmScheduler.EXTRA_NOTIFICATION_TYPE)
        val advanceDays = intent.getIntExtra(AlarmScheduler.EXTRA_ADVANCE_DAYS, 0)

        if (birthdayId == -1L || notificationType == null) {
            Timber.e("AlarmReceiver: Invalid intent data")
            return
        }

        Timber.d("AlarmReceiver: Processing notification for birthday $birthdayId, type: $notificationType")

        val pendingResult = goAsync()
        val scope =
            kotlinx.coroutines.CoroutineScope(
                kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO,
            )
        scope.launch {
            try {
                processNotification(context, birthdayId, notificationType, advanceDays)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processNotification(
        context: Context,
        birthdayId: Long,
        notificationType: String,
        advanceDays: Int,
    ) {
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AlarmReceiverEntryPoint::class.java,
            )

        val birthdayRepository = hiltEntryPoint.birthdayRepository()
        val calculateCountdownUseCase = hiltEntryPoint.calculateCountdownUseCase()
        val notificationHelper = hiltEntryPoint.notificationHelper()

        try {
            val birthday = birthdayRepository.getBirthdayById(birthdayId)

            if (birthday == null) {
                Timber.w("AlarmReceiver: Birthday not found for id $birthdayId")
                return
            }

            if (!birthday.notificationsEnabled) {
                Timber.d("AlarmReceiver: Notifications disabled for birthday $birthdayId")
                return
            }

            when (notificationType) {
                AlarmScheduler.TYPE_BIRTHDAY_TODAY -> {
                    val daysUntil = calculateCountdownUseCase.getDaysUntilBirthday(birthday.birthDate)
                    if (daysUntil == 0) {
                        val age = calculateAge(birthday.birthDate)
                        notificationHelper.showBirthdayNotification(
                            birthdayId = birthday.id,
                            personName = birthday.name,
                            age = age,
                        )
                        Timber.d("AlarmReceiver: Showed birthday notification for ${birthday.name}")
                    } else {
                        Timber.d("AlarmReceiver: Skipped birthday notification - not today (days until: $daysUntil)")
                    }
                }

                AlarmScheduler.TYPE_ADVANCE_REMINDER -> {
                    val daysUntil = calculateCountdownUseCase.getDaysUntilBirthday(birthday.birthDate)
                    if (daysUntil == advanceDays && advanceDays > 0) {
                        notificationHelper.showAdvanceBirthdayNotification(
                            birthdayId = birthday.id,
                            personName = birthday.name,
                            daysUntil = advanceDays,
                        )
                        Timber.d("AlarmReceiver: Showed advance notification for ${birthday.name} ($advanceDays days)")
                    } else {
                        Timber.d(
                            "AlarmReceiver: Skipped advance notification - " +
                                "daysUntil=$daysUntil, advanceDays=$advanceDays",
                        )
                    }
                }

                else -> {
                    Timber.e("AlarmReceiver: Unknown notification type: $notificationType")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "AlarmReceiver: Error processing notification")
        }
    }

    private fun calculateAge(birthDate: LocalDate): Int {
        return java.time.Period.between(birthDate, LocalDate.now()).years
    }
}
