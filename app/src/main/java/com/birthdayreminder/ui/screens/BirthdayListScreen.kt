package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.BirthdayCard
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import com.birthdayreminder.ui.viewmodel.BirthdayListViewModel
import java.time.LocalDate

/**
 * Screen displaying the chronological list of birthdays with countdowns.
 * Satisfies requirements 4.1, 4.2, 4.3, 4.4 for birthday list functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListScreen(
    onNavigateToAddBirthday: () -> Unit,
    onNavigateToEditBirthday: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BirthdayListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var birthdayToDelete by remember { mutableStateOf<BirthdayWithCountdown?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Birthdays",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBirthday,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text("Add Birthday")
                },
                modifier =
                    Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        BirthdayListContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onEditBirthday = onNavigateToEditBirthday,
            onDeleteBirthday = { birthday ->
                birthdayToDelete = birthday
            },
            onClearError = viewModel::clearError,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        )

        // Delete confirmation dialog
        birthdayToDelete?.let { birthday ->
            ConfirmationDialog(
                title = "Delete Birthday",
                message = "Are you sure you want to delete ${birthday.name}'s birthday?",
                onConfirm = {
                    viewModel.deleteBirthday(birthday.id)
                    birthdayToDelete = null
                },
                onDismiss = {
                    birthdayToDelete = null
                },
            )
        }
    }
}

@Composable
private fun BirthdayListContent(
    uiState: BirthdayListUiState,
    onRefresh: () -> Unit,
    onEditBirthday: (Long) -> Unit,
    onDeleteBirthday: (BirthdayWithCountdown) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Refresh button when not loading
        if (!uiState.isLoading && !uiState.isRefreshing) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }
        when {
            uiState.isLoading && uiState.birthdays.isEmpty() -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }

            uiState.hasError -> {
                ErrorState(
                    message = uiState.errorMessage ?: "An error occurred",
                    onRetry = {
                        onClearError()
                        onRefresh()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            uiState.showEmptyState -> {
                EmptyState(modifier = Modifier.fillMaxSize())
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.birthdays,
                        key = { it.id },
                    ) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            onEditClick = { onEditBirthday(birthday.id) },
                            onDeleteClick = { onDeleteBirthday(birthday) },
                        )
                    }

                    // Add some bottom padding for the FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading birthdays...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "😕",
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "🎂",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "No birthdays yet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Add your first birthday to get started!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun birthdayListScreenPreview() {
    BirthdayReminderAppTheme {
        // Preview with sample data
        val sampleBirthdays =
            listOf(
                BirthdayWithCountdown(
                    birthday =
                        Birthday(
                            id = 1,
                            name = "John Doe",
                            birthDate = LocalDate.now().minusYears(30),
                            notes = "Best friend",
                        ),
                    daysUntilNext = 0,
                    isToday = true,
                    nextOccurrence = LocalDate.now(),
                    age = 30,
                ),
                BirthdayWithCountdown(
                    birthday =
                        Birthday(
                            id = 2,
                            name = "Jane Smith",
                            birthDate = LocalDate.now().plusDays(5).minusYears(25),
                            notes = null,
                        ),
                    daysUntilNext = 5,
                    isToday = false,
                    nextOccurrence = LocalDate.now().plusDays(5),
                    age = 25,
                ),
            )

        BirthdayListContent(
            uiState =
                BirthdayListUiState(
                    birthdays = sampleBirthdays,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                ),
            onRefresh = {},
            onEditBirthday = {},
            onDeleteBirthday = {},
            onClearError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun emptyStatePreview() {
    BirthdayReminderAppTheme {
        EmptyState()
    }
}

@Preview(showBackground = true)
@Composable
fun loadingStatePreview() {
    BirthdayReminderAppTheme {
        LoadingState()
    }
}
