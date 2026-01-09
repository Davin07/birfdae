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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.components.ErrorDialog
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaBirthdayCard
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.components.LuminaTitle
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import com.birthdayreminder.ui.viewmodel.BirthdayListViewModel
import java.time.format.DateTimeFormatter

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

    LuminaBackground {
        Scaffold(
            containerColor = Color.Transparent,
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            // Dialogs
            if (uiState.hasError && !uiState.isLoading && uiState.birthdays.isNotEmpty()) {
                uiState.errorResult?.let { error ->
                    ErrorDialog(
                        error = error,
                        onRetry = { viewModel.refresh() },
                        onDismiss = { viewModel.clearError() },
                    )
                }
            }

            birthdayToDelete?.let { birthday ->
                ConfirmationDialog(
                    title = "Delete Birthday",
                    message = "Are you sure you want to delete ${birthday.name}'s birthday?",
                    onConfirm = {
                        viewModel.deleteBirthday(birthday.id)
                        birthdayToDelete = null
                    },
                    onDismiss = { birthdayToDelete = null },
                )
            }
        }
    }
}

@Composable
fun BirthdayListContent(
    uiState: BirthdayListUiState,
    onRefresh: () -> Unit,
    onEditBirthday: (Long) -> Unit,
    onDeleteBirthday: (BirthdayWithCountdown) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LuminaTitle(text = "Birthdays")
            if (!uiState.isLoading) {
                TextButton(onClick = onRefresh) {
                    Text("Refresh", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        when {
            uiState.isLoading && uiState.birthdays.isEmpty() -> LoadingState(Modifier.fillMaxSize())
            uiState.hasError -> ErrorState(uiState.errorMessage ?: "Error", { onClearError(); onRefresh() }, Modifier.fillMaxSize())
            uiState.showEmptyState -> EmptyState(Modifier.fillMaxSize())
            else -> {
                // Determine Pinned / Hero
                val sorted = uiState.birthdays
                val pinned = sorted.find { it.birthday.isPinned } ?: sorted.firstOrNull()
                val others = if (pinned != null) sorted.filter { it.id != pinned.id } else sorted

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (pinned != null) {
                        item {
                            HeroBirthdayCard(
                                birthday = pinned,
                                isPinned = pinned.birthday.isPinned,
                                onEditClick = { onEditBirthday(pinned.id) }
                            )
                        }
                        item {
                            Text(
                                "Upcoming",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }

                    items(others, key = { it.id }) { birthday ->
                        LuminaBirthdayCard(
                            name = birthday.name,
                            dateString = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                            age = birthday.age,
                            daysUntil = birthday.daysUntilNext,
                            onClick = { onEditBirthday(birthday.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBirthdayCard(
    birthday: BirthdayWithCountdown,
    isPinned: Boolean,
    onEditClick: () -> Unit
) {
    Column {
        if (isPinned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PINNED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = birthday.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• Turning ${birthday.age}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Countdown Badge
                    LuminaGlassCard {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COUNT DOWN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text("${birthday.daysUntilNext} Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Oops!", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No Birthdays", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Add your first birthday!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}