package com.birthdayreminder.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.ui.card.BirthdayCardArtifact
import com.birthdayreminder.ui.card.CardImageRenderer
import com.birthdayreminder.ui.card.CardSharer
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronSecondaryButton
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import kotlinx.coroutines.launch

/**
 * Hosts the shareable birthday card and the actions that distribute it.
 *
 * Sharing is the whole point of this screen, so the WhatsApp path is the
 * primary button and everything else is a fallback. WhatsApp is not assumed
 * to be installed: if it is missing, the generic system share sheet is used
 * instead rather than throwing.
 *
 * The card is handed to the share target as a real PNG produced by
 * [CardSharer], not as a screenshot, so the recipient gets a clean image at
 * full resolution.
 */
@Composable
fun BirthdayCardScreen(
    birthday: Birthday?,
    nextOccurrence: java.time.LocalDate?,
    ageTurning: Int,
    senderName: String,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    var isSharing by remember { mutableStateOf(false) }

    if (birthday == null) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(SaffronTokens.space24),
            ) {
                SectionHeader(title = "Card", onBackClick = onNavigateBack)
                Spacer(Modifier.height(SaffronTokens.space24))
                Text(
                    text = "That birthday is no longer on your list.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(SaffronTokens.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionHeader(title = "Share the card", onBackClick = onNavigateBack)

            Spacer(Modifier.height(SaffronTokens.space8))

            Text(
                text = "Personalised from the birthday you saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SaffronTokens.space24))

            BirthdayCardArtifact(
                name = birthday.name,
                ageTurning = ageTurning,
                birthDate = birthday.birthDate,
                occasionDate = nextOccurrence ?: birthday.birthDate,
                senderName = senderName,
                personalMessage = birthday.notes,
                createdAtYear = birthday.createdAt.year,
            )

            Spacer(Modifier.height(SaffronTokens.space24))

            // Primary: WhatsApp. Falls back to the system sheet when absent.
            SaffronButton(
                onClick = {
                    if (isSharing) return@SaffronButton
                    scope.launch {
                        isSharing = true
                        val message = CardSharer.shareMessage(birthday.name, ageTurning)

                        // Rendered natively rather than screenshotted, so the
                        // recipient gets a clean 1080x1350 image with no
                        // navigation chrome baked into it.
                        val uri =
                            CardImageRenderer.renderAndWrite(
                                context = context,
                                name = birthday.name,
                                ageTurning = ageTurning,
                                birthDate = birthday.birthDate,
                                occasionDate = nextOccurrence ?: birthday.birthDate,
                                senderName = senderName,
                                personalMessage = birthday.notes,
                                createdAtYear = birthday.createdAt.year,
                                fileName = "card_${birthday.id}",
                            )

                        val whatsapp = uri?.let { CardSharer.whatsappIntent(it, message) }
                        val chosen =
                            if (whatsapp != null && CardSharer.canResolve(context, whatsapp)) {
                                whatsapp
                            } else {
                                uri?.let { CardSharer.genericShareIntent(it, message) }
                                    ?: Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                            }

                        val started =
                            runCatching { context.startActivity(Intent.createChooser(chosen, "Share card")) }
                                .isSuccess
                        isSharing = false

                        if (!started) {
                            clipboard.setText(AnnotatedString(message))
                            snackbarHost.showSnackbar("WhatsApp is not available, so the message was copied instead.")
                        }
                    }
                },
                label = if (isSharing) "Preparing…" else "Share on WhatsApp",
                icon = Icons.Filled.Share,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSharing,
            )

            Spacer(Modifier.height(SaffronTokens.space12))

            // Fallback that always works, even with no shareable apps at all.
            SaffronSecondaryButton(
                onClick = {
                    val message = CardSharer.shareMessage(birthday.name, ageTurning)
                    clipboard.setText(AnnotatedString(message))
                    scope.launch { snackbarHost.showSnackbar("Message copied.") }
                },
                label = "Copy message",
                icon = Icons.Filled.ContentCopy,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SaffronTokens.space24))
        }
    }
}

/**
 * Clears out share images that are no longer needed.
 *
 * Called when the card screen leaves, so the cache does not grow one PNG per
 * share.
 */
internal fun cleanupSharedCards(context: Context) {
    CardSharer.clearSharedCards(context)
}
