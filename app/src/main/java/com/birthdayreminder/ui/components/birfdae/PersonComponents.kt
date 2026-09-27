package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Circular person avatar with deterministic accent tinting.
 *
 * Replaces [LuminaAvatar], which always used a primary-to-tertiary gradient,
 * so a list of people rendered as a column of identical circles. Tinting now
 * derives from the name via [accentFor], which keeps a person visually stable
 * across screens while making neighbours distinguishable.
 *
 * @param name person's name, used for initials and accent selection
 * @param imageUri optional local image; falls back to initials when null
 * @param size avatar diameter
 * @param modifier applied to the avatar
 */
@Composable
fun PersonAvatar(
    name: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = SaffronTokens.avatarMedium,
) {
    val (container, content) = accentFor(name)

    Box(
        modifier =
            modifier
                .size(size)
                .semantics { contentDescription = "$name avatar" },
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().size(size),
            )
        } else {
            Surface(
                modifier = Modifier.size(size),
                shape = CircleShape,
                color = container,
                contentColor = content,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initialsOf(name),
                        style = MaterialTheme.typography.titleMedium,
                        color = content,
                    )
                }
            }
        }
    }
}

/**
 * A person as a list row: avatar, name, date line, and a days-until counter.
 *
 * Replaces [LuminaBirthdayCard]. The counter numeral is set in the display
 * serif because it is the first thing the eye lands on when scanning a list.
 *
 * @param name person's name
 * @param imageUri optional local image
 * @param dateString formatted birth date, e.g. "2 March"
 * @param ageTurning age they turn this year
 * @param daysUntil days until the next birthday
 * @param onClick invoked on tap
 * @param modifier applied to the row
 * @param isPinned shows a pin icon
 */
@Composable
fun PersonRow(
    name: String,
    imageUri: String?,
    dateString: String,
    ageTurning: Int,
    daysUntil: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        "$name, $dateString, turning $ageTurning, " +
                        if (daysUntil == 0) {
                            "today"
                        } else {
                            "$daysUntil days away"
                        }
                },
        shape = SaffronTokens.radiusLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(SaffronTokens.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
        ) {
            PersonAvatar(name = name, imageUri = imageUri)

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isPinned) {
                        Box(Modifier.size(SaffronTokens.space6))
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Box(Modifier.size(SaffronTokens.space4))
                Text(
                    text = "$dateString · Turning $ageTurning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            CountdownPill(days = daysUntil)
        }
    }
}

/**
 * Derives up to two uppercase initials from a name.
 *
 * Only letters are accepted, so a name like "123" falls back to "?" rather
 * than rendering the digits as an avatar.
 *
 * @param name the person's name
 * @return initials, or "?" when the name has no letters
 */
fun initialsOf(name: String): String =
    name
        .split(Regex("\\s+"))
        .mapNotNull { word -> word.firstOrNull { it.isLetter() }?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }
