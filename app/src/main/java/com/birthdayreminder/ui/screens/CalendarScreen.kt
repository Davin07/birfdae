package com.birthdayreminder.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.components.BirthdayCard
import com.birthdayreminder.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Calendar screen that displays birthdays in a monthly calendar grid format.
 * Shows birthday indicators on relevant dates and allows month navigation.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onBirthdayClick: (BirthdayWithCountdown) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize PagerState with a large enough page count to allow scrolling in both directions
    // We'll use a fixed large number and adjust the displayed month based on the current page
    val pagerState = rememberPagerState(
        initialPage = 1000,
        pageCount = { 2001 } // 2001 pages centered around the current month
    )
    
    // Update the view model when the page changes
    LaunchedEffect(pagerState.currentPage) {
        val initialMonth = YearMonth.now()
        val targetMonth = initialMonth.plusMonths((pagerState.currentPage - 1000).toLong())
        viewModel.navigateToMonth(targetMonth)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month navigation header
        CalendarHeader(
            currentMonth = uiState.currentMonth,
            onPreviousMonth = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNextMonth = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onTodayClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(1000)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar grid with horizontal pager
        CalendarPager(
            pagerState = pagerState,
            birthdaysInMonth = uiState.birthdaysInMonth,
            selectedDate = uiState.selectedDate,
            onDateClick = viewModel::selectDate,
            isLoading = uiState.isLoading
        )
        
        // Selected date details
        if (uiState.hasSelectedDate && uiState.selectedDateHasBirthdays) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SelectedDateDetails(
                selectedDate = uiState.selectedDate!!,
                birthdays = uiState.selectedDateBirthdays,
                onBirthdayClick = onBirthdayClick,
                onDismiss = viewModel::clearSelectedDate
            )
        }
        
        // Error handling
        if (uiState.hasError) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error loading calendar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.errorMessage ?: "Unknown error occurred",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.refresh()
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Calendar pager that allows swiping between months.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarPager(
    pagerState: PagerState,
    birthdaysInMonth: Map<LocalDate, List<BirthdayWithCountdown>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    isLoading: Boolean
) {
    Column {
        // Day of week headers
        DayOfWeekHeaders()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Horizontal pager for calendar months
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val initialMonth = YearMonth.now()
                val currentMonth = initialMonth.plusMonths((page - 1000).toLong())
                
                CalendarDaysGrid(
                    currentMonth = currentMonth,
                    birthdaysInMonth = birthdaysInMonth,
                    selectedDate = selectedDate,
                    onDateClick = onDateClick
                )
            }
        }
    }
}

/**
 * Calendar header with month navigation controls.
 */
@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous month button
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }
        
        // Current month and year
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Next month button
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
    
    // Today button (centered below month/year)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onTodayClick,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Today")
        }
    }
}

/**
 * Headers showing days of the week.
 */
@Composable
private fun DayOfWeekHeaders() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DayOfWeek.values().forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Grid of calendar days with birthday indicators.
 */
@Composable
private fun CalendarDaysGrid(
    currentMonth: YearMonth,
    birthdaysInMonth: Map<LocalDate, List<BirthdayWithCountdown>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Convert to 0-6 (Sunday = 0)
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // Calculate total cells needed (6 weeks max)
    val totalCells = 42
    val dates = mutableListOf<LocalDate?>()
    
    // Add empty cells for days before the first day of the month
    repeat(firstDayOfWeek) {
        dates.add(null)
    }
    
    // Add all days of the current month
    for (day in 1..daysInMonth) {
        dates.add(currentMonth.atDay(day))
    }
    
    // Fill remaining cells with nulls
    while (dates.size < totalCells) {
        dates.add(null)
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(dates) { date ->
            CalendarDayCell(
                date = date,
                birthdays = date?.let { birthdaysInMonth[it] } ?: emptyList(),
                isSelected = date == selectedDate,
                isToday = date == LocalDate.now(),
                onClick = { date?.let(onDateClick) }
            )
        }
    }
}

/**
 * Individual calendar day cell with birthday indicator.
 */
@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    birthdays: List<BirthdayWithCountdown>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val hasBirthdays = birthdays.isNotEmpty()
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = date != null) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Day number
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Birthday indicator
                if (hasBirthdays) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Show up to 3 dots for birthdays
                        repeat(minOf(birthdays.size, 3)) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.tertiary
                                    )
                            )
                            if (it < minOf(birthdays.size, 3) - 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                        
                        // Show "+" if more than 3 birthdays
                        if (birthdays.size > 3) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+",
                                fontSize = 8.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Details panel showing birthdays for the selected date.
 */
@Composable
private fun SelectedDateDetails(
    selectedDate: LocalDate,
    birthdays: List<BirthdayWithCountdown>,
    onBirthdayClick: (BirthdayWithCountdown) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Birthdays on ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            birthdays.forEach { birthday ->
                BirthdayCard(
                    birthday = birthday,
                    onClick = { onBirthdayClick(birthday) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (birthday != birthdays.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}