package com.birthdayreminder.ui.card

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * Guards the card gradient's core invariant.
 *
 * The card is shared into WhatsApp, so its text has to stay legible for a
 * recipient who has never seen the app. The v2 prototype shipped a 1.71:1
 * quote on a dark gradient before this was caught by eye; these tests make it
 * a build failure instead.
 */
class CardGradientTest {
    /** The ink the card draws its body copy in. */
    private val cardInk = Color(0xFF1A1A17)

    /**
     * Every month/day combination, which is the entire input space of
     * [CardGradient.forBirthDate].
     */
    private fun allBirthSeeds(): List<LocalDate> =
        buildList {
            for (month in 1..12) {
                for (day in 1..28) {
                    add(LocalDate.of(1990, month, day))
                }
            }
        }

    @Test
    fun `every seed keeps the darkest stop above the luminance floor`() {
        val failures =
            allBirthSeeds().mapNotNull { date ->
                val darkest = CardGradient.forBirthDate(date).minBy { CardGradient.relativeLuminance(it) }
                val lum = CardGradient.relativeLuminance(darkest)
                if (lum < CardGradient.LUMINANCE_FLOOR) "$date -> $lum" else null
            }

        assertTrue(
            "Stops below the floor:\n${failures.take(10).joinToString("\n")}",
            failures.isEmpty(),
        )
    }

    @Test
    fun `card ink clears AA against every stop of every seed`() {
        val failures =
            allBirthSeeds().mapNotNull { date ->
                CardGradient.forBirthDate(date).mapNotNull { stop ->
                    val ratio = CardGradient.contrastRatio(cardInk, stop)
                    if (ratio < CardGradient.MIN_CONTRAST) "$date -> ${"%.2f".format(ratio)}" else null
                }
            }.flatten()

        assertTrue(
            "Stops under 4.5:1:\n${failures.take(10).joinToString("\n")}",
            failures.isEmpty(),
        )
    }

    @Test
    fun `the year does not change the gradient`() {
        val early = CardGradient.forBirthDate(LocalDate.of(1975, 3, 14))
        val late = CardGradient.forBirthDate(LocalDate.of(2004, 3, 14))

        assertEquals(early, late)
    }

    @Test
    fun `the same date always produces the same gradient`() {
        val date = LocalDate.of(1991, 9, 27)

        assertEquals(CardGradient.forBirthDate(date), CardGradient.forBirthDate(date))
    }

    @Test
    fun `different dates produce visibly different gradients`() {
        val a = CardGradient.forBirthDate(LocalDate.of(1990, 1, 1))
        val b = CardGradient.forBirthDate(LocalDate.of(1990, 6, 15))

        // The golden-angle seed should separate these well beyond rounding noise.
        assertTrue("Seeds should differ", a != b)
        assertTrue(
            "Hues should differ by at least 60 degrees",
            kotlin.math.abs(hueOf(a) - hueOf(b)) > 60f,
        )
    }

    @Test
    fun `stops are ordered lightest to darkest`() {
        for (date in allBirthSeeds().take(60)) {
            val stops = CardGradient.forBirthDate(date)
            val lums = stops.map { CardGradient.relativeLuminance(it) }
            assertTrue(
                "$date was not monotonic: $lums",
                lums.zipWithNext().all { (a, b) -> a > b },
            )
        }
    }

    @Test
    fun `hostile hues and saturations are still clamped into the band`() {
        val hostile =
            listOf(
                0f to 0f,
                360f to 1f,
                -180f to -0.5f,
                720.5f to 0.42f,
                180f to 5f,
            )

        for ((hue, saturation) in hostile) {
            for (stop in CardGradient.forHueAndSaturation(hue, saturation)) {
                assertTrue(
                    "hue=$hue sat=$saturation produced $stop",
                    CardGradient.relativeLuminance(stop) >= CardGradient.LUMINANCE_FLOOR,
                )
            }
        }
    }

    @Test
    fun `leap day is handled without crashing`() {
        val stops = CardGradient.forBirthDate(LocalDate.of(1992, 2, 29))
        assertEquals(3, stops.size)
    }

    @Test
    fun `all twelve months produce distinct stops`() {
        val perMonth =
            (1..12).map { month ->
                CardGradient.forBirthDate(LocalDate.of(1990, month, 15))
            }
        val distinct = perMonth.toSet()

        assertEquals("Each month should read differently", 12, distinct.size)
    }

    private fun hueOf(stops: List<Color>): Float {
        val r = stops[1].red
        val g = stops[1].green
        val b = stops[1].blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        if (delta == 0f) return 0f
        val hue =
            when (max) {
                r -> 60f * (((g - b) / delta) % 6f)
                g -> 60f * (((b - r) / delta) + 2f)
                else -> 60f * (((r - g) / delta) + 4f)
            }
        return (hue + 360f) % 360f
    }

    @Test
    fun `months named by enum produce the same seed as their numbers`() {
        assertEquals(
            CardGradient.forBirthDate(LocalDate.of(1990, Month.JULY.value, 4)),
            CardGradient.forBirthDate(LocalDate.of(2001, 7, 4)),
        )
    }
}
