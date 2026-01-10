package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val calculateCountdownUseCase: CalculateCountdownUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        fun onQueryChanged(query: String) {
            _uiState.value = _uiState.value.copy(query = query)
            performSearch()
        }

        fun onSearchTypeChanged(type: SearchType) {
            _uiState.value = _uiState.value.copy(searchType = type)
            performSearch()
        }

        fun deleteBirthday(birthdayId: Long) {
            viewModelScope.launch {
                birthdayRepository.deleteBirthdayById(birthdayId)
                performSearch()
            }
        }

        private fun performSearch() {
            val query = _uiState.value.query
            val type = _uiState.value.searchType

            viewModelScope.launch {
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(results = emptyList())
                    return@launch
                }

                if (type == SearchType.NAME) {
                    birthdayRepository.searchBirthdaysByName(query).collectLatest { birthdays ->
                        val results =
                            birthdays.map {
                                calculateCountdownUseCase.calculateCountdown(it)
                            }.sortedBy { it.daysUntilNext }
                        _uiState.value = _uiState.value.copy(results = results)
                    }
                } else {
                    val month = mapMonthToNumber(query)
                    if (month != null) {
                        birthdayRepository.getBirthdaysForMonth(month).collectLatest { birthdays ->
                            val results =
                                birthdays.map {
                                    calculateCountdownUseCase.calculateCountdown(it)
                                }.sortedBy { it.daysUntilNext }
                            _uiState.value = _uiState.value.copy(results = results)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(results = emptyList())
                    }
                }
            }
        }

        private fun mapMonthToNumber(query: String): String? {
            val months =
                listOf("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")
            val index = months.indexOfFirst { it.startsWith(query.lowercase()) }

            // Also support numbers 1-12
            if (index == -1) {
                val num = query.toIntOrNull()
                if (num != null && num in 1..12) {
                    return String.format("%02d", num)
                }
            }

            return if (index != -1) String.format("%02d", index + 1) else null
        }
    }

data class SearchUiState(
    val query: String = "",
    val searchType: SearchType = SearchType.NAME,
    val results: List<BirthdayWithCountdown> = emptyList(),
)

enum class SearchType {
    NAME,
    MONTH,
}
