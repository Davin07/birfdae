package com.birthdayreminder.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Locks the type scale to the approved prototype.
 *
 * These numbers are not a preference. The prototype is the design contract, and
 * the app drifted from it silently once already: it had been built on Material
 * 3's stock scale, which has no 9.5px or 10.5px step, so every small style was
 * rounded up and several mid styles drifted with them. A test that only fails on
 * a typo would not have caught that, so it asserts against the prototype's
 * actual values.
 *
 * The two lifted steps are the documented exception, not an oversight: see
 * MIN_READABLE in Type.kt.
 */
class SaffronTypographyTest {
    @Test
    fun `the hero headline is the prototype's 22sp, not Material's 30sp`() {
        assertEquals(22f, Typography.displaySmall.fontSize.value, 0.01f)
    }

    @Test
    fun `the countdown is the prototype's 17sp`() {
        assertEquals(17f, Typography.titleLarge.fontSize.value, 0.01f)
    }

    @Test
    fun `running copy is the prototype's 12_5sp`() {
        assertEquals(12.5f, Typography.bodyMedium.fontSize.value, 0.01f)
    }

    @Test
    fun `chips are the prototype's 12_5sp`() {
        assertEquals(12.5f, Typography.labelMedium.fontSize.value, 0.01f)
    }

    @Test
    fun `a text field is 14sp semi-bold, matching the prototype's weight`() {
        assertEquals(14f, Typography.bodyLarge.fontSize.value, 0.01f)
    }

    @Test
    fun `sub-11px prototype steps are lifted to 11sp, never below`() {
        // The prototype asks for 9.5 and 10.5; MIN_READABLE holds both at 11.
        assertEquals(11f, Typography.labelSmall.fontSize.value, 0.01f)
        assertEquals(11f, Typography.bodySmall.fontSize.value, 0.01f)
    }

    @Test
    fun `nothing in the scale falls below the readable floor`() {
        val sizes =
            listOf(
                Typography.displaySmall,
                Typography.headlineLarge,
                Typography.headlineMedium,
                Typography.headlineSmall,
                Typography.titleLarge,
                Typography.titleMedium,
                Typography.titleSmall,
                Typography.bodyLarge,
                Typography.bodyMedium,
                Typography.bodySmall,
                Typography.labelLarge,
                Typography.labelMedium,
                Typography.labelSmall,
            ).map { it.fontSize.value }

        assertEquals(
            "A style slipped under the floor: $sizes",
            sizes.filter { it < 11f },
            emptyList<Float>(),
        )
    }

    @Test
    fun `display styles use Fraunces and the rest use Figtree`() {
        // Compared by identity, not by toString: a resource-backed font family
        // does not print its name, so asserting on the string would pass
        // vacuously against any font at all.
        assertSame(Fraunces, Typography.displaySmall.fontFamily)
        assertSame(Fraunces, Typography.titleLarge.fontFamily)
        assertSame(Figtree, Typography.bodyMedium.fontFamily)
        assertSame(Figtree, Typography.labelMedium.fontFamily)
    }
}
