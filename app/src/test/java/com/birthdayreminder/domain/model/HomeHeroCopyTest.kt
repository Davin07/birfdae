package com.birthdayreminder.domain.model

import com.birthdayreminder.domain.usecase.ReminderStreak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The hero is the one screen that makes a claim about the user's behaviour, so
 * these tests are mostly about what it must *not* say.
 *
 * A streak that reads "1 of 1" on a fresh install is true and worthless, and
 * dressing a new user up with a track record is the exact failure §6 of the
 * plan was written to prevent.
 */
class HomeHeroCopyTest {
    private fun person(
        id: Long,
        name: String,
        age: Int,
    ) = HeroPerson(birthdayId = id, name = name, ageTurning = age)

    private fun streak(
        name: String = "Amma",
        streakYears: Int = 11,
        trackedYears: Int = 11,
        sharedYears: Int = 0,
        firstTrackedYear: Int = 2015,
    ) = ReminderStreak(
        birthdayId = 1,
        name = name,
        streakYears = streakYears,
        trackedYears = trackedYears,
        sharedYears = sharedYears,
        firstTrackedYear = firstTrackedYear,
    )

    @Test
    fun `no birthdays means no hero, rather than an empty shell`() {
        val hero = HomeHeroCopy.build(dueOn = emptyList(), nextUp = null, streak = null)

        assertNull("A hero with nobody in it is worse than none", hero)
    }

    @Test
    fun `a birthday today is the Today eyebrow`() {
        val hero = HomeHeroCopy.build(dueOn = listOf(person(1, "Amma", 55)), nextUp = null, streak = null)

        assertEquals("Today", hero?.eyebrow)
        assertEquals("Amma", hero?.headline)
        assertTrue(hero?.isToday == true)
    }

    @Test
    fun `nothing today falls back to the Coming up eyebrow`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = emptyList(),
                nextUp = person(2, "Karthik", 33),
                streak = null,
            )

        // "Coming up" was a section heading that introduced nothing, and the
        // hero's own eyebrow said "Next up" while the list heading below said
        // it again. The heading is gone, so the eyebrow takes the freed word
        // and no single word labels two different things on this screen.
        assertEquals("Coming up", hero?.eyebrow)
        assertEquals("Karthik", hero?.headline)
        assertFalse(hero!!.isToday)
    }

    @Test
    fun `the supporting line does not repeat the eyebrow`() {
        val withStreak =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55)),
                nextUp = null,
                streak = streak("Amma", 3, 4),
            )
        val withoutHistory =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55)),
                nextUp = null,
                streak = null,
            )

        // The line used to read "Today turns 55. You've remembered 3 of Amma's
        // last 4." with "Today" in the eyebrow directly above it.
        for (hero in listOfNotNull(withStreak, withoutHistory)) {
            val eyebrowWord = hero.eyebrow.lowercase()
            assertFalse(
                "supporting line repeats the eyebrow: ${hero.supportingLine}",
                hero.supportingLine.lowercase().startsWith(eyebrowWord),
            )
        }
        // The age stays -- it is the fact the line exists to carry. What
        // went is the "Today"/"Coming up" lead in front of it.
        assertEquals("turns 55. You've remembered 3 of Amma's last 4.", withStreak?.supportingLine)
    }

    @Test
    fun `two birthdays on the same day are named together`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55), person(2, "Appa", 57)),
                nextUp = null,
                streak = null,
            )

        assertEquals(
            "The prototype led with \"Amma & Appa share a birthday\", and hiding the second name hides the point",
            "Amma & Appa",
            hero?.headline,
        )
        assertTrue(hero!!.supportingLine.contains("55 and 57"))
    }

    @Test
    fun `a real streak is stated as a streak`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55)),
                nextUp = null,
                streak = streak(streakYears = 11, trackedYears = 11),
            )

        assertTrue(
            "The streak is the emotional core and should appear: ${hero?.supportingLine}",
            hero!!.supportingLine.contains("remembered 11 of Amma's last 11"),
        )
    }

    @Test
    fun `a single tracked year does not claim a streak`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55)),
                nextUp = null,
                streak = streak(streakYears = 1, trackedYears = 1, firstTrackedYear = 2026),
            )

        assertFalse(
            "A fresh install must not be told it has a track record: ${hero?.supportingLine}",
            hero!!.supportingLine.contains("remembered"),
        )
        assertTrue(
            "It should fall back to a fact about the relationship instead",
            hero.supportingLine.contains("On your list since 2026"),
        )
    }

    @Test
    fun `a broken streak is still shown honestly`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = listOf(person(1, "Amma", 55)),
                nextUp = null,
                streak = streak(streakYears = 4, trackedYears = 11),
            )

        assertTrue(
            "Understating would be its own lie: ${hero?.supportingLine}",
            hero!!.supportingLine.contains("remembered 4 of Amma's last 11"),
        )
    }

    @Test
    fun `the action is for the first person named`() {
        val hero =
            HomeHeroCopy.build(
                dueOn = listOf(person(7, "Amma", 55), person(8, "Appa", 57)),
                nextUp = null,
                streak = null,
            )

        assertEquals(7L, hero?.primaryBirthdayId)
    }
}
