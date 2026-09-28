package com.birthdayreminder.domain.util

import java.time.LocalDate

/**
 * Which calendar year a birthday belongs to, and which years count toward a
 * streak.
 *
 * Both questions are about the same trap: an advance reminder fires days or
 * weeks before the date, and a birthday on 1 January is announced in December
 * of the year before. The acknowledgement streak is per year, so attributing
 * either an event or a year of eligibility to the wrong one is silent -- the
 * reminder fires, the user opens it, the number is stored, and nothing on
 * screen looks broken.
 */
object BirthdayYear {
    /**
     * The year the upcoming occurrence of [birthDate] falls in.
     *
     * @param birthDate the person's date of birth
     * @param on the day the reminder is firing
     */
    fun occurrenceYear(
        birthDate: LocalDate,
        on: LocalDate = LocalDate.now(),
    ): Int {
        val thisYear = birthDate.withYear(on.year)
        // Compare month and day only: the birth year is irrelevant to which
        // occurrence is next.
        val alreadyPassed = thisYear.isBefore(on)
        return if (alreadyPassed) on.year + 1 else on.year
    }

    /**
     * The first year in which this person's birthday could have been reminded
     * about, given they were added on [addedOn].
     *
     * A birthday is not eligible before the app knew about it, and the year
     * someone was added usually *does* count: added in March with a December
     * birthday, you were reminded in December. What does not count is the part
     * of the year already gone -- added in June with a March birthday, the next
     * chance is the following March.
     *
     * @param birthDate the person's date of birth
     * @param addedOn when they were saved
     */
    fun firstEligibleYear(
        birthDate: LocalDate,
        addedOn: LocalDate,
    ): Int {
        val monthDayThisYear = java.time.MonthDay.of(birthDate.monthValue, birthDate.dayOfMonth)
        val addedMonthDay = java.time.MonthDay.of(addedOn.monthValue, addedOn.dayOfMonth)
        return if (monthDayThisYear >= addedMonthDay) addedOn.year else addedOn.year + 1
    }
}
