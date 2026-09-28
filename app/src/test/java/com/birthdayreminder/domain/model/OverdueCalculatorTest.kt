package com.birthdayreminder.domain.model

import com.birthdayreminder.data.local.entity.Birthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Guards the overdue window.
 *
 * The rules exist to prevent two opposite failures: never mentioning a missed
 * birthday, and mentioning a birthday from eight months ago on every launch.
 */
class OverdueCalculatorTest {
    private fun birthday(
        month: Int,
        day: Int,
        year: Int = 1990,
        skippedYear: Int = Birthday.NO_SKIPPED_YEAR,
    ) = Birthday(
        id = 1,
        name = "Test",
        birthDate = LocalDate.of(year, month, day),
        skippedYear = skippedYear,
    )

    @Test
    fun `a birthday that has not arrived is not overdue`() {
        val today = LocalDate.of(2026, 9, 27)

        assertNull(OverdueCalculator.overdueFor(birthday(12, 25), today))
    }

    @Test
    fun `a birthday falling today is not yet overdue`() {
        val today = LocalDate.of(2026, 9, 27)

        // Zero days overdue is a birthday, not a missed one. Showing it as
        // overdue put the same person in the "Needs a moment" card, the hero,
        // and the list on the same screen.
        assertNull(OverdueCalculator.overdueFor(birthday(9, 27), today))
    }

    @Test
    fun `a birthday yesterday is one day overdue`() {
        val today = LocalDate.of(2026, 9, 27)

        assertEquals(1, OverdueCalculator.overdueFor(birthday(9, 26), today)?.daysOverdue)
    }

    @Test
    fun `a birthday inside the window is overdue`() {
        val today = LocalDate.of(2026, 9, 27)
        val days = OverdueCalculator.OVERDUE_WINDOW_DAYS.toInt()

        assertNotNull(OverdueCalculator.overdueFor(birthday(9, 28 - days), today))
    }

    @Test
    fun `a birthday outside the window stops being overdue`() {
        val today = LocalDate.of(2026, 9, 27)
        val days = OverdueCalculator.OVERDUE_WINDOW_DAYS.toInt() + 1

        assertNull(OverdueCalculator.overdueFor(birthday(9, 27 - days), today))
    }

    @Test
    fun `skipping hides the birthday for that year only`() {
        val today = LocalDate.of(2026, 9, 27)
        val person = birthday(9, 20, skippedYear = 2026)

        assertNull("Should be hidden in 2026", OverdueCalculator.overdueFor(person, today))

        // The same person comes back into scope the following year without the
        // user doing anything.
        val nextYear = LocalDate.of(2027, 9, 25)
        assertNotNull(
            "Should return in 2027",
            OverdueCalculator.overdueFor(person, nextYear),
        )
    }

    @Test
    fun `an ancient birthday is never overdue`() {
        val today = LocalDate.of(2026, 9, 27)

        assertNull(OverdueCalculator.overdueFor(birthday(1, 1), today))
    }

    @Test
    fun `a 29 February birthday clamps in a non-leap year instead of throwing`() {
        val leapling = birthday(2, 29, year = 1996)
        val today = LocalDate.of(2026, 3, 1)

        val result = OverdueCalculator.overdueFor(leapling, today)

        assertNotNull("A leapling must still be tracked in a non-leap year", result)
        assertEquals(2, result!!.occurredOn.monthValue)
        assertEquals(28, result.occurredOn.dayOfMonth)
    }

    @Test
    fun `a 31st birthday is clamped when placed in a shorter month`() {
        // occurrenceIn puts the month and day into a given year. A 31st placed
        // in a 30-day month must clamp to the 30th rather than throw, which is
        // exactly what happens for a 31-January birthday in a non-leap year.
        val clamped = OverdueCalculator.occurrenceIn(LocalDate.of(1990, 1, 31), 2026)

        assertEquals(1, clamped.monthValue)
        assertEquals(31, clamped.dayOfMonth) // January has 31 days

        // April does not. Placing a 31st there is the interesting case.
        val aprilBirth = LocalDate.of(1990, 3, 31)
        val inApril = OverdueCalculator.occurrenceIn(aprilBirth, 2026)
        assertEquals(3, inApril.monthValue)
        assertEquals(31, inApril.dayOfMonth)
    }

    @Test
    fun `a 29 February birthday clamps to the 28th in a non-leap year`() {
        val leapling = birthday(2, 29, year = 1996)

        val in2026 = OverdueCalculator.occurrenceIn(leapling.birthDate, 2026)
        assertEquals(2, in2026.monthValue)
        assertEquals(28, in2026.dayOfMonth)

        val in2024 = OverdueCalculator.occurrenceIn(leapling.birthDate, 2024)
        assertEquals(29, in2024.dayOfMonth)
    }

    @Test
    fun `the age turned matches the occurrence year`() {
        val person = birthday(9, 20, year = 1985)
        val result = OverdueCalculator.overdueFor(person, LocalDate.of(2026, 9, 27))

        assertEquals(41, result!!.ageTurned)
    }

    @Test
    fun `wasSkippedThisYear only matches the current year`() {
        val person = birthday(9, 20, skippedYear = 2025)

        assertFalse(OverdueCalculator.wasSkippedThisYear(person, LocalDate.of(2026, 9, 27)))
        assertTrue(OverdueCalculator.wasSkippedThisYear(person, LocalDate.of(2025, 9, 27)))
    }

    @Test
    fun `occurrenceIn keeps the month and day`() {
        val date = LocalDate.of(1990, 7, 4)

        assertEquals(LocalDate.of(2026, 7, 4), OverdueCalculator.occurrenceIn(date, 2026))
    }

    @Test
    fun `overdue covers a full sweep of the window without gaps`() {
        val today = LocalDate.of(2026, 9, 27)
        val window = OverdueCalculator.OVERDUE_WINDOW_DAYS.toInt()

        // Starts at one day ago: day zero is the birthday itself, which is
        // happening rather than missed.
        for (daysAgo in 1..window) {
            val occurrence = today.minusDays(daysAgo.toLong())
            val person = birthday(occurrence.monthValue, occurrence.dayOfMonth)
            assertNotNull(
                "Gap at $daysAgo days ago",
                OverdueCalculator.overdueFor(person, today),
            )
        }
    }
}
