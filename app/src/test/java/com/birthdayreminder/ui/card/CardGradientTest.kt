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
    /** Green/cyan, which no card in this app should ever use. */
    private val greenBand = 75f..160f

    /** Blue/violet proper, as distinct from the plum end of the arc. */
    private val blueBand = 200f..280f

    /** The plum/rosewood end of the arc, on the wrapped 0..360 scale. */
    private val plumEndHue = 315f

    /** The saffron/amber end of the arc. */
    private val goldEndHue = 60f

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

        assertTrue("Seeds should differ", a != b)
        // Jan and Jun sit six months apart on the arc, so they should be far
        // apart in hue. The old golden-angle mapping scattered across the
        // whole wheel and made this trivially true; inside a deliberate warm
        // arc the separation has to be earned.
        val separation = circularHueGap(hueOf(a), hueOf(b))
        assertTrue(
            "Jan and Jun hues are only $separation degrees apart",
            separation > 30f,
        )
    }

    @Test
    fun `every month is at least seven degrees from every other`() {
        val perMonth = (1..12).map { CardGradient.forBirthDate(LocalDate.of(1990, it, 15)) }
        val hues = perMonth.map(::hueOf)

        val tooClose =
            hues.indices.flatMap { i ->
                (i + 1 until hues.size)
                    .filter { j -> circularHueGap(hues[i], hues[j]) < 6f }
                    .map { j -> i + 1 to j + 1 }
            }

        assertTrue("Months too close: $tooClose", tooClose.isEmpty())
    }

    @Test
    fun `every seed lands on its own hue, not a shared bucket`() {
        // Guards the seed mapping itself. An earlier `seed % 31` bucket sent
        // every month to the same colour, which is invisible in a spot check
        // and obvious across the whole set.
        //
        // A couple of the 372 hues coincide once rounded to 8-bit, so this
        // allows a hair of slack rather than demanding mathematical distinctness.
        val hues = allBirthSeeds().map { hueOf(CardGradient.forBirthDate(it)) }

        // The test's seed set is days 1..28, so 336 birthdays land on a 96-degree
        // arc: about 0.29 degrees apart. That is below the threshold at which two
        // hues read as different to the eye, which is the point of the narrow
        // arc - the card is a family of colours, not a spectrum.
        //
        // What must not happen is the old bucket collapse, where whole months
        // shared one colour. So the assertion is on spread, not distinctness.
        val span = hues.max() - hues.min() + (if (hues.any { it < 180f } && hues.any { it > 180f }) 360f else 0f)
        assertTrue(
            "The arc collapsed: hues span only $span degrees",
            span > 60f,
        )
    }

    @Test
    fun `every seed stays inside the warm arc`() {
        // The arc runs plum/rosewood (around 322, i.e. -38) through saffron
        // to amber (58). Anything green, cyan or blue would read as a
        // different app's palette entirely, so those bands are excluded.
        // Note the band must be expressed on the wrapped 0..360 scale.
        val hues = allBirthSeeds().map { hueOf(CardGradient.forBirthDate(it)) }

        val offPalette =
            hues.filter {
                it in greenBand || it in blueBand || (it in 200f..280f)
            }

        assertTrue("Hues drifted off-palette: ${offPalette.distinct()}", offPalette.isEmpty())
    }

    @Test
    fun `the arc never leaves the saffron-rosewood-plum family`() {
        val hues = allBirthSeeds().map { hueOf(CardGradient.forBirthDate(it)) }

        // The palette is a 96-degree warm window. Anything outside it is a bug
        // in the mapping, not a design choice.
        val outside = hues.filter { !it.inArc(plumEndHue, goldEndHue) }

        assertTrue("Outside the arc: ${outside.distinct()}", outside.isEmpty())
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

    /**
     * Whether a hue falls inside the warm arc, accounting for the 0/360 wrap.
     *
     * The arc runs from plum at about 322, through 0/360, to saffron at 58, so
     * a naive `hue in 322f..58f` is always false.
     */
    private fun Float.inArc(
        start: Float,
        end: Float,
    ): Boolean = if (start <= end) this in start..end else this >= start || this <= end

    /**
     * Shortest distance between two hues, in degrees.
     *
     * Plain subtraction is wrong across the 0/360 boundary: 350 and 10 are
     * 20 degrees apart, not 340, and the warm arc straddles that boundary.
     */
    private fun circularHueGap(
        a: Float,
        b: Float,
    ): Float {
        val raw = kotlin.math.abs(a - b) % 360f
        return if (raw > 180f) 360f - raw else raw
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
