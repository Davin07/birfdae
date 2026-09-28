package com.birthdayreminder.domain.model

import com.birthdayreminder.data.local.entity.Birthday
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.Period

/**
 * A birthday that is *today* is not overdue.
 *
 * It is a narrow bug with an obvious symptom: the home screen opened with a
 * "Needs a moment" card for someone whose birthday was happening that day,
 * under a hero announcing the same person as today's headline, with a third
 * copy of the name in the "Next up" list below. Zero days overdue is a
 * birthday, not a missed one, and the window has to exclude it.
 */
class OverdueTodayTest {
    @Test
    fun `a birthday falling today is not overdue`() {
        val birthday =
            Birthday(
                id = 1,
                name = "Priya",
                birthDate = LocalDate.of(1995, 9, 28),
                skippedYear = Birthday.NO_SKIPPED_YEAR,
            )
        val today = LocalDate.of(2026, 9, 28)

        assertNull(
            "Someone turning " +
                "${Period.between(birthday.birthDate, today).years} today has not been missed",
            OverdueCalculator.overdueFor(birthday, today),
        )
    }

    @Test
    fun `yesterday is still overdue, so the fix did not overcorrect`() {
        val birthday =
            Birthday(
                id = 1,
                name = "Priya",
                birthDate = LocalDate.of(1995, 9, 28),
                skippedYear = Birthday.NO_SKIPPED_YEAR,
            )
        // The birthday is 28 September; two days on, it is genuinely behind us.
        val afterBirthday = LocalDate.of(2026, 9, 30)

        val overdue = OverdueCalculator.overdueFor(birthday, afterBirthday)

        assert(overdue != null) { "A birthday that slipped by should still be asked about" }
        assert(overdue!!.daysOverdue == 2)
    }
}
