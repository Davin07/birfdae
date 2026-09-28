package com.birthdayreminder.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * The year an advance reminder is credited to.
 *
 * Getting this wrong is silent: the reminder fires, the user opens it, the
 * year is recorded, and the streak counts a birthday that had not happened
 * yet while missing the one that did. Nothing on screen looks broken.
 */
class BirthdayYearTest {
    @Test
    fun `a birthday later this year belongs to this year`() {
        val on = LocalDate.of(2026, 3, 1)
        val birthDate = LocalDate.of(1971, 12, 3)

        assertEquals(2026, BirthdayYear.occurrenceYear(birthDate, on))
    }

    @Test
    fun `a birthday already past this year belongs to next year`() {
        val on = LocalDate.of(2026, 12, 4)
        val birthDate = LocalDate.of(1971, 12, 3)

        assertEquals(
            "A reminder for 3 December that fires in December after it is a reminder for next year",
            2027,
            BirthdayYear.occurrenceYear(birthDate, on),
        )
    }

    @Test
    fun `a new year reminder belongs to the new year`() {
        // A 1 January birthday is announced in December of the year before.
        val on = LocalDate.of(2026, 12, 28)
        val birthDate = LocalDate.of(1990, 1, 1)

        assertEquals(2027, BirthdayYear.occurrenceYear(birthDate, on))
    }

    @Test
    fun `the birthday itself counts as the current year`() {
        val birthDate = LocalDate.of(1990, 7, 4)
        val on = LocalDate.of(2026, 7, 4)

        assertEquals(2026, BirthdayYear.occurrenceYear(birthDate, on))
    }

    @Test
    fun `a leap day birthday does not throw in a common year`() {
        val birthDate = LocalDate.of(1992, 2, 29)
        val on = LocalDate.of(2026, 2, 28)

        // withYear(2026) cannot represent 29 February, so this is exactly the
        // case that used to be able to throw.
        assertEquals(2026, BirthdayYear.occurrenceYear(birthDate, on))
    }
}
