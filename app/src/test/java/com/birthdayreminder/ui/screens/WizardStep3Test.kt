package com.birthdayreminder.ui.screens

import com.birthdayreminder.ui.card.CardTone
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * The wizard's last step, and the lead-time labels shared with the per-person
 * screen.
 *
 * The summary line exists to answer "when will I actually be reminded", and it
 * is built by interpolating an enum. That is exactly the shape that produces
 * "ONE_DAY before" instead of "1 day before", which is how a debug string gets
 * into the UI where nobody notices until a screenshot.
 */
class WizardStep3Test {
    private fun state(
        notificationsEnabled: Boolean = true,
        advanceNotificationDays: Int = 3,
        notificationTime: LocalTime? = null,
        name: String = "Rekha",
    ) = AddEditBirthdayUiState(
        name = name,
        birthDate = LocalDate.of(1990, 6, 12),
        notificationsEnabled = notificationsEnabled,
        advanceNotificationDays = advanceNotificationDays,
        notificationTime = notificationTime,
    )

    @Test
    fun `the summary names the lead time, not the enum constant`() {
        // The regression this exists for: interpolating the enum instance.
        val text = ReminderSummary.of(state(advanceNotificationDays = 1))
        assertEquals("9:00 AM · 1 day before", text)
        assertTrue("An enum constant leaked into the UI: $text", !text.contains("ONE_DAY"))
    }

    @Test
    fun `each lead time reads as words`() {
        assertEquals(
            "9:00 AM · 1 day before",
            ReminderSummary.of(state(advanceNotificationDays = 1)),
        )
        assertEquals(
            "9:00 AM · 3 days before",
            ReminderSummary.of(state(advanceNotificationDays = 3)),
        )
        assertEquals(
            "9:00 AM · 1 week before",
            ReminderSummary.of(state(advanceNotificationDays = 7)),
        )
    }

    @Test
    fun `reminders off says only that`() {
        assertEquals("Off", ReminderSummary.of(state(notificationsEnabled = false)))
    }

    @Test
    fun `a chosen time replaces the default`() {
        assertEquals(
            "8:00 PM · 3 days before",
            ReminderSummary.of(state(notificationTime = LocalTime.of(20, 0))),
        )
    }

    @Test
    fun `an offset with no chip still says how many days`() {
        // 5 days is storable, so the line has to be truthful about it rather
        // than blank or wrong.
        assertEquals(
            "9:00 AM · 5 days before",
            ReminderSummary.of(state(advanceNotificationDays = 5)),
        )
    }

    @Test
    fun `the wizard's lead-time chips are the advance ones only`() {
        // On the day is available per person, but this step is about being
        // reminded earlier, so it offers the three advance values.
        assertEquals(
            listOf("1 day", "3 days", "1 week"),
            LeadTime.options.map { it.label },
        )
    }

    @Test
    fun `an offset the chips omit falls back to the shortest reminder`() {
        // 0 means "on the day" and is not a chip here. Falling back to 3 days
        // would move someone's reminder earlier without them asking.
        assertEquals(LeadTime.ONE_DAY, LeadTime.of(0))
    }

    @Test
    fun `the wizard and the card start on the same tone`() {
        // Two different defaults would mean the tone the user picked on the
        // last step is not the tone they see.
        assertEquals(CardTone.DEFAULT, AddEditBirthdayUiState().cardTone)
    }

    @Test
    fun `an unset tone falls back rather than failing`() {
        // The stored value is a free string, so it can be absent or written by
        // a newer build. Neither may stop a card from opening.
        assertEquals(CardTone.DEFAULT, CardTone.fromName(null))
        assertEquals(CardTone.DEFAULT, CardTone.fromName(""))
        assertEquals(CardTone.DEFAULT, CardTone.fromName("SASSY"))
    }

    @Test
    fun `a stored tone name resolves`() {
        assertEquals(CardTone.FUNNY, CardTone.fromName("FUNNY"))
        assertEquals(CardTone.SHORT, CardTone.fromName(" short "))
    }
}
