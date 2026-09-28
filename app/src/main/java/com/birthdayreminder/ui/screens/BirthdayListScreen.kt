package com.birthdayreminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.components.ErrorDialog
import com.birthdayreminder.ui.components.birfdae.HomeHeroSection
import com.birthdayreminder.ui.components.birfdae.OverdueBirthdayCard
import com.birthdayreminder.ui.components.birfdae.OverdueRemainderRow
import com.birthdayreminder.ui.components.birfdae.PersonRow
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronButton
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionLabel
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import com.birthdayreminder.ui.viewmodel.BirthdayListViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Home screen: the next birthday that matters, then everyone else.
 *
 * Explore surface. The hero states only what the data supports - the name, the
 * date, the age they turn and the reminder time - because this app has no
 * reminder-history table, so any "you have never missed one" claim would be
 * invented.
 *
 * @param onNavigateToAddBirthday opens the add wizard
 * @param onNavigateToEditBirthday opens the edit wizard for a person id
 * @param modifier applied to the screen
 * @param viewModel screen ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListScreen(
    onNavigateToAddBirthday: () -> Unit,
    onNavigateToEditBirthday: (Long) -> Unit,
    onShareCard: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BirthdayListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var birthdayToDelete by remember { mutableStateOf<BirthdayWithCountdown?>(null) }

    SaffronBackground {
        BirthdayListContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onEditBirthday = onNavigateToEditBirthday,
            onAddBirthday = onNavigateToAddBirthday,
            onDeleteBirthday = { birthday -> birthdayToDelete = birthday },
            onPinBirthday = { birthday -> viewModel.togglePin(birthday.id) },
            onShareCard = onShareCard,
            onSkipBirthday = viewModel::skipForThisYear,
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

/**
 * Stateless body of the home screen, split out so it can be previewed.
 *
 * @param uiState current screen state
 * @param onRefresh invoked when pull-to-refresh settles
 * @param onEditBirthday opens the edit wizard
 * @param onAddBirthday opens the add wizard
 * @param onDeleteBirthday requests deletion; the caller confirms it
 * @param onPinBirthday toggles the pin flag
 * @param onShareCard opens the shareable card for a person id
 * @param onSkipBirthday records that a missed birthday is not being celebrated
 * @param onClearError dismisses an error
 * @param birthdayToDelete pending deletion, used to snap swipe rows back
 * @param modifier applied to the body
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListContent(
    uiState: BirthdayListUiState,
    onRefresh: () -> Unit = {},
    onEditBirthday: (Long) -> Unit = {},
    onAddBirthday: () -> Unit = {},
    onDeleteBirthday: (BirthdayWithCountdown) -> Unit = {},
    onPinBirthday: (BirthdayWithCountdown) -> Unit = {},
    onShareCard: (Long) -> Unit = {},
    onSkipBirthday: (Long) -> Unit = {},
    onClearError: () -> Unit = {},
    birthdayToDelete: BirthdayWithCountdown? = null,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) { onRefresh() }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (uiState.isRefreshing) {
            pullRefreshState.startRefresh()
        } else {
            pullRefreshState.endRefresh()
        }
    }

    Box(modifier = modifier.nestedScroll(pullRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(title = "Upcoming")

            when {
                uiState.isLoading && uiState.birthdays.isEmpty() -> LoadingState(Modifier.fillMaxSize())
                uiState.hasError ->
                    ErrorState(
                        message = uiState.errorMessage ?: "Something went wrong.",
                        onRetry = {
                            onClearError()
                            onRefresh()
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                uiState.showEmptyState -> EmptyState(onAddBirthday = onAddBirthday, modifier = Modifier.fillMaxSize())
                else -> {
                    val sorted = uiState.birthdays
                    // The hero is text, not a card, so it no longer swallows a
                    // row. It did once hold one person back to fill a hero card,
                    // which meant the person the screen was about could be the
                    // one missing from the list.
                    //
                    // The hero's own people are still excluded here, but only
                    // because the section headings are the problem: a "Next up"
                    // list whose first row repeats the name the hero just
                    // announced reads as a bug. Everything else stays in.
                    val heroIds = uiState.hero?.people?.map { it.birthdayId }?.toSet().orEmpty()
                    val listed = sorted.filterNot { it.birthday.id in heroIds }
                    val soon = listed.filter { it.daysUntilNext <= 30 }
                    val later = listed.filter { it.daysUntilNext > 30 }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
                        contentPadding =
                            PaddingValues(
                                start = SaffronTokens.gutter,
                                end = SaffronTokens.gutter,
                                bottom = SaffronTokens.navBarHeight + SaffronTokens.space24,
                            ),
                    ) {
                        // Overdue comes before the hero: a birthday already
                        // missed is more pressing than one still upcoming, and
                        // the countdown flow cannot surface it.
                        //
                        // Only the first is expanded. Three or four full cards
                        // pushed the hero and every upcoming row off the
                        // screen, which is the same as hiding them; the rest
                        // stay reachable through the collapsed count.
                        if (uiState.overdue.isNotEmpty()) {
                            item(key = "overdue-header") {
                                SectionLabel(
                                    title =
                                        if (uiState.overdue.size == 1) {
                                            "Needs a moment"
                                        } else {
                                            "Needs a moment \u00b7 ${uiState.overdue.size} birthdays"
                                        },
                                )
                            }
                            item(key = "overdue-${uiState.overdue.first().id}") {
                                val overdue = uiState.overdue.first()
                                OverdueBirthdayCard(
                                    overdue = overdue,
                                    onSendBelatedWish = { onShareCard(overdue.id) },
                                    onNotThisYear = { onSkipBirthday(overdue.id) },
                                )
                            }
                            if (uiState.overdue.size > 1) {
                                item(key = "overdue-more") {
                                    OverdueRemainderRow(
                                        remaining = uiState.overdue.drop(1),
                                        onSelect = onEditBirthday,
                                    )
                                }
                            }
                        }

                        uiState.hero?.let { hero ->
                            item(key = "hero") {
                                HomeHeroSection(
                                    hero = hero,
                                    onSendWish = { onShareCard(hero.primaryBirthdayId) },
                                )
                            }
                        }

                        if (soon.isNotEmpty()) {
                            item(key = "soon-header") { SectionLabel(title = "Next up") }
                            items(soon, key = { it.id }) { birthday ->
                                SwipeablePersonRow(
                                    birthday = birthday,
                                    snapBack = birthdayToDelete == null,
                                    onEdit = { onEditBirthday(birthday.id) },
                                    onShare = { onShareCard(birthday.id) },
                                    onDelete = { onDeleteBirthday(birthday) },
                                    onPin = { onPinBirthday(birthday) },
                                )
                            }
                        }

                        if (later.isNotEmpty()) {
                            item(key = "later-header") { SectionLabel(title = "Later this year") }
                            items(later, key = { it.id }) { birthday ->
                                SwipeablePersonRow(
                                    birthday = birthday,
                                    snapBack = birthdayToDelete == null,
                                    onEdit = { onEditBirthday(birthday.id) },
                                    onShare = { onShareCard(birthday.id) },
                                    onDelete = { onDeleteBirthday(birthday) },
                                    onPin = { onPinBirthday(birthday) },
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

/**
 * A person row with swipe-to-pin (from the start) and swipe-to-delete (from
 * the end). Both gestures snap back, because both need confirmation.
 *
 * @param birthday person and countdown
 * @param snapBack when true, settles the row; used after a dialog closes
 * @param onEdit opens the edit wizard
 * @param onDelete requests deletion
 * @param onPin toggles the pin flag
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePersonRow(
    birthday: BirthdayWithCountdown,
    snapBack: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onShare: () -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete()
                        false
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onPin()
                        false
                    }
                    else -> false
                }
            },
        )

    LaunchedEffect(snapBack) {
        if (snapBack && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
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
                    SwipeAction(
                        width = widthDp,
                        color = MaterialTheme.colorScheme.primary,
                        alignStart = true,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                } else if (offset < 0) {
                    SwipeAction(
                        width = widthDp,
                        color = MaterialTheme.colorScheme.error,
                        alignStart = false,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onError,
                        )
                    }
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
                onClick = onEdit,
                onShareClick = onShare,
            )
        },
    )
}

/** Coloured reveal that sits behind a swiped row. */
@Composable
private fun SwipeAction(
    width: Dp,
    color: Color,
    alignStart: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(width)
                .background(color, SaffronTokens.radiusLarge)
                .padding(horizontal = SaffronTokens.space20),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        content()
    }
}

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = SaffronTokens.space24),
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(SaffronTokens.space8))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SaffronTokens.space16))
            SaffronButton(onClick = onRetry, label = "Try again")
        }
    }
}

/**
 * First-run state: one sentence, one action, no sample data.
 *
 * @param onAddBirthday opens the add wizard
 * @param modifier applied to the block
 */
@Composable
private fun EmptyState(
    onAddBirthday: () -> Unit = {},
    modifier: Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = SaffronTokens.space24),
        ) {
            Text(
                text = "No birthdays yet",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(SaffronTokens.space8))
            Text(
                text = "Add the people you'd be sad to forget. About fifteen seconds each.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SaffronTokens.space24))
            SaffronButton(onClick = onAddBirthday, label = "Add your first birthday")
        }
    }
}
