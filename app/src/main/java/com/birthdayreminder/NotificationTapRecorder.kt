package com.birthdayreminder

import com.birthdayreminder.data.repository.ReminderEventRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns "the user acted on a birthday" into a recorded year.
 *
 * Its own class because this is the one place where a behavioural fact is
 * turned into data, and the conversion has to be defensible: the tap and the
 * share are both observed, but the year they belong to is carried by the
 * caller, and getting that wrong would credit the wrong year forever.
 */
@Singleton
class NotificationTapRecorder
    @Inject
    constructor(
        private val reminderEventRepository: ReminderEventRepository,
    ) {
        /**
         * Records that a reminder was acknowledged by opening the notification.
         *
         * @param birthdayId the person the notification was about
         * @param year the year that birthday falls in, which for an advance
         *   reminder is not the year the notification fired in
         */
        suspend fun recordAcknowledged(
            birthdayId: Long,
            year: Int,
            at: LocalDateTime = LocalDateTime.now(),
        ) {
            reminderEventRepository.recordAcknowledged(birthdayId, year, at)
        }

        /**
         * Records that a card was shared.
         *
         * Deliberately separate from [recordAcknowledged]: sharing does not
         * imply the reminder was ever seen, and the streak is built from
         * acknowledgements alone.
         *
         * @param birthdayId the person whose card was shared
         * @param year the year that birthday falls in
         */
        suspend fun recordShared(
            birthdayId: Long,
            year: Int,
            at: LocalDateTime = LocalDateTime.now(),
        ) {
            reminderEventRepository.recordShared(birthdayId, year, at)
        }
    }
