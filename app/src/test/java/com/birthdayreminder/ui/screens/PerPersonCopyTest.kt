package com.birthdayreminder.ui.screens

import com.birthdayreminder.ui.viewmodel.PerPersonReminderRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * The per-person screen's copy and its two chip sets.
 *
 * These are user-visible strings and a persisted mapping, so both are pinned
 * here rather than left to a screenshot.
 */
class PerPersonCopyTest {
    private fun person(
        leadTimeDays: Int = 3,
        notificationsEnabled: Boolean = true,
        reminderTime: LocalTime? = LocalTime.of(9, 0),
    ) = PerPersonReminderRow(
        id = 1,
        name = "Amma",
        birthDate = LocalDate.of(1971, 3, 2),
        leadTimeDays = leadTimeDays,
        notificationsEnabled = notificationsEnabled,
        reminderTime = reminderTime,
    )

    @Test
    fun `a reminder off says so plainly`() {
        assertEquals("No reminder", PerPersonSummary.of(person(notificationsEnabled = false)))
    }

    @Test
    fun `an on-the-day reminder states only the time`() {
        // "On the day · 9:00 AM before" would be wrong: nothing is earlier.
        assertEquals("9:00 AM", PerPersonSummary.of(person(leadTimeDays = 0)))
    }

    @Test
    fun `an advance reminder states both the time and the lead`() {
        assertEquals("9:00 AM · 3 days before", PerPersonSummary.of(person(leadTimeDays = 3)))
    }

    @Test
    fun `an unset time points at the app default`() {
        // Null is not 9:00 AM on purpose: null means the global setting owns
        // this, and saying 9:00 AM here would hide that.
        assertEquals(
            "your default time · 3 days before",
            PerPersonSummary.of(person(reminderTime = null)),
        )
    }

    @Test
    fun `the lead-time chips include On the day, which the wizard omits`() {
        assertEquals(
            listOf("On the day", "1 day", "3 days", "1 week"),
            LeadTimeChipLabel.options.map { it.label },
        )
    }

    @Test
    fun `the lead-time chips map to the stored offsets`() {
        LeadTimeChipLabel.options.forEach { option ->
            assertEquals(option.label, LeadTimeChipLabel.forDays(option.days))
        }
    }

    @Test
    fun `an offset with no chip does not pretend to be one of them`() {
        // 5 days is storable, and the chip row must show nothing selected
        // rather than silently claim 3.
        assertNull(LeadTimeChipLabel.optionForDays(5))
    }

    @Test
    fun `the time chips match the concept`() {
        assertEquals(
            listOf("7:00 AM", "9:00 AM", "12:00 PM", "8:00 PM"),
            ReminderTimeChoice.options.map { it.label },
        )
    }

    @Test
    fun `a stored time resolves to its chip`() {
        assertEquals(
            ReminderTimeChoice.EVENING,
            ReminderTimeChoice.of(LocalTime.of(20, 0)),
        )
    }

    @Test
    fun `a time that is not one of the four has no chip`() {
        assertNull(ReminderTimeChoice.of(LocalTime.of(14, 37)))
        assertNull(ReminderTimeChoice.of(null))
    }

    @Test
    fun `an unset time selects no chip, rather than the default looking chosen`() {
        // The chip row must not claim a choice nobody made. Null means the
        // global setting in Settings owns this person, so showing 9:00 AM
        // selected would hide that changing the global default changes it here
        // too. This reads like a bug, so it is pinned.
        assertNull(
            "An unset time must leave every time chip unselected",
            ReminderTimeChoice.of(null),
        )
    }

    @Test
    fun `an offset with no chip leaves every lead-time chip unselected`() {
        // Same rule for the same reason: 5 days is a real stored value, and
        // falling back to "On the day" would describe a reminder the user did
        // not set.
        assertNull(LeadTimeChipLabel.optionForDays(5))
    }

    @Test
    fun `a stored time or offset does select its chip`() {
        // The other direction: showing nothing when a value IS set would be
        // just as wrong as claiming one that is not.
        assertEquals(ReminderTimeChoice.NOON, ReminderTimeChoice.of(LocalTime.of(12, 0)))
        assertEquals(LeadTimeChipLabel.ONE_WEEK, LeadTimeChipLabel.optionForDays(7))
    }

    @Test
    fun `the clock reads the way the concept shows it`() {
        // A 12-hour clock with a space before the meridiem: "9:00 AM", and
        // midnight and noon both correct.
        assertEquals("9:00 AM", ReminderClock.format(LocalTime.of(9, 0)))
        assertEquals("12:00 AM", ReminderClock.format(LocalTime.of(0, 0)))
        assertEquals("12:00 PM", ReminderClock.format(LocalTime.of(12, 0)))
        assertEquals("8:05 PM", ReminderClock.format(LocalTime.of(20, 5)))
    }
}
