package com.birthdayreminder.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var birthdayRepository: com.birthdayreminder.data.repository.BirthdayRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        if (context == null) return

        val validActions = listOf(ACTION_BOOT_COMPLETED, ACTION_QUICKBOOT_POWERON)
        if (intent?.action !in validActions) {
            Timber.d("BootReceiver received unexpected action: ${intent?.action}")
            return
        }

        Timber.d("BootReceiver: Device booted, rescheduling all notifications")

        rescheduleAllNotifications()
    }

    private fun rescheduleAllNotifications() {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val birthdays = birthdayRepository.getAllBirthdays().first()
                var successCount = 0
                var failCount = 0

                for (birthday in birthdays) {
                    if (birthday.notificationsEnabled) {
                        if (alarmScheduler.scheduleNotification(birthday)) {
                            successCount++
                            Timber.d("Rescheduled notification for ${birthday.name}")
                        } else {
                            failCount++
                            Timber.e("Failed to reschedule notification for ${birthday.id}")
                        }
                    }
                }

                Timber.d("BootReceiver: Rescheduled $successCount notifications, $failCount failures")
            } catch (e: Exception) {
                Timber.e(e, "BootReceiver: Error rescheduling notifications")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
