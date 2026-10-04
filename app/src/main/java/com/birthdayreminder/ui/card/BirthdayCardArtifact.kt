package com.birthdayreminder.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.birthdayreminder.domain.util.ZodiacUtils
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.theme.CardArtifactName
import com.birthdayreminder.ui.theme.CardArtifactQuote
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The shareable birthday card.
 *
 * This is the app's growth surface: it is the one screen a recipient actually
 * sees, and the reason to keep the app installed is that it makes this thing.
 *
 * Design constraints, all of them deliberate:
 *
 * - The gradient is [CardGradient]-derived, so it varies per person but never
 *   drops below the luminance floor. Dark ink on a light band.
 * - Every fact on the card is backed by a column. There is no streak, no
 *   "you have remembered 11 of her last 11" - the schema has `createdAt` and
 *   nothing else, so a streak would be invented. [onYourListSince] is the
 *   truthful alternative.
 * - The recipient is named directly, and the sender is attributed at the
 *   bottom. The recipient should be able to tell who made this.
 */
@Composable
fun BirthdayCardArtifact(
    name: String,
    ageTurning: Int,
    birthDate: LocalDate,
    occasionDate: LocalDate,
    senderName: String,
    message: CardMessage,
    createdAtYear: Int?,
    modifier: Modifier = Modifier,
) {
    // Always the light ramp, regardless of the phone's setting.
    //
    // The app is light-only, so a card that went dark when exported from a
    // dark-mode phone would no longer match the app it came from. One less
    // thing that varies between two people looking at the same birthday.
    //
    // CardGradient still carries the dark ramp and its contrast tests; it is
    // simply not requested here.
    val stops = remember(birthDate) { CardGradient.forBirthDate(birthDate, darkTheme = false) }
    val ink = CardInk
    // Blend toward the card's own lightest stop. Using copy(alpha = 0.72)
    // instead leaves every muted label semi-transparent, so it picks up the
    // hue of whatever it sits on and drifts green over a pink card.
    val mutedInk = lerp(ink, stops.first(), 0.30f)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(0.78f) // Portrait, close to a shareable story/portrait crop
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(stops)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(SaffronTokens.space32),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Occasion eyebrow
            Text(
                text = occasionLabel(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = mutedInk,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing * 3,
            )

            Spacer(Modifier.height(SaffronTokens.space24))

            // Recipient name, the hero of the card
            Text(
                text = name,
                style = CardArtifactName,
                fontWeight = FontWeight.Bold,
                color = ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(SaffronTokens.space8))

            Text(
                text = "turning $ageTurning",
                style = MaterialTheme.typography.titleLarge,
                color = mutedInk,
            )

            Spacer(Modifier.height(SaffronTokens.space4))

            Text(
                text = occasionDate.format(CARD_DATE_FORMAT),
                style = MaterialTheme.typography.bodyMedium,
                color = mutedInk,
            )

            Spacer(Modifier.height(SaffronTokens.space24))

            // Zodiac chip, computed locally from the birth date
            zodiacChip(ZodiacUtils.getZodiacSign(birthDate.month, birthDate.dayOfMonth), ink)

            // One flexible gap, then a fixed gap. The card is a fixed aspect
            // ratio, so the message block has to be given room explicitly --
            // a lone weight(1f) collapses to nothing as the message grows and
            // the text then rides into the attribution.
            Spacer(Modifier.weight(1f))

            // The message. Set in the display face at a reading size, the way
            // the approved prototype sets it, and quoted so it reads as
            // something said *about* the person rather than by the app.
            Text(
                text = "\u201C${message.text}\u201D",
                // bodyLarge in the display face, not titleLarge. The card is a
                // fixed aspect ratio with a header and a footer already
                // committed, and at titleLarge a four-line message runs into
                // the attribution below.
                style = CardArtifactQuote,
                fontStyle = FontStyle.Italic,
                color = ink,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SaffronTokens.space8),
            )

            Spacer(Modifier.height(SaffronTokens.space12))

            onYourListSince(createdAtYear, mutedInk)

            Spacer(Modifier.height(SaffronTokens.space20))

            // Attribution. The recipient must be able to tell who sent this.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(lerp(ink, Color.Transparent, 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = senderName.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ink,
                    )
                }
                Spacer(Modifier.width(SaffronTokens.space8))
                Text(
                    text = attributionLine(senderName),
                    style = MaterialTheme.typography.labelMedium,
                    color = mutedInk,
                )
            }
        }
    }
}

@Composable
private fun zodiacChip(
    sign: String,
    ink: Color,
) {
    Row(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(lerp(ink, Color.Transparent, 0.14f))
                .padding(horizontal = SaffronTokens.space16, vertical = SaffronTokens.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = ink,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(SaffronTokens.space8))
        Text(
            text = sign,
            style = MaterialTheme.typography.labelLarge,
            color = ink,
        )
    }
}

/**
 * Truthful provenance line.
 *
 * There is no reminder history in the database, so this reports when the
 * person was added rather than a count of times they were remembered. The copy
 * is skipped entirely when the year is unknown rather than guessing.
 */
@Composable
private fun onYourListSince(
    year: Int?,
    ink: Color,
) {
    if (year == null) return
    Text(
        text = "on your list since $year",
        style = MaterialTheme.typography.labelMedium,
        color = ink,
    )
}

/**
 * The card's ink on the light ramp.
 *
 * Matches the concept's light card, which pairs the gold gradient with dark
 * ink. [CardGradient] guarantees this clears 4.5:1 on every light-ramp stop.
 */
internal val CardInk: Color = Color(0xFF1A1A17)

/**
 * The card's ink on the dark ramp.
 *
 * The concept's dark card pairs deep amber with a warm cream. Taken from its
 * own light-mode card background so the two ramps stay one design.
 */
internal val CardDarkInk: Color = Color(0xFFFFFEC8)

/**
 * The attribution line.
 *
 * Shared by the on-screen composable and the exported PNG renderer so the
 * two cannot drift. The schema has no user profile, so the default sender is
 * the app itself rather than an invented name.
 *
 * @param senderName the sender, as known to the app
 */
internal fun attributionLine(senderName: String): String =
    if (senderName.equals("Birf Dae", ignoreCase = true)) {
        "Made with Birf Dae"
    } else {
        "from $senderName  ·  Birf Dae"
    }

private fun occasionLabel(): String = "HAPPY BIRTHDAY"

/** e.g. "27 September 2026" — unambiguous for a recipient in any locale. */
internal val CARD_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun birthdayCardArtifactPreview() {
    BirthdayReminderAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BirthdayCardArtifact(
                name = "Amma",
                ageTurning = 59,
                birthDate = LocalDate.of(1967, 9, 27),
                occasionDate = LocalDate.of(2026, 9, 27),
                senderName = "Davin",
                message =
                    CardMessagePicker.initial(
                        name = "Amma",
                        ageTurning = 59,
                        zodiac = "Libra",
                        personalNote = null,
                        tone = CardTone.SINCEREST,
                    ),
                createdAtYear = 2024,
            )
        }
    }
}
