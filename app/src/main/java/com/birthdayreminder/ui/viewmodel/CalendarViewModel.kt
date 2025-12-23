package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.usecase.GetAllBirthdaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * ViewModel for the calendar screen.
 * Manages UI state for monthly calendar view with birthday indicators.
 */
@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CalendarUiState())
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        init {
            loadBirthdaysForCurrentMonth()
        }

        /**
         * Navigates to the previous month.
         */
        fun navigateToPreviousMonth() {
            val currentMonth = _uiState.value.currentMonth
            val previousMonth = currentMonth.minusMonths(1)
            _uiState.value = _uiState.value.copy(currentMonth = previousMonth)
            loadBirthdaysForMonth(previousMonth)
        }

        /**
         * Navigates to the next month.
         */
        fun navigateToNextMonth() {
            val currentMonth = _uiState.value.currentMonth
            val nextMonth = currentMonth.plusMonths(1)
            _uiState.value = _uiState.value.copy(currentMonth = nextMonth)
            loadBirthdaysForMonth(nextMonth)
        }

        /**
         * Navigates to the current month (today).
         */
        fun navigateToCurrentMonth() {
            val currentMonth = YearMonth.now()
            _uiState.value = _uiState.value.copy(currentMonth = currentMonth)
            loadBirthdaysForMonth(currentMonth)
        }

        /**
         * Navigates to a specific month and year.
         */
        fun navigateToMonth(yearMonth: YearMonth) {
            _uiState.value = _uiState.value.copy(currentMonth = yearMonth)
            loadBirthdaysForMonth(yearMonth)
        }

        /**
         * Handles date selection in the calendar.
         */
        fun selectDate(date: LocalDate) {
            val birthdaysOnDate = _uiState.value.birthdaysInMonth[date] ?: emptyList()
            _uiState.value =
                _uiState.value.copy(
                    selectedDate = date,
                    selectedDateBirthdays = birthdaysOnDate,
                )
        }

        /**
         * Clears the selected date.
         */
        fun clearSelectedDate() {
            _uiState.value =
                _uiState.value.copy(
                    selectedDate = null,
                    selectedDateBirthdays = emptyList(),
                )
        }

        /**
         * Refreshes the birthdays for the current month.
         */
        fun refresh() {
            loadBirthdaysForMonth(_uiState.value.currentMonth)
        }

        /**
         * Clears any error messages.
         */
        fun clearError() {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }

        /**
         * Loads birthdays for the current month.
         */
        private fun loadBirthdaysForCurrentMonth() {
            loadBirthdaysForMonth(YearMonth.now())
        }

        /**
         * Loads birthdays for a specific month and groups them by date.
         */
        private fun loadBirthdaysForMonth(yearMonth: YearMonth) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                getAllBirthdaysUseCase.getBirthdaysForMonth(
                    month = yearMonth.monthValue,
                    year = yearMonth.year,
                )
                    .catch { exception ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Failed to load birthdays for month",
                            )
                    }
                    .collect { birthdays ->
                        val birthdaysGroupedByDate = groupBirthdaysByDate(birthdays)
                        _uiState.value =
                            _uiState.value.copy(
                                birthdaysInMonth = birthdaysGroupedByDate,
                                isLoading = false,
                                errorMessage = null,
                            )
                    }
            }
        }

        /**
         * Groups birthdays by their occurrence date within the month.
         */
        private fun groupBirthdaysByDate(
            birthdays: List<BirthdayWithCountdown>,
        ): Map<LocalDate, List<BirthdayWithCountdown>> {
            return birthdays.groupBy { birthday ->
                // Group by the actual date in the current month/year
                val currentMonth = _uiState.value.currentMonth
                LocalDate.of(
                    currentMonth.year,
                    currentMonth.monthValue,
                    birthday.birthDate.dayOfMonth,
                )
            }
        }

        /**
         * Gets birthdays for a specific date.
         */
        fun getBirthdaysForDate(date: LocalDate): List<BirthdayWithCountdown> {
            return _uiState.value.birthdaysInMonth[date] ?: emptyList()
        }

        /**
         * Checks if a date has any birthdays.
         */
        fun hasAnyBirthdaysOnDate(date: LocalDate): Boolean {
            return _uiState.value.birthdaysInMonth.containsKey(date)
        }

        /**
         * Gets the number of birthdays on a specific date.
         */
        fun getBirthdayCountForDate(date: LocalDate): Int {
            return _uiState.value.birthdaysInMonth[date]?.size ?: 0
        }
    }

/**
 * UI state for the calendar screen.
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val birthdaysInMonth: Map<LocalDate, List<BirthdayWithCountdown>> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateBirthdays: List<BirthdayWithCountdown> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasError: Boolean get() = errorMessage != null
    val hasSelectedDate: Boolean get() = selectedDate != null
    val selectedDateHasBirthdays: Boolean get() = selectedDateBirthdays.isNotEmpty()

    /**
     * Gets all dates in the current month that have birthdays.
     */
    val datesWithBirthdays: Set<LocalDate> get() = birthdaysInMonth.keys

    /**
     * Gets the total number of birthdays in the current month.
     */
    val totalBirthdaysInMonth: Int get() = birthdaysInMonth.values.sumOf { it.size }
}
