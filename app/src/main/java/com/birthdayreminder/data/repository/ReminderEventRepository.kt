package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.entity.ReminderEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * What the user has actually done about birthdays.
 *
 * Separate from [BirthdayRepository] on purpose. A birthday is a fact about
 * someone else; a reminder event is a fact about the user's behaviour, and the
 * two deserve to be read, changed and reasoned about independently. It also
 * means the streak can never be derived from a field on the birthday row, which
 * is how a "remembered 11 of 11" claim would end up being asserted rather than
 * counted.
 */
interface ReminderEventRepository {
    /** One person's history, newest year first. */
    fun getEventsForBirthday(birthdayId: Long): Flow<List<ReminderEvent>>

    /**
     * A one-shot read of one person's history.
     *
     * The streak is computed on demand rather than observed, because it is
     * only read when the home screen is drawn and there is nothing for a
     * collector to keep in step with.
     */
    suspend fun getEventsForBirthdaySnapshot(birthdayId: Long): List<ReminderEvent>

    /** Every event across all people. */
    fun getAllEvents(): Flow<List<ReminderEvent>>

    /**
     * Records that a birthday notification was opened.
     *
     * Idempotent per year, and does not overwrite a recorded share.
     *
     * @param birthdayId the person the notification was about
     * @param year the calendar year the birthday fell in
     */
    suspend fun recordAcknowledged(
        birthdayId: Long,
        year: Int,
        at: LocalDateTime = LocalDateTime.now(),
    )

    /**
     * Records that a card was shared.
     *
     * Idempotent per year, and does not overwrite a recorded acknowledgement.
     *
     * @param birthdayId the person whose card was shared
     * @param year the calendar year the birthday fell in
     */
    suspend fun recordShared(
        birthdayId: Long,
        year: Int,
        at: LocalDateTime = LocalDateTime.now(),
    )
}
