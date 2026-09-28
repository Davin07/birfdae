package com.birthdayreminder.ui.card

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Produces the PNG that actually gets sent to a recipient.
 *
 * This is a hand-drawn [Canvas] renderer rather than a screenshot of the
 * on-screen composable, for three reasons:
 *
 * - the shared image is a fixed 1080x1350 regardless of the sender's device,
 *   so it looks identical everywhere;
 * - it never leaks navigation chrome, the bottom nav, or a snackbar;
 * - the text and colours are the same values the composable uses, via
 *   [CardGradient] and [CardCopy], so preview and share cannot drift apart.
 */
object CardImageRenderer {
    private const val TAG = "CardImageRenderer"

    /** Portrait 4:5, the shape that survives Instagram, WhatsApp and status. */
    const val WIDTH_PX = 1080
    const val HEIGHT_PX = 1350

    private const val CORNER_RADIUS = 72f
    private const val MARGIN = 96f

    private const val SHARE_DIR = "shared_cards"

    private val cardInk = 0xFF1A1A17.toInt()

    /**
     * Renders the card and writes it to the shared cache.
     *
     * @param context any context; used for cacheDir and the FileProvider
     * @param name recipient's name
     * @param ageTurning the age they are turning
     * @param birthDate drives the gradient and the zodiac
     * @param occasionDate the date printed on the card
     * @param senderName attribution line
     * @param message the message shown on the card, with the tone it came from
     * @param createdAtYear the year the person was added, or null to omit
     * @param fileName stable, filesystem-safe base name
     * @return a content URI, or null if rendering or writing failed
     */
    fun renderAndWrite(
        context: Context,
        name: String,
        ageTurning: Int,
        birthDate: LocalDate,
        occasionDate: LocalDate,
        senderName: String,
        message: CardMessage,
        createdAtYear: Int?,
        fileName: String,
    ): Uri? {
        val bitmap =
            runCatching {
                render(name, ageTurning, birthDate, occasionDate, senderName, message, createdAtYear)
            }.getOrNull()
                ?: return null

        return try {
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            val file = File(dir, "$fileName.png")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write card image", e)
            null
        }
    }

    /**
     * Writes the card into the device's shared Pictures collection.
     *
     * Sharing writes to the cache, which is right for a temporary handoff but
     * wrong for "Save image": the system is free to purge the cache, so a card
     * the sender believes they have kept can vanish. MediaStore needs no
     * storage permission from API 29 and produces a file the gallery shows.
     *
     * @param context used to reach MediaStore
     * @param name recipient's name
     * @param ageTurning the age they turn
     * @param birthDate used for the gradient
     * @param occasionDate the date printed on the card
     * @param senderName the sender
     * @param message the message shown on the card
     * @param createdAtYear the year the person was added, or null to omit
     * @return the content URI of the saved image, or null if it could not be written
     */
    fun saveToGallery(
        context: Context,
        name: String,
        ageTurning: Int,
        birthDate: LocalDate,
        occasionDate: LocalDate,
        senderName: String,
        message: CardMessage,
        createdAtYear: Int?,
    ): Uri? =
        runCatching {
            val bitmap =
                render(name, ageTurning, birthDate, occasionDate, senderName, message, createdAtYear)
                    ?: return null

            val values =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "birf-dae-$name.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    // RELATIVE_PATH is what puts it in Pictures/ on API 29+.
                    // IS_PENDING hides it from the gallery until the write lands,
                    // so a half-written file never appears.
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Birf Dae")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) return null

            val written =
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } == true

            if (!written) {
                resolver.delete(uri, null, null)
                return null
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            uri
        }.getOrNull()

    /**
     * Draws the card onto a fresh bitmap.
     *
     * Renders natively rather than screenshotting, so the exported image has
     * no navigation chrome and is deterministic for a given birthday.
     *
     * @return an opaque bitmap, or null if a gradient could not be built
     */
    fun render(
        name: String,
        ageTurning: Int,
        birthDate: LocalDate,
        occasionDate: LocalDate,
        senderName: String,
        message: CardMessage,
        createdAtYear: Int?,
    ): Bitmap? {
        val stops = CardGradient.forBirthDate(birthDate)
        if (stops.isEmpty()) return null

        val bitmap = Bitmap.createBitmap(WIDTH_PX, HEIGHT_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        // Rounded card with the birth-data gradient.
        val path = roundedRectPath(WIDTH_PX, HEIGHT_PX, CORNER_RADIUS)
        val shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                WIDTH_PX.toFloat(),
                HEIGHT_PX.toFloat(),
                stops.map { it.toArgb() }.toIntArray(),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawPath(path, gradientPaint)

        val ink = cardInk
        // Blend toward the card's own lightest stop instead of scaling the
        // packed ARGB int. Multiplying 0xFF1A1A17 by 0.72 also scales the alpha
        // byte, which left the muted text semi-transparent and picking up a
        // green cast from the pink underneath.
        val muted = blend(ink, stops.first().toArgb(), 0.30f)

        // The header sits on an explicit grid rather than by accumulating
        // return values: at 132px a name's ascent is ~100px, so adding the
        // previous line's height is what let it collide with the eyebrow.
        drawCenteredText(
            canvas = canvas,
            text = "H A P P Y   B I R T H D A Y",
            textSize = 34f,
            y = MARGIN + 60f,
            color = muted,
            bold = true,
            letterSpacing = 0.18f,
        )

        var y =
            drawWrappedCenteredText(
                canvas = canvas,
                text = name,
                textSize = 132f,
                maxLines = 2,
                y = MARGIN + 250f,
                color = ink,
                bold = true,
                maxWidth = WIDTH_PX - MARGIN * 2,
            )

        y += 20f
        drawCenteredText(canvas, "turning $ageTurning", 52f, y, ink, bold = true)

        y += 48f

        // The date the card is celebrating, spelled out so a recipient in any
        // locale cannot misread it.
        drawCenteredText(canvas, occasionDate.format(CARD_DATE_FORMAT), 40f, y, muted)

        y += 96f

        // Zodiac, in a pill to match the on-screen card. Without it the export
        // and the preview look like two different artefacts.
        val sign = com.birthdayreminder.domain.util.ZodiacUtils.getZodiacSign(birthDate.month, birthDate.dayOfMonth)
        drawCenteredPill(canvas, sign, 44f, y, ink, stops.first().toArgb())

        // The sender's words are centred in the space between the header block
        // and the footer, so the card never has a hole in the middle or a
        // footer adrift at the bottom.
        val footerTop = HEIGHT_PX - MARGIN - 250f
        // The message is always present: the picker never returns a blank
        // line and an empty personal note falls back to a tone, so this is
        // unconditional rather than a guard.
        val messageLines = 4
        val messageHeight = messageLines * 48f * 1.3f
        val centre = (y + footerTop) / 2f
        drawWrappedCenteredText(
            canvas = canvas,
            text = "\u201C${message.text}\u201D",
            textSize = 48f,
            maxLines = messageLines,
            y = centre - messageHeight / 2f + 48f,
            color = ink,
            bold = false,
            maxWidth = WIDTH_PX - MARGIN * 2 - 80f,
        )

        // Truthful provenance: no streak exists in the schema, so report the
        // year added rather than a number of times remembered.
        if (createdAtYear != null) {
            drawCenteredText(canvas, "on your list since $createdAtYear", 36f, footerTop + 90f, muted)
        }

        drawCenteredText(
            canvas = canvas,
            text = attributionLine(senderName),
            textSize = 34f,
            y = footerTop + 180f,
            color = muted,
        )

        return bitmap
    }

    /** Builds the rounded-rect outline the gradient is clipped to. */
    private fun roundedRectPath(
        width: Int,
        height: Int,
        radius: Float,
    ): android.graphics.Path =
        android.graphics.Path().apply {
            addRoundRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                radius,
                radius,
                android.graphics.Path.Direction.CW,
            )
        }

    /**
     * Mixes two opaque ARGB colours.
     *
     * @param from the starting colour
     * @param to the colour to move toward
     * @param amount 0 returns [from], 1 returns [to]
     * @return an opaque colour
     */
    private fun blend(
        from: Int,
        to: Int,
        amount: Float,
    ): Int {
        val t = amount.coerceIn(0f, 1f)

        fun channel(shift: Int): Int {
            val a = (from shr shift) and 0xFF
            val b = (to shr shift) and 0xFF
            return (a + (b - a) * t).toInt().coerceIn(0, 255)
        }
        return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    private fun textPaint(
        size: Float,
        color: Int,
        bold: Boolean,
    ): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface =
                Typeface.create(
                    Typeface.SERIF,
                    if (bold) Typeface.BOLD else Typeface.NORMAL,
                )
        }

    /**
     * Draws text inside a rounded pill, centred on [y].
     *
     * Mirrors the zodiac chip on the Compose card so the exported PNG and the
     * preview read as the same object.
     */
    private fun drawCenteredPill(
        canvas: Canvas,
        text: String,
        textSize: Float,
        y: Float,
        ink: Int,
        lightTint: Int,
    ) {
        val paint = textPaint(textSize, ink, bold = true)
        val textWidth = paint.measureText(text)
        val padH = textSize * 1.1f
        val padV = textSize * 0.62f
        val left = (WIDTH_PX - (textWidth + padH * 2)) / 2f
        val top = y - textSize - padV
        val rect = RectF(left, top, left + textWidth + padH * 2, y + padV)
        val radius = rect.height() / 2f

        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                // A light wash of the card's own tint, not a dark one. The
                // Compose chip is 14% ink over a light background; blending
                // towards the ink here produced a near-black pill and the sign
                // became unreadable.
                color = blend(lightTint, ink, 0.14f)
            },
        )
        canvas.drawText(text, left + padH, y - padV * 0.35f, paint)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        textSize: Float,
        y: Float,
        color: Int,
        bold: Boolean = false,
        letterSpacing: Float = 0f,
    ): Float {
        val paint = textPaint(textSize, color, bold)
        if (letterSpacing > 0f) paint.letterSpacing = letterSpacing
        val x = (WIDTH_PX - paint.measureText(text)) / 2f
        canvas.drawText(text, x, y, paint)
        return y + textSize * 1.25f
    }

    /**
     * Draws centred text, wrapping and shrinking until it fits.
     *
     * A long name must never overflow the card, so the size steps down rather
     * than clipping.
     *
     * @return the y position below the drawn text
     */
    private fun drawWrappedCenteredText(
        canvas: Canvas,
        text: String,
        textSize: Float,
        maxLines: Int,
        y: Float,
        color: Int,
        bold: Boolean,
        maxWidth: Float,
    ): Float {
        var size = textSize
        var lines = listOf(text)
        var paint = textPaint(size, color, bold)

        // Shrink until it fits in maxLines, or stop at a legible floor.
        while (size > 44f) {
            lines = wrap(text, paint, maxWidth)
            if (lines.size <= maxLines && lines.all { paint.measureText(it) <= maxWidth }) break
            size -= 6f
            paint = textPaint(size, color, bold)
        }

        var cursor = y
        for (line in lines.take(maxLines)) {
            val x = (WIDTH_PX - paint.measureText(line)) / 2f
            canvas.drawText(line, x, cursor, paint)
            cursor += size * 1.16f
        }
        return cursor
    }

    private fun wrap(
        text: String,
        paint: Paint,
        maxWidth: Float,
    ): List<String> {
        val words = text.trim().split(Regex("\\s+"))
        if (words.isEmpty()) return listOf("")

        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        // A single unbreakable word longer than the line: hard-split it.
        return lines.flatMap { line ->
            if (paint.measureText(line) <= maxWidth) {
                listOf(line)
            } else {
                val out = mutableListOf<String>()
                var chunk = StringBuilder()
                for (ch in line) {
                    if (paint.measureText(chunk.toString() + ch) > maxWidth) {
                        out.add(chunk.toString())
                        chunk = StringBuilder()
                    }
                    chunk.append(ch)
                }
                if (chunk.isNotEmpty()) out.add(chunk.toString())
                out
            }
        }
    }

    /** Convenience: the card's ink as a Compose colour, for the composable. */
    fun cardInkColor(): Color = Color(cardInk)

    /** Rounds a float to an int, for callers laying out the exported size. */
    fun px(value: Float): Int = value.roundToInt()
}
