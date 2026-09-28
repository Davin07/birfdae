package com.birthdayreminder.domain.model

import com.birthdayreminder.data.local.entity.Birthday
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * A birthday whose date has already passed this year.
 *
 * The countdown model always rolls forward to the *next* occurrence, so a
 * missed birthday is invisible to it. This type is the explicit "you have not
 * acknowledged this yet" case, and it only exists for a bounded window: once
 * the date is old enough that next year's is closer, it stops being overdue.
 */
data class OverdueBirthday(
    val birthday: Birthday,
    /** The date that has already passed. */
    val occurredOn: LocalDate,
    /** Whole days since [occurredOn]; 0 means it is today. */
    val daysOverdue: Int,
) {
    val name: String get() = birthday.name
    val id: Long get() = birthday.id

    /**
     * The age the person turned on [occurredOn].
     *
     * Uses the same arithmetic as SafeDateCalculator.calculateAge rather than
     * subtracting years, because a plain year difference is not the age someone
     * has reached: 29 February in a common year clamps to 28 February, and a
     * birth date later in the year than [occurredOn] has not come round yet.
     */
    val ageTurned: Int = Period.between(birthday.birthDate, occurredOn).years
}

/**
 * Decides whether a birthday is overdue, and for how long.
 *
 * A birthday is overdue when its date has passed and the user has not already
 * said they are not celebrating it this year. It stops being overdue after
 * [OVERDUE_WINDOW_DAYS] so the prompt cannot nag about something from eleven
 * months ago.
 */
object OverdueCalculator {
    /**
     * How long a missed birthday keeps asking.
     *
     * Long enough to catch someone who noticed late, short enough that it is
     * not a permanent reminder about a date that has lost all meaning.
     */
    const val OVERDUE_WINDOW_DAYS: Long = 14

    /**
     * Returns the overdue entry for a birthday, or null.
     *
     * @param birthday the person
     * @param today the current date
     * @return an [OverdueBirthday] when the date has passed, is inside the
     *   window, and has not been skipped for this year
     */
    fun overdueFor(
        birthday: Birthday,
        today: LocalDate = LocalDate.now(),
    ): OverdueBirthday? {
        if (wasSkippedThisYear(birthday, today)) return null

        val thisYearOccurrence = occurrenceIn(birthday.birthDate, today.year)
        // A birthday that is today is not overdue, it is happening. Only a date
        // that has already gone by counts, so this must be isBefore rather than
        // !isAfter -- otherwise the same person shows up as both the overdue
        // prompt and today's hero, which is what a same-day birthday did.
        if (!thisYearOccurrence.isBefore(today)) return null

        val daysOverdue = ChronoUnit.DAYS.between(thisYearOccurrence, today)
        if (daysOverdue > OVERDUE_WINDOW_DAYS) return null

        return OverdueBirthday(
            birthday = birthday,
            occurredOn = thisYearOccurrence,
            daysOverdue = daysOverdue.toInt(),
        )
    }

    /**
     * Whether the user already declined to celebrate this year.
     *
     * The decision is stored per-year, so someone skipped in 2026 comes back
     * into scope in 2027 without the user having to do anything.
     *
     * @param birthday the person
     * @param today the current date
     */
    fun wasSkippedThisYear(
        birthday: Birthday,
        today: LocalDate = LocalDate.now(),
    ): Boolean = birthday.skippedYear == today.year

    /**
     * The month and day of [birthDate] in [year].
     *
     * Clamped for 29 February in a non-leap year rather than throwing or
     * silently shifting the date.
     *
     * @param birthDate the original birth date
     * @param year the year to place the month and day in
     */
    fun occurrenceIn(
        birthDate: LocalDate,
        year: Int,
    ): LocalDate {
        val month = birthDate.monthValue
        val day = birthDate.dayOfMonth
        val maxDay = java.time.YearMonth.of(year, month).lengthOfMonth()
        return LocalDate.of(year, month, minOf(day, maxDay))
    }
}
