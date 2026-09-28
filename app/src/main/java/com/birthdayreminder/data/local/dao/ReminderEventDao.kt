package com.birthdayreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.birthdayreminder.data.local.entity.ReminderEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for [ReminderEvent].
 *
 * Records what the user did about a birthday, and nothing it cannot observe.
 * An acknowledgement is a notification the user actually opened; a share is a
 * card the user actually sent from the app. Neither is a claim about delivery,
 * because the app cannot see either outcome.
 */
@Dao
abstract class ReminderEventDao {
    /**
     * Every recorded event for one person, newest year first.
     *
     * The streak needs the whole history rather than a count, so the copy can
     * say "11 of her last 11" and mean the denominator.
     */
    @Query("SELECT * FROM reminder_events WHERE birthdayId = :birthdayId ORDER BY year DESC")
    abstract fun getEventsForBirthday(birthdayId: Long): Flow<List<ReminderEvent>>

    /**
     * A one-shot read of one person's history, for use cases that compute a
     * streak and do not need to observe it.
     */
    @Query("SELECT * FROM reminder_events WHERE birthdayId = :birthdayId ORDER BY year DESC")
    abstract suspend fun getEventsForBirthdaySnapshot(birthdayId: Long): List<ReminderEvent>

    /** Every event across all people, for the home screen hero. */
    @Query("SELECT * FROM reminder_events ORDER BY year DESC, id DESC")
    abstract fun getAllEvents(): Flow<List<ReminderEvent>>

    /**
     * Reads the single row for a birthday and year, if one exists.
     *
     * The unique index on (birthdayId, year) guarantees at most one, which is
     * what lets the writers below be read-modify-write rather than blind
     * inserts.
     */
    @Query("SELECT * FROM reminder_events WHERE birthdayId = :birthdayId AND year = :year LIMIT 1")
    protected abstract suspend fun findForYear(
        birthdayId: Long,
        year: Int,
    ): ReminderEvent?

    /**
     * Writes a row, replacing any existing one for the same birthday and year.
     *
     * Replace rather than ignore because the callers have already merged with
     * the existing row, and an ignore would silently drop the write.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertReplacing(event: ReminderEvent)

    /**
     * Records that a birthday notification was opened.
     *
     * Idempotent and additive: opening the same notification twice records one
     * year, and an acknowledgement never clears a share recorded earlier. The
     * two timestamps are independent because they answer different questions --
     * "did you see it" and "did you do something about it" -- and neither order
     * of arrival should lose the other.
     *
     * @param birthdayId the person the notification was about
     * @param year the calendar year the birthday fell in
     * @param at when the notification was opened
     */
    @Transaction
    open suspend fun recordAcknowledged(
        birthdayId: Long,
        year: Int,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        val existing = findForYear(birthdayId, year)
        insertReplacing(
            existing?.copy(acknowledgedAt = existing.acknowledgedAt ?: at)
                ?: ReminderEvent(
                    birthdayId = birthdayId,
                    year = year,
                    acknowledgedAt = at,
                    createdAt = at,
                ),
        )
    }

    /**
     * Records that a card was shared for this birthday.
     *
     * Additive in the same way as [recordAcknowledged]: sharing does not
     * overwrite an acknowledgement, and sharing twice records one year.
     *
     * @param birthdayId the person whose card was shared
     * @param year the calendar year the birthday fell in
     * @param at when the share was started
     */
    @Transaction
    open suspend fun recordShared(
        birthdayId: Long,
        year: Int,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        val existing = findForYear(birthdayId, year)
        insertReplacing(
            existing?.copy(sharedAt = existing.sharedAt ?: at)
                ?: ReminderEvent(
                    birthdayId = birthdayId,
                    year = year,
                    sharedAt = at,
                    createdAt = at,
                ),
        )
    }

    /**
     * Removes a person's history.
     *
     * The foreign key cascades when the person is deleted, so this is only
     * needed where history has to go without the person going with it.
     */
    @Query("DELETE FROM reminder_events WHERE birthdayId = :birthdayId")
    abstract suspend fun deleteForBirthday(birthdayId: Long)
}
