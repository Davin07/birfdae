package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.birthdayreminder.domain.model.HomeHero

/**
 * The home screen's opening statement.
 *
 * Text-first, not a card. The approved concept puts an eyebrow, a large
 * headline, one supporting line and a primary action above the list, because
 * the screen opens with a sentence about today rather than with a person-shaped
 * box. A card implies one subject; the prototype's subject is often two people
 * and a date they share.
 *
 * @param hero the words and the person the action is for
 * @param onSendWish invoked by the primary button, which opens that person's
 *   card -- the one action the concept puts front and centre
 */
@Composable
fun HomeHeroSection(
    hero: HomeHero,
    onSendWish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(SaffronTokens.space4))
            Text(
                text = hero.eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(SaffronTokens.space4))

        Text(
            text = hero.headline,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        )

        Spacer(Modifier.height(SaffronTokens.space4))

        Text(
            text = hero.supportingLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(SaffronTokens.space12))

        SaffronButton(
            onClick = onSendWish,
            label = "Send a wish",
            icon = Icons.Filled.Send,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
