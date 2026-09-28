package com.birthdayreminder.ui.card

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Deterministic, birth-data-derived gradient for the shareable birthday card.
 *
 * The card is a *gift to the recipient*, so it has to look good enough to send
 * to someone. That creates a hard constraint: the gradient carries dark ink,
 * and a colourful gradient can easily push contrast under WCAG AA.
 *
 * ## The rule
 *
 * Variety comes from **hue only**. Lightness is pinned to a narrow band whose
 * *darkest* stop still clears [LUMINANCE_FLOOR]. This is the lesson from the v2
 * prototype, where the card quote measured 1.71:1 against a dark gradient end
 * before the ramp was reoriented.
 *
 * A test walks all 372 month/day seeds and asserts the invariant, so the
 * constraint cannot silently regress as colours are tuned.
 *
 * Colours are built in HSL rather than HCT: HSL maps directly onto the
 * lightness knob this file needs, and it ships with Compose without a
 * dependency. Saturation stays low so the result reads as tinted paper stock
 * rather than a saturated graphic.
 */
object CardGradient {
    /**
     * Darkest allowed relative luminance for a gradient stop.
     *
     * Dark ink on these gradients needs roughly this much luminance behind it
     * to clear 4.5:1, so it is enforced rather than eyeballed.
     */
    const val LUMINANCE_FLOOR: Double = 0.38

    /** Contrast the card's body ink must achieve against its own stop. */
    const val MIN_CONTRAST: Double = 4.5

    private const val MAX_SATURATION = 0.42f

    /**
     * Width of the hue arc the generated gradients are allowed to roam.
     *
     * The first implementation hashed the seed with the golden angle, which
     * spreads every person across the whole colour wheel. That produced lilac
     * cards in a saffron app - a Libra born in October landed on violet, and
     * the artefact that is supposed to look designed looked arbitrary.
     *
     * The palette is a deliberate one: saffron, rosewood and plum are
     * neighbouring warm hues. Variety within a narrow arc reads as a family;
     * variety across the wheel reads as random. So the hue is centred on
     * saffron and only wanders a limited distance from it.
     */
    private const val HUE_CENTER = 10f

    /**
     * Total span of the arc, in degrees.
     *
     * The range is chosen to cover exactly the app's three hue families -
     * saffron through rosewood to plum - and no further. A wider arc reaches
     * green and cyan, which reads as a different app's palette entirely.
     */
    private const val HUE_ARC = 96f

    /**
     * The seed's own range, so it can be mapped linearly onto the arc.
     *
     * month * 31 + day puts Jan 1 at 32 and Dec 31 at 403. Using the true
     * range rather than a modulo is what keeps every one of the 372 seeds on
     * its own hue.
     */
    private const val MIN_SEED = 32f
    private const val MAX_SEED = 403f

    // Lightness steps for the three stops, lightest to darkest.
    private const val L_LIGHTEST = 0.88f
    private const val L_MIDDLE = 0.76f
    private const val L_DARKEST = 0.66f

    private const val LIGHTNESS_STEP = 0.04f
    private const val MAX_LIGHTNESS_STEPS = 12

    /**
     * Builds the three-stop gradient for a person's birthday.
     *
     * The seed is month and day only — the year is deliberately excluded so
     * two people born on the same day share a colour, which reads as
     * intentional rather than random.
     *
     * @param birthDate the person's birth date; only month and day are read
     * @return three colours ordered lightest to darkest, for a top-left to
     *   bottom-right [androidx.compose.ui.graphics.Brush.linearGradient]
     */
    fun forBirthDate(birthDate: LocalDate): List<Color> {
        val seed = birthDate.monthValue * 31 + birthDate.dayOfMonth
        // Map the seed range straight onto the arc, with no modulo. Bucketing
        // by `seed % n` collapses birthdays onto the same colour, and with a
        // 31-day month stride `seed % 31` sends every month to the same
        // bucket, which made all twelve months render one colour. A linear
        // map keeps all 372 seeds distinct and spreads the months evenly.
        val spread = (seed - MIN_SEED) / (MAX_SEED - MIN_SEED).toFloat() - 0.5f
        val hue = HUE_CENTER + spread * HUE_ARC
        val saturation = 0.26f + (seed % 5) * 0.035f // 0.26..0.40
        return forHueAndSaturation(hue, saturation)
    }

    /**
     * Builds the ramp for an explicit hue and saturation, clamped into the
     * safe band.
     *
     * Exposed separately so the clamping can be tested against hostile inputs
     * (0 or 360 hue, negative saturation, huge saturation) without having to
     * find a birth date that produces them.
     *
     * @param hue base hue in degrees; wrapped into 0 until less than 360
     * @param saturation HSL saturation; clamped to 0 through [MAX_SATURATION]
     * @return three colours ordered lightest to darkest
     */
    fun forHueAndSaturation(
        hue: Float,
        saturation: Float,
    ): List<Color> {
        val safeHue = wrapHue(hue)
        val safeSaturation = saturation.coerceIn(0f, MAX_SATURATION)

        return listOf(L_LIGHTEST, L_MIDDLE, L_DARKEST).map { lightness ->
            // Raise lightness, holding hue, until the stop clears the floor.
            // Dropping saturation alongside it matters: some hues stay dark
            // while saturated even at high lightness, and the floor would not hold.
            var candidateLightness = lightness
            var candidateSaturation = safeSaturation
            var guard = 0
            while (
                relativeLuminance(hsl(safeHue, candidateSaturation, candidateLightness)) < LUMINANCE_FLOOR &&
                guard < MAX_LIGHTNESS_STEPS
            ) {
                candidateLightness = (candidateLightness + LIGHTNESS_STEP).coerceAtMost(1f)
                candidateSaturation = (candidateSaturation * 0.94f).coerceAtLeast(0f)
                guard++
            }
            hsl(safeHue, candidateSaturation, candidateLightness)
        }
    }

    /**
     * WCAG 2.1 relative luminance of an opaque colour.
     *
     * @param color the colour to measure
     * @return luminance in 0.0..1.0
     */
    fun relativeLuminance(color: Color): Double {
        fun linearise(value: Float): Double {
            val c = value.toDouble().coerceIn(0.0, 1.0)
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linearise(color.red) +
            0.7152 * linearise(color.green) +
            0.0722 * linearise(color.blue)
    }

    /**
     * WCAG 2.1 contrast ratio between two opaque colours.
     *
     * @param a first colour
     * @param b second colour
     * @return ratio in 1.0..21.0
     */
    fun contrastRatio(
        a: Color,
        b: Color,
    ): Double {
        val lighter = max(relativeLuminance(a), relativeLuminance(b))
        val darker = min(relativeLuminance(a), relativeLuminance(b))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun hsl(
        hue: Float,
        saturation: Float,
        lightness: Float,
    ): Color =
        Color.hsl(
            hue = wrapHue(hue),
            saturation = saturation.coerceIn(0f, 1f),
            lightness = lightness.coerceIn(0f, 1f),
        )

    private fun wrapHue(hue: Float): Float = ((hue % 360f) + 360f) % 360f
}
