package com.birthdayreminder.ui.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rules for what appears on a shared card.
 *
 * These are behavioural, not cosmetic: a card is forwarded to someone else, so
 * a wrong line, a duplicate re-roll, or a line that implies a streak the app
 * cannot substantiate all reach a third party.
 */
class CardMessageTest {
    private val name = "Amma"
    private val age = 53
    private val zodiac = "Pisces"

    @Test
    fun `a saved personal note wins over a generated line`() {
        val result = CardMessagePicker.initial(name, age, zodiac, "Call you tonight")

        assertEquals("Call you tonight", result.text)
        assertTrue(result.isPersonalNote)
    }

    @Test
    fun `a blank note is treated as no note`() {
        val result = CardMessagePicker.initial(name, age, zodiac, "   ")

        assertFalse(result.isPersonalNote)
        assertTrue(result.text.isNotBlank())
    }

    @Test
    fun `a null note falls back to the default tone`() {
        val result = CardMessagePicker.initial(name, age, zodiac, null)

        assertEquals(CardTone.DEFAULT, result.tone)
    }

    @Test
    fun `a note is trimmed`() {
        val result = CardMessagePicker.initial(name, age, zodiac, "  happy birthday  ")

        assertEquals("happy birthday", result.text)
    }

    @Test
    fun `reroll always changes the line`() {
        val first = CardMessagePicker.initial(name, age, zodiac, null, CardTone.SHORT)
        var current = first
        var seed = 0

        repeat(20) {
            val next = CardMessagePicker.reroll(current, name, age, zodiac, seed)
            assertNotEquals("reroll $seed returned the same line", current.text, next.text)
            current = next
            seed++
        }
    }

    @Test
    fun `reroll keeps the tone`() {
        val first = CardMessagePicker.initial(name, age, zodiac, null, CardTone.FUNNY)
        val next = CardMessagePicker.reroll(first, name, age, zodiac, 3)

        assertEquals(CardTone.FUNNY, next.tone)
    }

    @Test
    fun `reroll keeps the name and age out of the wrong tone`() {
        val start = CardMessagePicker.initial(name, age, zodiac, null, CardTone.SINCEREST)

        repeat(10) { seed ->
            val line = CardMessagePicker.reroll(start, name, age, zodiac, seed).text
            val sincerity = CardTone.forTone(CardTone.SINCEREST, name, age, zodiac)
            assertTrue("'$line' is not a Sincerest line", line in sincerity)
        }
    }

    @Test
    fun `switching tone returns a line from the new tone`() {
        val start = CardMessagePicker.initial(name, age, zodiac, null, CardTone.SHORT)

        CardTone.entries.forEach { tone ->
            val switched = CardMessagePicker.withTone(start, tone, name, age, zodiac)
            val pool = CardTone.forTone(tone, name, age, zodiac)
            assertTrue("'$tone' produced '${switched.text}'", switched.text in pool)
            assertEquals(tone, switched.tone)
        }
    }

    @Test
    fun `every tone offers a choice of lines`() {
        CardTone.entries.forEach { tone ->
            val pool = CardTone.forTone(tone, name, age, zodiac)
            assertTrue("$tone has only ${pool.size} lines", pool.size >= 4)
            assertEquals("$tone has duplicate lines", pool.size, pool.distinct().size)
        }
    }

    @Test
    fun `no line is long enough to overrun the card`() {
        // The card is a fixed aspect ratio with a header and footer already
        // committed. A message that needs more than four lines at the card's
        // text size gets ellipsised, which loses the ending - and the ending is
        // usually the point of the line.
        //
        // The card's inner width is 300dp less 32dp padding each side, at
        // roughly 16sp Fraunces that is about 42 characters per line.
        val maxChars = 4 * 42

        CardTone.entries.forEach { tone ->
            CardTone.forTone(tone, "Bartholomew Fitzgerald-Montgomery", 41, zodiac).forEach { line ->
                assertTrue(
                    "$tone line is ${line.length} chars, over $maxChars: $line",
                    line.length <= maxChars,
                )
            }
        }
    }

    @Test
    fun `no line claims a reminder streak`() {
        // The database has no reminder history, so any line implying one would
        // be a fabricated fact on a card the recipient may screenshot.
        val banned = listOf("remembered", "streak", "last 11", "years running", "every year you remembered")

        CardTone.entries.forEach { tone ->
            CardTone.forTone(tone, name, age, zodiac).forEach { line ->
                val lower = line.lowercase()
                banned.forEach { word ->
                    assertFalse("$tone line says '$word': $line", lower.contains(word))
                }
            }
        }
    }

    @Test
    fun `funny lines that use the age use this year's age`() {
        val pool = CardTone.forTone(CardTone.FUNNY, name, 53, zodiac)

        assertTrue(
            "no funny line references the age, so it is not personalised",
            pool.any { it.contains("53") },
        )
    }

    @Test
    fun `lines that use the name use this name`() {
        CardTone.entries.forEach { tone ->
            CardTone.forTone(tone, "Karthik", 33, zodiac).forEach { line ->
                // A line may or may not name them, but must never name someone else.
                assertFalse("$tone line names the wrong person: $line", line.contains("Amma"))
            }
        }
    }

    @Test
    fun `the note does not survive a tone switch made by the user`() {
        // Regression: the screen recomputed the message whenever the tone
        // changed, which handed the saved note straight back and left the card
        // looking broken - Funny selected, Sincerest line still showing. Once
        // the user picks a tone, the note must not override them.
        val note = CardMessagePicker.initial(name, age, zodiac, "Call you tonight")
        val switched =
            CardMessagePicker.initial(
                name = name,
                ageTurning = age,
                zodiac = zodiac,
                // what the screen passes after a tone change
                personalNote = null,
                tone = CardTone.FUNNY,
            )

        assertNotEquals(note.text, switched.text)
        assertEquals(CardTone.FUNNY, switched.tone)
    }

    @Test
    fun `a personal note is dropped once the tone is switched`() {
        val note = CardMessagePicker.initial(name, age, zodiac, "Call you tonight")
        val switched = CardMessagePicker.withTone(note, CardTone.FUNNY, name, age, zodiac)

        assertFalse(switched.isPersonalNote)
        assertNotEquals("Call you tonight", switched.text)
    }

    @Test
    fun `a negative seed does not index out of bounds`() {
        CardTone.entries.forEach { tone ->
            val start = CardMessagePicker.initial(name, age, zodiac, null, tone)
            val next = CardMessagePicker.reroll(start, name, age, zodiac, -7)

            assertTrue(next.text.isNotBlank())
        }
    }

    @Test
    fun `an absurdly large seed does not index out of bounds`() {
        val start = CardMessagePicker.initial(name, age, zodiac, null, CardTone.WARM)
        val next = CardMessagePicker.reroll(start, name, age, zodiac, Int.MAX_VALUE)

        assertTrue(next.text.isNotBlank())
    }
}
