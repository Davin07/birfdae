package com.birthdayreminder.ui.theme

import com.birthdayreminder.ui.card.CardGradient
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The overdue card is the one place the app asks for something emotionally
 * loaded, so "warm-urgent" has to stay readable rather than merely warm.
 *
 * Both pairs are checked against the 4.5:1 body-text floor. A missed birthday
 * rendered in a colour that needs 3:1 is a card nobody can read.
 */
class OverdueColorsTest {
    @Test
    fun `light overdue text clears the body contrast floor on its container`() {
        val ratio = CardGradient.contrastRatio(SaffronLightOverdue.onContainer, SaffronLightOverdue.container)

        assertTrue(
            "Light overdue text is $ratio:1, below the 4.5:1 body floor",
            ratio >= MIN_BODY_CONTRAST,
        )
    }

    @Test
    fun `dark overdue text clears the body contrast floor on its container`() {
        val ratio = CardGradient.contrastRatio(SaffronDarkOverdue.onContainer, SaffronDarkOverdue.container)

        assertTrue(
            "Dark overdue text is $ratio:1, below the 4.5:1 body floor",
            ratio >= MIN_BODY_CONTRAST,
        )
    }

    @Test
    fun `the light pair stays in the warm saffron family, not plum`() {
        val hue = SaffronLightOverdue.container.hueDegrees()

        assertTrue(
            "Light overdue container is at $hue degrees, outside the saffron/rosewood band",
            hue in 15.0..70.0,
        )
    }

    @Test
    fun `the dark pair stays in the warm saffron family, not plum`() {
        val hue = SaffronDarkOverdue.container.hueDegrees()

        assertTrue(
            "Dark overdue container is at $hue degrees, outside the saffron/rosewood band",
            hue in 15.0..70.0,
        )
    }

    private companion object {
        const val MIN_BODY_CONTRAST = 4.5
    }
}

/**
 * The hue of an opaque colour in degrees, 0 to 360.
 *
 * Computed here rather than pulled from Compose so the assertion is about the
 * literal token in Color.kt and does not depend on a colour-space conversion
 * that could change the number for an unrelated reason.
 */
private fun androidx.compose.ui.graphics.Color.hueDegrees(): Double {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    if (delta == 0f) return 0.0
    val hue =
        when (max) {
            r -> 60.0 * (((g - b) / delta) % 6.0)
            g -> 60.0 * (((b - r) / delta) + 2.0)
            else -> 60.0 * (((r - g) / delta) + 4.0)
        }
    return ((hue + 360.0) % 360.0)
}
