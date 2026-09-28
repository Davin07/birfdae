package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.model.Relationship
import com.birthdayreminder.domain.model.SearchFilter
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Search, scoped by the relationship filters in the approved concept.
 *
 * The filters are All / Family / Friends / This month, matching the
 * [Relationship] vocabulary the add wizard writes, so a person saved as
 * "Parents" is findable by the same idea they were tagged with. The previous
 * Name/Month split filtered on text the app already had a better filter for,
 * and left `Birthday.relationship` unsearchable.
 */
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val calculateCountdownUseCase: CalculateCountdownUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        init {
            // The screen has no "run a search" action, so the initial load has
            // to happen here. Without it the list stays empty until the user
            // types, and opening Search reads as "no birthdays yet" even when
            // the home screen is full of people.
            performSearch()
        }

        fun onQueryChanged(query: String) {
            _uiState.value = _uiState.value.copy(query = query)
            performSearch()
        }

        /**
         * Narrows the list to one of the concept's filters.
         *
         * @param filter which slice to show
         */
        fun onFilterChanged(filter: SearchFilter) {
            if (_uiState.value.filter == filter) return
            _uiState.value = _uiState.value.copy(filter = filter)
            performSearch()
        }

        fun deleteBirthday(birthdayId: Long) {
            viewModelScope.launch {
                birthdayRepository.deleteBirthdayById(birthdayId)
                performSearch()
            }
        }

        private fun performSearch() {
            viewModelScope.launch {
                // Every path reads the whole list and narrows in memory. The
                // database holds a person's name and their relationship but no
                // index on either, and a phone's worth of birthdays is a few
                // hundred rows, so a SQL filter would add a query surface
                // without changing what the user sees.
                birthdayRepository.getAllBirthdays().collectLatest { birthdays ->
                    val query = _uiState.value.query.trim()
                    val filter = _uiState.value.filter

                    val results =
                        birthdays
                            .filter { it.matchesFilter(filter) }
                            .filter { it.matchesQuery(query) }
                            .map { calculateCountdownUseCase.calculateCountdown(it) }
                            .sortedBy { it.daysUntilNext }

                    _uiState.value = _uiState.value.copy(results = results)
                }
            }
        }
    }

/**
 * Whether this person belongs under the active relationship filter.
 *
 * @param filter the selected chip
 */
private fun Birthday.matchesFilter(filter: SearchFilter): Boolean =
    when (filter) {
        SearchFilter.ALL -> true
        // "Family" is one chip and covers parents, because the concept's chip
        // is the only place a parent could be reached and a separate Parents
        // filter would leave someone tagged Parents unfindable.
        SearchFilter.FAMILY ->
            Relationship.FAMILY.matches(relationship) ||
                Relationship.PARENTS.matches(relationship)
        SearchFilter.FRIENDS -> Relationship.FRIENDS.matches(relationship)
        // From today to the end of the month, so someone whose date is in three
        // weeks still counts as "this month".
        SearchFilter.THIS_MONTH -> {
            val now = LocalDate.now()
            birthDate.monthValue == now.monthValue && birthDate.dayOfMonth >= now.dayOfMonth
        }
    }

/**
 * Whether this person matches the typed query.
 *
 * A blank query matches everyone, which is what makes Search show the full list
 * on open rather than an empty state. The name is the primary match; the
 * relationship is also matched so that typing "parents" reaches a parent, which
 * costs nothing and is what someone would expect to work.
 *
 * @param query the text typed, possibly empty
 */
private fun Birthday.matchesQuery(query: String): Boolean {
    if (query.isEmpty()) return true
    return name.contains(query, ignoreCase = true) ||
        relationship?.contains(query, ignoreCase = true) == true
}

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val results: List<BirthdayWithCountdown> = emptyList(),
)
