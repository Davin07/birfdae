package com.birthdayreminder.ui.card

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Turns a rendered card bitmap into something another app can receive.
 *
 * Every method here is a pure-ish helper over Android's framework types so the
 * interesting logic - intent construction, provider URIs, the missing-app
 * fallback - can be unit tested without a device.
 */
object CardSharer {
    private const val TAG = "CardSharer"

    /** WhatsApp's package name. Absent on plenty of devices, so never assume. */
    const val WHATSAPP_PACKAGE = "com.whatsapp"

    /** Subdirectory of the app cache that FileProvider exposes. */
    private const val SHARE_DIR = "shared_cards"

    /**
     * Builds the WhatsApp deep link for a card.
     *
     * Targets the package explicitly rather than using a bare
     * `whatsapp://send` scheme, because the scheme form is handled
     * inconsistently across OEM builds and can open a web page instead of the
     * app.
     *
     * @param context used only to read the FileProvider authority
     * @param imageUri the card's content URI; must already carry a read grant
     * @param message the accompanying text
     * @return an intent targeting WhatsApp
     */
    fun whatsappIntent(
        imageUri: Uri,
        message: String,
    ): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage(WHATSAPP_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    /**
     * Builds the generic share intent used when WhatsApp is not installed.
     *
     * @param context used only to read the FileProvider authority
     * @param imageUri the card's content URI
     * @param message the accompanying text
     */
    fun genericShareIntent(
        imageUri: Uri,
        message: String,
    ): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    /**
     * Whether any app on the device can handle [intent].
     *
     * `PackageManager.resolveActivity` returns null on some OEM builds rather
     * than throwing, and querying a package that is not installed throws
     * `NameNotFoundException`, so both are handled.
     *
     * @param context any context
     * @param intent the intent to test
     * @return true when at least one activity can receive it
     */
    fun canResolve(
        context: Context,
        intent: Intent,
    ): Boolean =
        try {
            val pm = context.packageManager
            intent.resolveActivity(pm) != null
        } catch (e: Exception) {
            Log.w(TAG, "resolveActivity failed", e)
            false
        }

    /**
     * Writes a card bitmap into the shared cache and returns its content URI.
     *
     * @param context any context
     * @param bitmap the rendered card
     * @param fileName a stable, filesystem-safe name
     * @return a content URI, or null if the write failed
     */
    fun writeCardBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
    ): Uri? =
        try {
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            val file = File(dir, "$fileName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write card bitmap", e)
            null
        }

    /**
     * Composites a card bitmap over the app's own window so the shared PNG has
     * no transparent regions or window background bleeding through.
     *
     * @param bitmap the card
     * @return an opaque bitmap safe to hand to another app
     */
    fun flatten(bitmap: Bitmap): Bitmap {
        val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(opaque).apply {
            drawColor(android.graphics.Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        return opaque
    }

    /**
     * The text that accompanies a shared card.
     *
     * Built from data that actually exists. There is no reminder-history
     * table in the schema, so this never claims a streak.
     *
     * @param name the recipient's name
     * @param age the age they are turning
     */
    fun shareMessage(
        name: String,
        age: Int,
    ): String = "$name is turning $age today. Made with Birf Dae"

    /**
     * Removes previously shared card files.
     *
     * Share targets keep a content URI alive only as long as the grant lasts,
     * so leaving images behind is pure waste. Safe to call when the directory
     * does not exist.
     *
     * @param context any context
     * @return the number of files removed
     */
    fun clearSharedCards(context: Context): Int =
        try {
            val dir = File(context.cacheDir, SHARE_DIR)
            if (!dir.isDirectory) {
                0
            } else {
                val files = dir.listFiles() ?: emptyArray()
                dir.deleteRecursively()
                files.size
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear shared cards", e)
            0
        }
}
