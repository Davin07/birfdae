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
 * to someone. That creates a hard constraint: the gradient carries ink, and a
 * colourful gradient can easily push contrast under WCAG AA.
 *
 * ## The rule
 *
 * Variety comes from **hue only**. Lightness is pinned to a narrow band whose
 * *darkest* stop still clears the ink-contrast floor. This is the lesson from
 * the v2 prototype, where the card quote measured 1.71:1 against a dark
 * gradient end before the ramp was reoriented.
 *
 * A test walks all 372 month/day seeds and asserts the invariant, so the
 * constraint cannot silently regress as colours are tuned.
 *
 * Colours are built in HSL rather than HCT: HSL maps directly onto the
 * lightness knob this file needs, and it ships with Compose without a
 * dependency.
 *
 * ## Two ramps, not one
 *
 * The concept defines this card in both themes, and so does this: a card in
 * dark mode is deep amber with light ink, not a cream one with dark ink. The
 * hue logic is shared -- both bands sit in the same warm family, H 27..37 --
 * so only the lightness band, the floor and the ink change between them.
 *
 * The earlier single light band was a deliberate, and wrong, call: capping
 * saturation at 0.42 to read as "tinted paper stock" turned the approved gold
 * ramp into beige. The concept's own light stops sit at S 0.73..1.00.
 */
object CardGradient {
    /**
     * Darkest allowed relative luminance for a stop in the **light** ramp.
     *
     * Dark ink on these gradients needs roughly this much luminance behind it
     * to clear 4.5:1, so it is enforced rather than eyeballed.
     */
    const val LUMINANCE_FLOOR: Double = 0.38

    /**
     * Brightest allowed relative luminance for a stop in the **dark** ramp.
     *
     * The mirror of [LUMINANCE_FLOOR]. Light ink needs a ceiling rather than a
     * floor, and capping it is what keeps the darkest warm hue from washing
     * out into the card's own page background.
     */
    const val DARK_LUMINANCE_CEILING: Double = 0.13

    /** Contrast the card's body ink must achieve against its own stop. */
    const val MIN_CONTRAST: Double = 4.5

    /**
     * Ceiling on generated saturation.
     *
     * Was 0.42, which desaturated the approved gold ramp into beige. The
     * concept's own stops reach S 1.00, so this leaves room for the seeded
     * ramp to actually read as the colour it was designed as.
     */
    private const val MAX_SATURATION = 0.92f

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
    //
    // Both bands are measured off the concept: its light ramp runs
    // L 0.90 -> 0.57 and its dark ramp L 0.16 -> 0.27. The dark band is
    // narrower because there is much less room to work with once the ink has
    // to stay legible on top.
    private const val L_LIGHTEST = 0.90f
    private const val L_MIDDLE = 0.78f
    private const val L_DARKEST = 0.66f

    private const val L_DARK_RAMP_DARKEST = 0.20f
    private const val L_DARK_RAMP_MIDDLE = 0.24f
    private const val L_DARK_RAMP_LIGHTEST = 0.29f

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
    fun forBirthDate(
        birthDate: LocalDate,
        darkTheme: Boolean = false,
    ): List<Color> {
        val seed = birthDate.monthValue * 31 + birthDate.dayOfMonth
        // Map the seed range straight onto the arc, with no modulo. Bucketing
        // by `seed % n` collapses birthdays onto the same colour, and with a
        // 31-day month stride `seed % 31` sends every month to the same
        // bucket, which made all twelve months render one colour. A linear
        // map keeps all 372 seeds distinct and spreads the months evenly.
        val spread = (seed - MIN_SEED) / (MAX_SEED - MIN_SEED).toFloat() - 0.5f
        val hue = HUE_CENTER + spread * HUE_ARC
        // Saturation varies a little per seed so two people in the same warm
        // family are not literally the same colour, but it stays in the range
        // the concept uses rather than the desaturated one it used before.
        val saturation =
            if (darkTheme) {
                0.62f + (seed % 5) * 0.055f // 0.62..0.84
            } else {
                0.70f + (seed % 5) * 0.055f // 0.70..0.92
            }
        return forHueAndSaturation(hue, saturation, darkTheme)
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
     * @param darkTheme whether to build the concept's dark ramp (light ink)
     *   rather than its light ramp (dark ink)
     * @return three colours ordered as [forBirthDate] orders them
     */
    fun forHueAndSaturation(
        hue: Float,
        saturation: Float,
        darkTheme: Boolean = false,
    ): List<Color> {
        val safeHue = wrapHue(hue)
        val safeSaturation = saturation.coerceIn(0f, MAX_SATURATION)

        if (darkTheme) {
            return listOf(L_DARK_RAMP_DARKEST, L_DARK_RAMP_MIDDLE, L_DARK_RAMP_LIGHTEST)
                .map { lightness -> clampDarkStop(safeHue, safeSaturation, lightness) }
        }

        return listOf(L_LIGHTEST, L_MIDDLE, L_DARKEST).map { lightness ->
            clampLightStop(safeHue, safeSaturation, lightness)
        }
    }

    /**
     * Raises lightness until the stop clears the dark-ink floor.
     *
     * Saturation is dropped alongside it: some hues stay dark while saturated
     * even at high lightness, and the floor would not otherwise hold.
     */
    private fun clampLightStop(
        hue: Float,
        saturation: Float,
        lightness: Float,
    ): Color {
        var candidateLightness = lightness
        var candidateSaturation = saturation
        var guard = 0
        while (
            relativeLuminance(hsl(hue, candidateSaturation, candidateLightness)) < LUMINANCE_FLOOR &&
            guard < MAX_LIGHTNESS_STEPS
        ) {
            candidateLightness = (candidateLightness + LIGHTNESS_STEP).coerceAtMost(1f)
            candidateSaturation = (candidateSaturation * 0.94f).coerceAtLeast(0f)
            guard++
        }
        return hsl(hue, candidateSaturation, candidateLightness)
    }

    /**
     * Lowers lightness until the stop clears the light-ink ceiling.
     *
     * Same reasoning as [clampLightStop] with the direction reversed: a warm
     * hue can still be too bright for cream ink once saturated, so the ramp
     * walks back down and sheds saturation until the ceiling holds.
     */
    private fun clampDarkStop(
        hue: Float,
        saturation: Float,
        lightness: Float,
    ): Color {
        var candidateLightness = lightness
        var candidateSaturation = saturation
        var guard = 0
        while (
            relativeLuminance(hsl(hue, candidateSaturation, candidateLightness)) > DARK_LUMINANCE_CEILING &&
            guard < MAX_LIGHTNESS_STEPS
        ) {
            candidateLightness = (candidateLightness - LIGHTNESS_STEP).coerceAtLeast(0f)
            candidateSaturation = (candidateSaturation * 0.94f).coerceAtLeast(0f)
            guard++
        }
        return hsl(hue, candidateSaturation, candidateLightness)
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
