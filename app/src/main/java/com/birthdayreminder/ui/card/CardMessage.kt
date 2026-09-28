package com.birthdayreminder.ui.card

import java.time.LocalDate

/**
 * The tone of the message written on a card.
 *
 * The card exists to be forwarded, so the sender needs to be able to shape how
 * it reads without rewriting the card from scratch. Each tone is a small bank
 * of lines; picking one swaps the pool, and re-rolling draws again from the
 * current pool so the *tone* never drifts while the *wording* changes.
 *
 * Two rules, both learned the hard way:
 *
 * - **Re-rolling must not change the colours.** The gradient is derived from
 *   the birth date and is part of what makes the card feel personal; a card
 *   whose colour jumps on every tap stops being an artefact and starts being a
 *   slot machine. Tone only ever changes [linesFor].
 * - **Every line must be true or clearly affectionate.** These are not
 *   fabricated facts about the recipient - they carry no claims that the
 *   database cannot back. See the streak discussion in the implementation
 *   plan: the app has no reminder history, so nothing here may imply one.
 */
enum class CardTone(
    /** Label on the chip. */
    val label: String,
) {
    WARM("Warm"),
    FUNNY("Funny"),
    SINCEREST("Sincerest"),
    SHORT("Short"),
    ;

    companion object {
        /**
         * The tone selected when the card is first opened.
         *
         * Sincerest: it is the safest default, because a card that oversells
         * is worse than one that simply says something kind.
         */
        val DEFAULT: CardTone = SINCEREST

        /**
         * Lines that stand on their own, used when the saved personal note is
         * the thing the sender wants on the card.
         */
        fun forTone(
            tone: CardTone,
            name: String,
            ageTurning: Int,
            zodiac: String,
        ): List<String> =
            when (tone) {
                CardTone.WARM -> warm(name, ageTurning)
                CardTone.FUNNY -> funny(ageTurning, zodiac)
                CardTone.SINCEREST -> sincerest(name)
                CardTone.SHORT -> short(name, ageTurning)
            }

        private fun warm(
            name: String,
            ageTurning: Int,
        ) = listOf(
            "Another trip around the sun, and you only get more yourself.",
            "Here's to a year as good as the one you're finishing.",
            "$name, we saved you the good seat. Sit down, you're $ageTurning today.",
            "The years have been good to you. Let's keep them that way.",
            "Happy birthday. May this one be as kind to you as you are to everyone else.",
            "Another year of you being reliably excellent. Thank you for that.",
        )

        private fun funny(
            ageTurning: Int,
            zodiac: String,
        ) = listOf(
            "Congratulations on reaching an advanced age. The discount is permanent.",
            "$ageTurning. Still under the age where the app starts asking follow-up questions.",
            "Your $zodiac sign says you're due for a nap. Your age says you already know that.",
            "New year, same excellent hair. We call that a strong trend.",
            "Birthdays are just a birthday for a year with excellent branding.",
            "You've been alive for $ageTurning years. A frankly unreasonable number of Tuesdays.",
        )

        private fun sincerest(name: String) =
            listOf(
                "Some people make a room better just by walking in. You always have.",
                "Every year you somehow get more patient. That's the whole trick, isn't it?",
                "You make it very easy to love you. I hope you know that.",
                "Here's to you, $name. Genuinely. Thank you for being so reliable.",
                "The world is better for having you in it, and I get to see that.",
                "I'm grateful it's you. That's the whole message.",
            )

        private fun short(
            name: String,
            ageTurning: Int,
        ) = listOf(
            "Happy birthday, $name.",
            "$ageTurning. Looking good.",
            "Today is yours.",
            "Happy birthday.",
            "Make it a good one.",
            "Here's to you, $name.",
        )
    }
}

/**
 * A message on the card, plus the tone it came from.
 *
 * Carrying the tone alongside the line lets the UI show which pool is
 * currently active when a re-roll happens, so the user can tell the difference
 * between "same tone, new words" and "the tone changed".
 */
data class CardMessage(
    val text: String,
    val tone: CardTone,
    /**
     * True when the line is the saved personal note rather than a generated
     * one. The card renders the note verbatim and offers re-rolling on top of
     * it, because the sender's own words are usually the best ones.
     */
    val isPersonalNote: Boolean = false,
)

/**
 * Chooses and re-rolls card messages.
 *
 * Kept as an object with no Compose dependency so the selection rules can be
 * unit tested directly - a card that shows the same line twice in a row, or
 * shows a line from the wrong tone, is exactly the kind of bug that only
 * shows up on someone's birthday.
 */
object CardMessagePicker {
    /**
     * Picks the opening message.
     *
     * The sender's own note wins when there is one. It is the most personal
     * thing available and it is what the user actually typed, so replacing it
     * with a generated line by default would be presumptuous.
     *
     * @param name recipient's name
     * @param ageTurning the age they turn on this birthday
     * @param zodiac their zodiac sign
     * @param personalNote the note saved against the birthday, if any
     * @param tone the tone to draw from when there is no note
     * @param seed a value that varies the opening pick; pass a stable one to
     *   get a repeatable result
     * @return the message to show
     */
    fun initial(
        name: String,
        ageTurning: Int,
        zodiac: String,
        personalNote: String?,
        tone: CardTone = CardTone.DEFAULT,
        seed: Int = 0,
    ): CardMessage {
        if (!personalNote.isNullOrBlank()) {
            return CardMessage(text = personalNote.trim(), tone = tone, isPersonalNote = true)
        }
        val pool = CardTone.forTone(tone, name, ageTurning, zodiac)
        return CardMessage(text = pool[floorMod(seed, pool.size)], tone = tone)
    }

    /**
     * Draws a different line from the same tone.
     *
     * Deterministic in [seed] so a re-roll can be unit tested, but the caller
     * passes an incrementing value in the app so each tap moves on.
     *
     * @param current the message being replaced
     * @param name recipient's name
     * @param ageTurning the age they turn on this birthday
     * @param zodiac their zodiac sign
     * @param seed varies the pick; must differ between re-rolls to change the line
     * @return a new message, always different from [current] where the pool allows
     */
    fun reroll(
        current: CardMessage,
        name: String,
        ageTurning: Int,
        zodiac: String,
        seed: Int,
    ): CardMessage {
        val pool = CardTone.forTone(current.tone, name, ageTurning, zodiac)
        if (pool.size < 2) return current
        // Start one past the current line so a single tap always changes it.
        val start = floorMod(seed, pool.size)
        val candidate = pool[start].takeIf { it != current.text } ?: pool[(start + 1) % pool.size]
        return CardMessage(text = candidate, tone = current.tone)
    }

    /**
     * Switches tone, keeping the current line where the new pool has it.
     *
     * Switching should feel responsive: if the same sentence also exists in
     * the new tone, keep it, otherwise draw that tone's first line.
     *
     * @param current the message on screen now
     * @param tone the tone being switched to
     * @param name recipient's name
     * @param ageTurning the age they turn on this birthday
     * @param zodiac their zodiac sign
     * @return a message in [tone]
     */
    fun withTone(
        current: CardMessage,
        tone: CardTone,
        name: String,
        ageTurning: Int,
        zodiac: String,
    ): CardMessage {
        val pool = CardTone.forTone(tone, name, ageTurning, zodiac)
        val kept = pool.firstOrNull { it == current.text }
        return CardMessage(text = kept ?: pool.first(), tone = tone, isPersonalNote = false)
    }

    /** Non-negative modulo, unlike Kotlin's `%` which keeps the dividend's sign. */
    private fun floorMod(
        value: Int,
        modulus: Int,
    ): Int = ((value % modulus) + modulus) % modulus
}

/**
 * The zodiac sign for a birth date, for use in generated lines.
 *
 * Wraps the existing domain helper so message code does not need to import it
 * directly, and so the sign used in a line is the same one shown on the card.
 */
internal fun zodiacFor(birthDate: LocalDate): String =
    com.birthdayreminder.domain.util.ZodiacUtils.getZodiacSign(birthDate.month, birthDate.dayOfMonth)
