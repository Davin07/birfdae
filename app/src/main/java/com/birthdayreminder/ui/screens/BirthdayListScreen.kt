package com.birthdayreminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.components.ErrorDialog
import com.birthdayreminder.ui.components.LuminaAvatar
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaBirthdayCard
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.components.LuminaHeader
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import com.birthdayreminder.ui.viewmodel.BirthdayListViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

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
        BirthdayListContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onEditBirthday = onNavigateToEditBirthday,
            onDeleteBirthday = { birthday ->
                birthdayToDelete = birthday
            },
            onPinBirthday = { birthday ->
                viewModel.togglePin(birthday.id)
            },
            onClearError = viewModel::clearError,
            modifier = Modifier.fillMaxSize(),
            birthdayToDelete = birthdayToDelete,
        )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListContent(
    uiState: BirthdayListUiState,
    onRefresh: () -> Unit,
    onEditBirthday: (Long) -> Unit,
    onDeleteBirthday: (BirthdayWithCountdown) -> Unit,
    onPinBirthday: (BirthdayWithCountdown) -> Unit,
    onClearError: () -> Unit,
    birthdayToDelete: BirthdayWithCountdown? = null,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.endRefresh()
        } else {
            pullRefreshState.startRefresh()
        }
    }

    Box(modifier = modifier.nestedScroll(pullRefreshState.nestedScrollConnection)) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            LuminaHeader(
                title = "Birthdays",
                onBackClick = null,
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                when {
                    uiState.isLoading && uiState.birthdays.isEmpty() -> LoadingState(Modifier.fillMaxSize())
                    uiState.hasError ->
                        ErrorState(uiState.errorMessage ?: "Error", {
                            onClearError()
                            onRefresh()
                        }, Modifier.fillMaxSize())
                    uiState.showEmptyState -> EmptyState(Modifier.fillMaxSize())
                    else -> {
                        val sorted = uiState.birthdays
                        val birthdayToday = sorted.find { it.isToday }
                        val pinnedBirthday = sorted.find { it.birthday.isPinned }
                        val hero = birthdayToday ?: pinnedBirthday ?: sorted.firstOrNull()
                        val others = if (hero != null) sorted.filter { it.id != hero.id } else sorted

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 140.dp),
                        ) {
                            if (hero != null) {
                                item {
                                    HeroBirthdayCard(
                                        birthday = hero,
                                        isPinned = hero.birthday.isPinned && !hero.isToday,
                                        isToday = hero.isToday,
                                        onClick = { onEditBirthday(hero.id) },
                                    )
                                }
                                item {
                                    Text(
                                        "COMING UP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                        letterSpacing = 1.sp,
                                    )
                                }
                            }

                            items(others, key = { it.id }) { birthday ->
                                val dismissState =
                                    rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->
                                            when (value) {
                                                SwipeToDismissBoxValue.EndToStart -> {
                                                    onDeleteBirthday(birthday)
                                                    false
                                                }
                                                SwipeToDismissBoxValue.StartToEnd -> {
                                                    onPinBirthday(birthday)
                                                    false
                                                }
                                                else -> false
                                            }
                                        },
                                    )

                                LaunchedEffect(birthdayToDelete) {
                                    if (
                                        birthdayToDelete == null &&
                                        dismissState.currentValue != SwipeToDismissBoxValue.Settled
                                    ) {
                                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                    }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val offset =
                                            try {
                                                dismissState.requireOffset()
                                            } catch (e: Exception) {
                                                0f
                                            }
                                        val widthDp = with(LocalDensity.current) { abs(offset).toDp() }

                                        Box(Modifier.fillMaxSize()) {
                                            if (offset > 0) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxHeight()
                                                            .width(widthDp)
                                                            .align(Alignment.CenterStart)
                                                            .background(
                                                                MaterialTheme.colorScheme.primary,
                                                                RoundedCornerShape(12.dp),
                                                            )
                                                            .padding(start = 20.dp),
                                                    contentAlignment = Alignment.CenterStart,
                                                ) {
                                                    Icon(
                                                        Icons.Default.PushPin,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                }
                                            } else if (offset < 0) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxHeight()
                                                            .width(widthDp)
                                                            .align(Alignment.CenterEnd)
                                                            .background(Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                                                            .padding(end = 20.dp),
                                                    contentAlignment = Alignment.CenterEnd,
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    content = {
                                        LuminaBirthdayCard(
                                            name = birthday.name,
                                            imageUri = birthday.birthday.imageUri,
                                            dateString =
                                                birthday.birthDate.format(
                                                    DateTimeFormatter.ofPattern("MMM dd"),
                                                ),
                                            age = birthday.age,
                                            daysUntil = birthday.daysUntilNext,
                                            isPinned = birthday.birthday.isPinned,
                                            onClick = { onEditBirthday(birthday.id) },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pullRefreshState.progress > 0f || pullRefreshState.isRefreshing) {
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
fun HeroBirthdayCard(
    birthday: BirthdayWithCountdown,
    isPinned: Boolean,
    isToday: Boolean = false,
    onClick: () -> Unit,
) {
    Column {
        val label =
            when {
                isToday -> "TODAY"
                isPinned -> "PINNED"
                else -> "UPCOMING"
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        LuminaGlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LuminaAvatar(
                            name = birthday.name,
                            imageUri = birthday.birthday.imageUri,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = birthday.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMMM dd")),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val time = birthday.birthday.notificationTime ?: LocalTime.of(9, 0)
                        Text(
                            text = time.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Countdown Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(90.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${birthday.daysUntilNext}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "DAYS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

// ... Loading/Error/Empty States ...
@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Oops!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No Birthdays",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text("Add your first birthday!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
