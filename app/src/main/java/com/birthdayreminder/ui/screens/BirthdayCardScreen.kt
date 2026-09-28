package com.birthdayreminder.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
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
import com.birthdayreminder.ui.card.CardMessage
import com.birthdayreminder.ui.card.CardMessagePicker
import com.birthdayreminder.ui.card.CardSharer
import com.birthdayreminder.ui.card.CardTone
import com.birthdayreminder.ui.card.zodiacFor
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronSecondaryButton
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionLabel
import com.birthdayreminder.ui.components.birfdae.ToneChipRow
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
    var isSaving by remember { mutableStateOf(false) }
    var rerollSeed by remember { mutableStateOf(0) }

    // Once the sender picks a tone or re-rolls, they have taken control of the
    // wording and the saved note must stop overriding them.
    var hasEditedLine by remember { mutableStateOf(false) }
    var tone by remember { mutableStateOf(CardTone.DEFAULT) }

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

    // The message on the card. Starts from the saved note when there is one,
    // and re-rolls within the current tone. Recolours never: the gradient is
    // part of what makes this card about this person.
    val message: CardMessage =
        remember(birthday.id, tone, rerollSeed, hasEditedLine) {
            val zodiac = zodiacFor(birthday.birthDate)
            if (rerollSeed == 0) {
                // The saved note is the opening line, so the card opens with
                // the sender's own words. It is a starting point, not a lock:
                // choosing a tone or re-rolling is an explicit request for a
                // different line, and must be honoured.
                CardMessagePicker.initial(
                    name = birthday.name,
                    ageTurning = ageTurning,
                    zodiac = zodiac,
                    personalNote = if (hasEditedLine) null else birthday.notes,
                    tone = tone,
                )
            } else {
                val previous =
                    CardMessagePicker.initial(
                        name = birthday.name,
                        ageTurning = ageTurning,
                        zodiac = zodiac,
                        personalNote = null,
                        tone = tone,
                        seed = rerollSeed - 1,
                    )
                CardMessagePicker.reroll(previous, birthday.name, ageTurning, zodiac, rerollSeed)
            }
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
                message = message,
                createdAtYear = birthday.createdAt.year,
            )

            Spacer(Modifier.height(SaffronTokens.space24))

            // Primary: WhatsApp. Falls back to the system sheet when absent.
            SaffronButton(
                onClick = {
                    if (isSharing) return@SaffronButton
                    scope.launch {
                        isSharing = true

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
                                message = message,
                                createdAtYear = birthday.createdAt.year,
                                fileName = "card_${birthday.id}",
                            )

                        val shareText = CardSharer.shareMessage(birthday.name, ageTurning, message.text)
                        val whatsapp = uri?.let { CardSharer.whatsappIntent(it, shareText) }
                        val chosen =
                            if (whatsapp != null && CardSharer.canResolve(context, whatsapp)) {
                                whatsapp
                            } else {
                                uri?.let { CardSharer.genericShareIntent(it, shareText) }
                                    ?: Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                            }

                        val started =
                            runCatching { context.startActivity(Intent.createChooser(chosen, "Share card")) }
                                .isSuccess
                        isSharing = false

                        if (!started) {
                            clipboard.setText(AnnotatedString(shareText))
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

            // The prototype pairs these two side by side: both are
            // secondary, neither is the point of the screen.
            Row(
                horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SaffronSecondaryButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(message.text))
                        scope.launch { snackbarHost.showSnackbar("Message copied.") }
                    },
                    label = "Copy text",
                    icon = Icons.Filled.ContentCopy,
                    modifier = Modifier.weight(1f),
                )

                SaffronSecondaryButton(
                    onClick = {
                        if (isSaving) return@SaffronSecondaryButton
                        scope.launch {
                            isSaving = true
                            val uri =
                                CardImageRenderer.saveToGallery(
                                    context = context,
                                    name = birthday.name,
                                    ageTurning = ageTurning,
                                    birthDate = birthday.birthDate,
                                    occasionDate = nextOccurrence ?: birthday.birthDate,
                                    senderName = senderName,
                                    message = message,
                                    createdAtYear = birthday.createdAt.year,
                                )
                            isSaving = false
                            scope.launch {
                                snackbarHost.showSnackbar(
                                    if (uri != null) {
                                        "Saved to Pictures/Birf Dae."
                                    } else {
                                        "Couldn't save the card."
                                    },
                                )
                            }
                        }
                    },
                    label = if (isSaving) "Saving…" else "Save image",
                    icon = Icons.Filled.Download,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                )
            }

            Spacer(Modifier.height(SaffronTokens.space24))

            SectionLabel(title = "Try another line")
            Spacer(Modifier.height(SaffronTokens.space4))
            Text(
                text = "Re-rolls the words, never the colours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SaffronTokens.space12))

            ToneChipRow(
                selected = tone,
                onSelect = {
                    tone = it
                    hasEditedLine = true
                    // Reset the seed so switching tone restarts that tone's
                    // pool rather than continuing the previous one's count.
                    rerollSeed = 0
                },
                onReroll = {
                    hasEditedLine = true
                    rerollSeed++
                },
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
