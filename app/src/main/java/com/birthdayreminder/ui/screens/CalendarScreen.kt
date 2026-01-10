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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaBirthdayCard
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.components.LuminaHeader
import com.birthdayreminder.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onBirthdayClick: (BirthdayWithCountdown) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val pagerState =
        rememberPagerState(
            initialPage = 1000,
            pageCount = { 2001 },
        )

    LaunchedEffect(pagerState.currentPage) {
        val initialMonth = YearMonth.now()
        val targetMonth = initialMonth.plusMonths((pagerState.currentPage - 1000).toLong())
        viewModel.navigateToMonth(targetMonth)
    }

    LuminaBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            LuminaHeader(
                title = "Calendar",
                actions = {
                    TextButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch { pagerState.animateScrollToPage(1000) }
                        },
                    ) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Today")
                    }
                },
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Month Selector
                LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White)
                        }

                        Text(
                            text = uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )

                        IconButton(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White)
                        }
                    }
                }

                // Calendar Grid
                LuminaGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DayOfWeekHeaders()
                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.isLoading) {
                            Box(Modifier.fillMaxWidth().height(310.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                            ) { page ->
                                val initialMonth = YearMonth.now()
                                val month = initialMonth.plusMonths((page - 1000).toLong())

                                val birthdaysInMonth =
                                    remember(month, uiState.allBirthdays) {
                                        uiState.allBirthdays.filter {
                                            it.birthDate.month == month.month
                                        }.groupBy {
                                            try {
                                                LocalDate.of(month.year, month.month, it.birthDate.dayOfMonth)
                                            } catch (e: Exception) {
                                                LocalDate.of(month.year, month.month, 28)
                                            }
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
                    }
                }

                // List (Bottom)
                val birthdaysToShow =
                    remember(uiState.selectedDate, uiState.currentMonth, uiState.allBirthdays) {
                        if (uiState.selectedDate != null) {
                            uiState.allBirthdays.filter {
                                it.birthDate.month == uiState.selectedDate!!.month &&
                                    it.birthDate.dayOfMonth == uiState.selectedDate!!.dayOfMonth
                            }
                        } else {
                            uiState.allBirthdays.filter {
                                it.birthDate.month == uiState.currentMonth.month
                            }.sortedBy { it.birthDate.dayOfMonth }
                        }
                    }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 140.dp), // Slightly increased padding
                ) {
                    items(birthdaysToShow) { birthday ->
                        LuminaBirthdayCard(
                            name = birthday.name,
                            imageUri = birthday.birthday.imageUri,
                            dateString = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                            age = birthday.age,
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
        DayOfWeek.values().forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    currentMonth: YearMonth,
    birthdaysInMonth: Map<LocalDate, List<BirthdayWithCountdown>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val totalCells = 42
    val dates = mutableListOf<LocalDate?>()

    repeat(firstDayOfWeek) { dates.add(null) }
    for (day in 1..daysInMonth) {
        dates.add(currentMonth.atDay(day))
    }
    while (dates.size < totalCells) {
        dates.add(null)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(310.dp), // Adjusted height
        userScrollEnabled = false,
    ) {
        items(dates) { date ->
            CalendarDayCell(
                date = date,
                birthdays = date?.let { birthdaysInMonth[it] } ?: emptyList(),
                isSelected = date == selectedDate,
                isToday = date == LocalDate.now(),
                onClick = { date?.let(onDateClick) },
            )
        }
    }
}

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

    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                )
                .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color =
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.secondary
                        else -> Color.White.copy(alpha = 0.8f)
                    },
            )

            if (birthdays.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(minOf(birthdays.size, 3)) {
                        Box(
                            modifier =
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}
