package com.birthdayreminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.components.birfdae.PersonRow
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronChip
import com.birthdayreminder.ui.components.birfdae.SaffronSearchField
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.viewmodel.SearchType
import com.birthdayreminder.ui.viewmodel.SearchViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * People search with name/month filtering and swipe-to-delete.
 *
 * Explore surface: the query field and filter chips are the controls, and
 * results are scanned rather than read.
 *
 * @param onNavigateToEditBirthday opens the edit wizard for a person id
 * @param viewModel screen ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToEditBirthday: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var birthdayToDelete by remember { mutableStateOf<Long?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(title = "People")

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = SaffronTokens.gutter),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(SaffronTokens.space12)) {
                    SaffronSearchField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder =
                            if (uiState.searchType == SearchType.NAME) {
                                "Search by name"
                            } else {
                                "Search by month"
                            },
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8)) {
                        SaffronChip(
                            selected = uiState.searchType == SearchType.NAME,
                            onClick = { viewModel.onSearchTypeChanged(SearchType.NAME) },
                            label = "Name",
                        )
                        SaffronChip(
                            selected = uiState.searchType == SearchType.MONTH,
                            onClick = { viewModel.onSearchTypeChanged(SearchType.MONTH) },
                            label = "Month",
                        )
                    }
                }

                if (uiState.results.isEmpty()) {
                    EmptySearchResult(isFiltered = uiState.query.isNotBlank())
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
                        contentPadding =
                            PaddingValues(
                                top = SaffronTokens.space16,
                                bottom = SaffronTokens.navBarHeight + SaffronTokens.space24,
                            ),
                    ) {
                        items(uiState.results, key = { it.birthday.id }) { birthday ->
                            val dismissState =
                                rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                birthdayToDelete = birthday.birthday.id
                                                // Snap back; the dialog handles the delete.
                                                false
                                            }
                                            else -> false
                                        }
                                    },
                                )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.Settled) {
                                        return@SwipeToDismissBox
                                    }

                                    val offset =
                                        try {
                                            dismissState.requireOffset()
                                        } catch (e: IllegalStateException) {
                                            0f
                                        }
                                    val widthDp = with(LocalDensity.current) { abs(offset).toDp() }

                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxHeight()
                                                    .width(widthDp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.error,
                                                        shape = SaffronTokens.radiusLarge,
                                                    ).padding(horizontal = SaffronTokens.space20),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onError,
                                            )
                                        }
                                    }
                                },
                                content = {
                                    PersonRow(
                                        name = birthday.name,
                                        imageUri = birthday.birthday.imageUri,
                                        dateString = birthday.birthDate.format(dateFormatter),
                                        ageTurning = birthday.age,
                                        daysUntil = birthday.daysUntilNext,
                                        isPinned = birthday.birthday.isPinned,
                                        onClick = { onNavigateToEditBirthday(birthday.birthday.id) },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        birthdayToDelete?.let { id ->
            ConfirmationDialog(
                title = "Delete Birthday",
                message = "Are you sure you want to delete this birthday?",
                onConfirm = {
                    viewModel.deleteBirthday(id)
                    birthdayToDelete = null
                },
                onDismiss = { birthdayToDelete = null },
            )
        }
    }
}

/**
 * Shown when a search returns nothing. Distinguishes "no people at all" from
 * "your filter matched nothing", because the two need different actions.
 *
 * @param isFiltered whether a query is currently applied
 */
@Composable
private fun EmptySearchResult(isFiltered: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = SaffronTokens.space24),
        ) {
            Text(
                text = if (isFiltered) "No matches" else "No birthdays yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(Modifier.padding(SaffronTokens.space8))
            Text(
                text =
                    if (isFiltered) {
                        "Try a different name or switch to month."
                    } else {
                        "People you add will show up here."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
