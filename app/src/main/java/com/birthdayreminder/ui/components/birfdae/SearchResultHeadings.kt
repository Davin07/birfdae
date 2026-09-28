package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The two headings the concept puts above search results: a count, and
 * "Recently added".
 *
 * The count is the only feedback a filter gives. Without it, tapping "Family"
 * and seeing a shorter list is indistinguishable from the list having failed to
 * load, and there is no way to tell "3 of your 12 people are family" from "the
 * screen is broken".
 *
 * @param resultCount how many people matched
 * @param isFiltered whether a chip or a query is narrowing the list, which
 *   decides whether the count reads as a total or as a result
 */
@Composable
fun SearchResultHeadings(
    resultCount: Int,
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = SaffronTokens.space16),
        verticalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = ResultCount.text(resultCount, isFiltered),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "Recently added",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * The wording of the count, kept out of the composable so it can be tested.
 */
object ResultCount {
    /**
     * @param count how many people matched
     * @param isFiltered whether a chip or query is active
     */
    fun text(
        count: Int,
        isFiltered: Boolean,
    ): String {
        val noun = if (count == 1) "match" else "matches"
        return if (isFiltered) "$count $noun" else "$count ${if (count == 1) "person" else "people"}"
    }
}
