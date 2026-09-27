package com.birthdayreminder.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.birfdae.PersonRow
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronIconButton
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val PAGE_OFFSET = 1000
private const val PAGE_COUNT = 2001

/**
 * Month calendar with per-day birthday markers.
 *
 * Explore surface. The grid leads with the real first-of-month weekday, so
 * leading blanks are computed from the date rather than hard-coded.
 *
 * @param viewModel screen ViewModel
 * @param onBirthdayClick opens a person from the list beneath the grid
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onBirthdayClick: (BirthdayWithCountdown) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val rowDateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }

    val pagerState =
        rememberPagerState(
            initialPage = PAGE_OFFSET,
            pageCount = { PAGE_COUNT },
        )

    LaunchedEffect(pagerState.currentPage) {
        val targetMonth = YearMonth.now().plusMonths((pagerState.currentPage - PAGE_OFFSET).toLong())
        viewModel.navigateToMonth(targetMonth)
    }

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(
                title = "Calendar",
                actions = {
                    TextButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch { pagerState.animateScrollToPage(PAGE_OFFSET) }
                        },
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier.padding(horizontal = SaffronTokens.gutter),
                verticalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SaffronIconButton(
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                    )

                    Text(
                        text = uiState.currentMonth.format(monthFormatter),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    SaffronIconButton(
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                    )
                }

                DayOfWeekHeaders()

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(310.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        val month = YearMonth.now().plusMonths((page - PAGE_OFFSET).toLong())

                        val birthdaysInMonth =
                            remember(month, uiState.allBirthdays) {
                                uiState.allBirthdays
                                    .filter { it.birthDate.month == month.month }
                                    .groupBy { candidate ->
                                        // A 29 Feb birthday lands on 28 Feb in a
                                        // non-leap year, so clamp rather than crash.
                                        runCatching {
                                            LocalDate.of(month.year, month.month, candidate.birthDate.dayOfMonth)
                                        }.getOrElse { month.atDay(month.lengthOfMonth()) }
                                    }
                            }

                        CalendarDaysGrid(
                            currentMonth = month,
                            birthdaysInMonth = birthdaysInMonth,
                            selectedDate = uiState.selectedDate,
                            onDateClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectDate(it)
                            },
                        )
                    }
                }

                val birthdaysToShow =
                    remember(uiState.selectedDate, uiState.currentMonth, uiState.allBirthdays) {
                        val selected = uiState.selectedDate
                        if (selected != null) {
                            uiState.allBirthdays.filter {
                                it.birthDate.month == selected.month &&
                                    it.birthDate.dayOfMonth == selected.dayOfMonth
                            }
                        } else {
                            uiState.allBirthdays
                                .filter { it.birthDate.month == uiState.currentMonth.month }
                                .sortedBy { it.birthDate.dayOfMonth }
                        }
                    }

                Text(
                    text = if (uiState.selectedDate != null) "On this day" else "This month",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = SaffronTokens.space4),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
                    contentPadding =
                        PaddingValues(bottom = SaffronTokens.navBarHeight + SaffronTokens.space24),
                ) {
                    items(birthdaysToShow, key = { it.id }) { birthday ->
                        PersonRow(
                            name = birthday.name,
                            imageUri = birthday.birthday.imageUri,
                            dateString = birthday.birthDate.format(rowDateFormatter),
                            ageTurning = birthday.age,
                            daysUntil = birthday.daysUntilNext,
                            isPinned = birthday.birthday.isPinned,
                            onClick = { onBirthdayClick(birthday) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekHeaders() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Seven-column day grid for one month.
 *
 * Leading blanks come from the real first-of-month weekday, and the grid is
 * always six rows tall so the pager does not change height between months.
 *
 * @param currentMonth the month being drawn
 * @param birthdaysInMonth people grouped by the date they land on
 * @param selectedDate currently selected date
 * @param onDateClick invoked with a tapped date
 */
@Composable
private fun CalendarDaysGrid(
    currentMonth: YearMonth,
    birthdaysInMonth: Map<LocalDate, List<BirthdayWithCountdown>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
) {
    // DayOfWeek.MONDAY.value == 1, so a month starting on Monday needs 0 blanks.
    val leadingBlanks = currentMonth.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
    val daysInMonth = currentMonth.lengthOfMonth()
    val today = LocalDate.now()

    val cells =
        remember(currentMonth) {
            buildList<LocalDate?> {
                repeat(leadingBlanks.coerceAtLeast(0)) { add(null) }
                for (day in 1..daysInMonth) add(currentMonth.atDay(day))
                // Pad to a full 42 cells (6 rows) so every month is the same height.
                while (size < 42) add(null)
            }
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(310.dp),
        userScrollEnabled = false,
    ) {
        items(cells) { date ->
            CalendarDayCell(
                date = date,
                birthdays = date?.let { birthdaysInMonth[it] } ?: emptyList(),
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { date?.let(onDateClick) },
            )
        }
    }
}

/**
 * One day cell. Marked days use primaryContainer with onPrimaryContainer ink
 * rather than a translucent primary, so the numeral and the dots stay legible
 * in both themes.
 *
 * @param date the day, or null for a padding cell
 * @param birthdays people whose birthday lands on this day
 * @param isSelected whether this day is selected
 * @param isToday whether this day is today
 * @param onClick invoked on tap
 */
@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    birthdays: List<BirthdayWithCountdown>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    if (date == null) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    // Resolve the container and its ink together, so they can never disagree.
    // Previously the "today" fill was painted by a second background() over the
    // first, which left the birthday dot in primary gold sitting on rosewood.
    val container: Color
    val contentColor: Color
    when {
        isSelected -> {
            container = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
        }

        isToday -> {
            container = MaterialTheme.colorScheme.secondary
            contentColor = MaterialTheme.colorScheme.onSecondary
        }

        birthdays.isNotEmpty() -> {
            container = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        }

        else -> {
            container = MaterialTheme.colorScheme.surface
            contentColor = MaterialTheme.colorScheme.onSurface
        }
    }

    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .clip(SaffronTokens.radiusSmall)
                .background(container)
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
            )

            if (birthdays.isNotEmpty()) {
                Spacer(Modifier.size(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(minOf(birthdays.size, 3)) {
                        Box(
                            modifier =
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(contentColor),
                        )
                    }
                }
            }
        }
    }
}
