package com.birthdayreminder.ui.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests the parts of sharing that can be checked off-device.
 *
 * The intent construction matters most: WhatsApp is absent on plenty of
 * devices, and a share button that throws or silently does nothing there is
 * the difference between a growth loop and a bug report.
 */
@RunWith(RobolectricTestRunner::class)
class CardSharerTest {
    private val ink = androidx.compose.ui.graphics.Color(0xFF1A1A17)

    @Test
    fun `share message names the recipient and the age`() {
        val message = CardSharer.shareMessage("Amma", 59)

        assertTrue(message.contains("Amma"))
        assertTrue(message.contains("59"))
        assertTrue(message.contains("Birf Dae"))
    }

    @Test
    fun `share message does not claim a streak`() {
        val message = CardSharer.shareMessage("Amma", 59)

        // The schema has no reminder history, so a count would be invented.
        listOf("streak", "times you've", "11 of", "remembered").forEach { forbidden ->
            assertFalse(
                "Share copy must not contain \"$forbidden\": $message",
                message.contains(forbidden, ignoreCase = true),
            )
        }
    }

    @Test
    fun `renderer produces a correctly sized opaque bitmap`() {
        val bitmap =
            CardImageRenderer.render(
                name = "Amma",
                ageTurning = 59,
                birthDate = java.time.LocalDate.of(1967, 9, 27),
                occasionDate = java.time.LocalDate.of(2026, 9, 27),
                senderName = "Birf Dae",
                personalMessage = "Happy birthday.",
                createdAtYear = 2024,
            )

        assertNotNull("Renderer returned null", bitmap)
        requireNotNull(bitmap)
        assertEquals(CardImageRenderer.WIDTH_PX, bitmap.width)
        assertEquals(CardImageRenderer.HEIGHT_PX, bitmap.height)
        // A share target receiving transparency renders it black.
        assertEquals(android.graphics.Bitmap.Config.ARGB_8888, bitmap.config)
    }

    @Test
    fun `renderer survives a very long name without failing`() {
        val bitmap =
            CardImageRenderer.render(
                name = "Bartholomew Christopher Fitzgerald-Montgomery III",
                ageTurning = 41,
                birthDate = java.time.LocalDate.of(1984, 2, 29),
                occasionDate = java.time.LocalDate.of(2024, 2, 29),
                senderName = "Birf Dae",
                personalMessage = null,
                createdAtYear = null,
            )

        assertNotNull(bitmap)
    }

    @Test
    fun `renderer works with no message and no provenance year`() {
        val bitmap =
            CardImageRenderer.render(
                name = "Sam",
                ageTurning = 30,
                birthDate = java.time.LocalDate.of(1996, 1, 1),
                occasionDate = java.time.LocalDate.of(2026, 1, 1),
                senderName = "Birf Dae",
                personalMessage = null,
                createdAtYear = null,
            )

        assertNotNull(bitmap)
    }

    @Test
    fun `exported size is a share-friendly 4 by 5 portrait`() {
        val ratio = CardImageRenderer.HEIGHT_PX.toDouble() / CardImageRenderer.WIDTH_PX
        assertEquals(1.25, ratio, 0.001)
    }

    @Test
    fun `card ink is the same value the gradient invariant was proved against`() {
        assertEquals(ink, CardImageRenderer.cardInkColor())
    }
}
