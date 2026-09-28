package com.birthdayreminder.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * One thing the user did about a birthday in one year.
 *
 * This is the history the hero's streak is built from, and it exists because
 * `Birthday` only records `createdAt` -- never whether a reminder was acted on.
 * The prototype's "You've remembered 11 of her last 11" needs that fact to be
 * stored somewhere, and there is nowhere else for it to live.
 *
 * **What counts as remembering.** [acknowledgedAt] is set when the user opens
 * the birthday notification, which is an action we observe and can therefore
 * state plainly. [sharedAt] is set when a card is shared, which is a different
 * and stronger act, but one the app cannot verify: the chooser opening does not
 * mean the message was delivered, and no recipient's read receipt is visible
 * to us. So the two are stored separately and never merged.
 *
 * One row per (person, year). A person who gets a three-day-early reminder and
 * the day-of reminder is still one remembered year, which is why
 * [year] is part of the unique index rather than the timestamp.
 *
 * @property id autoincrementing row id
 * @property birthdayId the person this event is about
 * @property year the calendar year the birthday fell in, so re-opening the
 *   same notification does not add a second row for the same birthday
 * @property acknowledgedAt when the notification was opened, or null
 * @property sharedAt when a card for this birthday was shared, or null
 * @property createdAt when the row was written
 */
@Entity(
    tableName = "reminder_events",
    foreignKeys = [
        ForeignKey(
            entity = Birthday::class,
            parentColumns = ["id"],
            childColumns = ["birthdayId"],
            // Deleting a person must delete their history, or deleting and
            // re-adding someone would inherit the deleted person's streak.
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // The streak query is always "this person, these years", and the
        // uniqueness constraint is what keeps a double-tap from counting twice.
        Index(value = ["birthdayId", "year"], unique = true),
    ],
)
data class ReminderEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val birthdayId: Long,
    val year: Int,
    val acknowledgedAt: LocalDateTime? = null,
    val sharedAt: LocalDateTime? = null,
    @ColumnInfo(defaultValue = "")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Marks a birthday this year as never having been acted on.
         *
         * Matches the convention `skippedYear` already uses, so both
         * "has not happened" columns read the same way.
         */
        const val NO_EVENT_YEAR: Int = -1
    }
}
