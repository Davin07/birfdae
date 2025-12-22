package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.theme.BirthdayReminderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun calendarScreen_displaysCurrentMonth_initially() {
        // Given
        val currentMonth = YearMonth.now()
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        val monthName = currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val year = currentMonth.year.toString()
        composeTestRule.onNodeWithText("$monthName $year").assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_showsNavigationButtons() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_callsOnPreviousMonth_whenPreviousButtonClicked() {
        // Given
        var previousMonthClicked = false
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = { previousMonthClicked = true },
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // When
        composeTestRule.onNodeWithContentDescription("Previous month").performClick()
        
        // Then
        assert(previousMonthClicked)
    }
    
    @Test
    fun calendarScreen_callsOnNextMonth_whenNextButtonClicked() {
        // Given
        var nextMonthClicked = false
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = { nextMonthClicked = true },
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // When
        composeTestRule.onNodeWithContentDescription("Next month").performClick()
        
        // Then
        assert(nextMonthClicked)
    }
    
    @Test
    fun calendarScreen_displaysDaysOfWeek() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Sun").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Thu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fri").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sat").assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_displaysDatesForCurrentMonth() {
        // Given
        val currentMonth = YearMonth.of(2024, 6) // June 2024
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then - Should display dates 1-30 for June
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText("30").assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_showsBirthdayIndicators_whenBirthdaysExist() {
        // Given
        val currentMonth = YearMonth.of(2024, 6)
        val birthdaysInMonth = mapOf(
            LocalDate.of(2024, 6, 15) to listOf(
                createMockBirthdayWithCountdown("Alice", LocalDate.of(2024, 6, 15))
            ),
            LocalDate.of(2024, 6, 20) to listOf(
                createMockBirthdayWithCountdown("Bob", LocalDate.of(2024, 6, 20))
            )
        )
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = birthdaysInMonth,
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then - Dates with birthdays should have indicators
        composeTestRule.onNode(
            hasText("15") and hasTestTag("date_with_birthday")
        ).assertIsDisplayed()
        composeTestRule.onNode(
            hasText("20") and hasTestTag("date_with_birthday")
        ).assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_callsOnDateClick_whenDateClicked() {
        // Given
        val currentMonth = YearMonth.of(2024, 6)
        var clickedDate: LocalDate? = null
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = { clickedDate = it },
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // When
        composeTestRule.onNodeWithText("15").performClick()
        
        // Then
        assert(clickedDate == LocalDate.of(2024, 6, 15))
    }
    
    @Test
    fun calendarScreen_highlightsSelectedDate_whenDateSelected() {
        // Given
        val currentMonth = YearMonth.of(2024, 6)
        val selectedDate = LocalDate.of(2024, 6, 15)
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = selectedDate,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNode(
            hasText("15") and hasTestTag("selected_date")
        ).assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_showsSelectedDateBirthdays_whenDateSelected() {
        // Given
        val currentMonth = YearMonth.of(2024, 6)
        val selectedDate = LocalDate.of(2024, 6, 15)
        val selectedDateBirthdays = listOf(
            createMockBirthdayWithCountdown("Alice Johnson", LocalDate.of(2024, 6, 15)),
            createMockBirthdayWithCountdown("Bob Smith", LocalDate.of(2024, 6, 15))
        )
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = selectedDate,
                    selectedDateBirthdays = selectedDateBirthdays,
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Birthdays on June 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_callsOnClearSelection_whenClearButtonClicked() {
        // Given
        val selectedDate = LocalDate.of(2024, 6, 15)
        var clearSelectionCalled = false
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.of(2024, 6),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = selectedDate,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = { clearSelectionCalled = true },
                    onBirthdayClick = {}
                )
            }
        }
        
        // When
        composeTestRule.onNodeWithContentDescription("Clear selection").performClick()
        
        // Then
        assert(clearSelectionCalled)
    }
    
    @Test
    fun calendarScreen_callsOnBirthdayClick_whenBirthdayClicked() {
        // Given
        val selectedDate = LocalDate.of(2024, 6, 15)
        val birthday = createMockBirthdayWithCountdown("Clickable Person", LocalDate.of(2024, 6, 15))
        var clickedBirthday: BirthdayWithCountdown? = null
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.of(2024, 6),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = selectedDate,
                    selectedDateBirthdays = listOf(birthday),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = { clickedBirthday = it }
                )
            }
        }
        
        // When
        composeTestRule.onNodeWithText("Clickable Person").performClick()
        
        // Then
        assert(clickedBirthday == birthday)
    }
    
    @Test
    fun calendarScreen_showsLoadingIndicator_whenLoading() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = true,
                    errorMessage = null,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNode(hasTestTag("loading_indicator")).assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_showsErrorMessage_whenErrorExists() {
        // Given
        val errorMessage = "Failed to load calendar data"
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = YearMonth.now(),
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = errorMessage,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
    
    @Test
    fun calendarScreen_navigatesMonths_maintainsState() {
        // Given
        var currentMonth = YearMonth.of(2024, 6)
        
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                CalendarScreen(
                    currentMonth = currentMonth,
                    birthdaysInMonth = emptyMap(),
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onDateClick = {},
                    onClearSelection = {},
                    onBirthdayClick = {}
                )
            }
        }
        
        // When - Navigate to next month
        composeTestRule.onNodeWithContentDescription("Next month").performClick()
        
        // Then - Should show July 2024
        composeTestRule.onNodeWithText("July 2024").assertIsDisplayed()
    }
    
    private fun createMockBirthdayWithCountdown(
        name: String,
        nextOccurrence: LocalDate,
        id: Long = 1L
    ): BirthdayWithCountdown {
        val birthday = Birthday(
            id = id,
            name = name,
            birthDate = LocalDate.of(1990, nextOccurrence.monthValue, nextOccurrence.dayOfMonth),
            notes = null,
            notificationsEnabled = true,
            advanceNotificationDays = 0,
            createdAt = LocalDateTime.now()
        )
        
        return BirthdayWithCountdown(
            birthday = birthday,
            daysUntilNext = 30,
            nextOccurrence = nextOccurrence,
            isToday = false,
            age = 30
        )
    }
}