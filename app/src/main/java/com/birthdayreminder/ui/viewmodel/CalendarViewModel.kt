package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.usecase.GetAllBirthdaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CalendarUiState())
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        init {
            loadAllBirthdays()
        }

        fun navigateToPreviousMonth() {
            _uiState.update { 
                it.copy(
                    currentMonth = it.currentMonth.minusMonths(1),
                    selectedDate = null 
                ) 
            }
        }

        fun navigateToNextMonth() {
            _uiState.update { 
                it.copy(
                    currentMonth = it.currentMonth.plusMonths(1),
                    selectedDate = null 
                ) 
            }
        }

        fun navigateToCurrentMonth() {
            _uiState.update { 
                it.copy(
                    currentMonth = YearMonth.now(),
                    selectedDate = null 
                ) 
            }
        }

        fun navigateToMonth(yearMonth: YearMonth) {
            _uiState.update { 
                it.copy(
                    currentMonth = yearMonth,
                    selectedDate = null 
                ) 
            }
        }

        fun selectDate(date: LocalDate) {
            _uiState.update { it.copy(selectedDate = date) }
        }

        fun clearSelectedDate() {
            _uiState.update { it.copy(selectedDate = null) }
        }

        fun refresh() {
            // Flow updates automatically, but we can trigger reload if needed
            loadAllBirthdays()
        }

        fun clearError() {
            _uiState.update { it.copy(errorMessage = null) }
        }

        private fun loadAllBirthdays() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    getAllBirthdaysUseCase.getAllBirthdaysSortedByNextOccurrence()
                        .collect { birthdays ->
                            _uiState.update { 
                                it.copy(
                                    allBirthdays = birthdays,
                                    isLoading = false,
                                    errorMessage = null
                                ) 
                            }
                        }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load birthdays"
                        ) 
                    }
                }
            }
        }
    }

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val allBirthdays: List<BirthdayWithCountdown> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasError: Boolean get() = errorMessage != null
    val hasSelectedDate: Boolean get() = selectedDate != null
    
    // Helper to get birthdays for selected date (computed in UI or here)
    // We'll compute in UI for flexibility
}
