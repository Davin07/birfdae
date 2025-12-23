package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.usecase.AddBirthdayResult
import com.birthdayreminder.domain.usecase.AddBirthdayUseCase
import com.birthdayreminder.domain.usecase.DeleteBirthdayResult
import com.birthdayreminder.domain.usecase.DeleteBirthdayUseCase
import com.birthdayreminder.domain.usecase.GetAllBirthdaysUseCase
import com.birthdayreminder.domain.usecase.UpdateBirthdayResult
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the birthday list screen.
 * Manages UI state and handles user interactions for the chronological birthday list.
 */
@HiltViewModel
class BirthdayListViewModel
    @Inject
    constructor(
        private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
        private val addBirthdayUseCase: AddBirthdayUseCase,
        private val updateBirthdayUseCase: UpdateBirthdayUseCase,
        private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BirthdayListUiState())
        val uiState: StateFlow<BirthdayListUiState> = _uiState.asStateFlow()

        // Keep track of active jobs to prevent memory leaks
        private var loadBirthdaysJob: Job? = null
        private var refreshJob: Job? = null

        init {
            loadBirthdays()
        }

        override fun onCleared() {
            super.onCleared()
            // Cancel any ongoing operations to prevent memory leaks
            loadBirthdaysJob?.cancel()
            refreshJob?.cancel()
        }

        /**
         * Loads all birthdays sorted by next occurrence.
         * Optimized with background thread execution and distinct emissions.
         */
        fun loadBirthdays() {
            loadBirthdaysJob?.cancel()
            loadBirthdaysJob =
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                    getAllBirthdaysUseCase.getAllBirthdaysSortedByNextOccurrence()
                        .distinctUntilChanged() // Only emit when data actually changes
                        .flowOn(Dispatchers.IO) // Execute on background thread
                        .catch { exception ->
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = exception.message ?: "Failed to load birthdays",
                                )
                        }
                        .collect { birthdays ->
                            _uiState.value =
                                _uiState.value.copy(
                                    birthdays = birthdays,
                                    isLoading = false,
                                    errorMessage = null,
                                )
                        }
                }
        }

        /**
         * Refreshes the birthday list.
         * Optimized to prevent multiple concurrent refresh operations.
         */
        fun refresh() {
            refreshJob?.cancel()
            refreshJob =
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

                    getAllBirthdaysUseCase.getAllBirthdaysSortedByNextOccurrence()
                        .distinctUntilChanged()
                        .flowOn(Dispatchers.IO)
                        .catch { exception ->
                            _uiState.value =
                                _uiState.value.copy(
                                    isRefreshing = false,
                                    errorMessage = exception.message ?: "Failed to refresh birthdays",
                                )
                        }
                        .collect { birthdays ->
                            _uiState.value =
                                _uiState.value.copy(
                                    birthdays = birthdays,
                                    isRefreshing = false,
                                    errorMessage = null,
                                )
                        }
                }
        }

        /**
         * Clears any error messages.
         */
        fun clearError() {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }

        /**
         * Adds a new birthday.
         */
        fun addBirthday(
            name: String,
            birthDate: LocalDate,
            notes: String? = null,
            notificationsEnabled: Boolean = true,
            advanceNotificationDays: Int = 0,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                when (
                    val result =
                        addBirthdayUseCase.addBirthday(
                            name = name,
                            birthDate = birthDate,
                            notes = notes,
                            notificationsEnabled = notificationsEnabled,
                            advanceNotificationDays = advanceNotificationDays,
                        )
                ) {
                    is AddBirthdayResult.Success -> {
                        // Birthday added successfully, the flow will automatically update
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is AddBirthdayResult.ValidationError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.errors.joinToString(", "),
                            )
                    }
                    is AddBirthdayResult.DatabaseError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                    }
                }
            }
        }

        /**
         * Updates an existing birthday.
         */
        fun updateBirthday(
            birthdayId: Long,
            name: String,
            birthDate: LocalDate,
            notes: String? = null,
            notificationsEnabled: Boolean = true,
            advanceNotificationDays: Int = 0,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                when (
                    val result =
                        updateBirthdayUseCase.updateBirthday(
                            birthdayId = birthdayId,
                            name = name,
                            birthDate = birthDate,
                            notes = notes,
                            notificationsEnabled = notificationsEnabled,
                            advanceNotificationDays = advanceNotificationDays,
                        )
                ) {
                    is UpdateBirthdayResult.Success -> {
                        // Birthday updated successfully, the flow will automatically update
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is UpdateBirthdayResult.ValidationError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.errors.joinToString(", "),
                            )
                    }
                    is UpdateBirthdayResult.NotFound -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                    }
                    is UpdateBirthdayResult.DatabaseError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                    }
                }
            }
        }

        /**
         * Deletes a birthday by ID.
         */
        fun deleteBirthday(birthdayId: Long) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                when (val result = deleteBirthdayUseCase.deleteBirthdayById(birthdayId)) {
                    is DeleteBirthdayResult.Success -> {
                        // Birthday deleted successfully, the flow will automatically update
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is DeleteBirthdayResult.NotFound -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                    }
                    is DeleteBirthdayResult.DatabaseError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                    }
                }
            }
        }

        /**
         * Toggles notification settings for a specific birthday.
         */
        fun toggleNotifications(
            birthdayId: Long,
            enabled: Boolean,
        ) {
            viewModelScope.launch {
                when (
                    val result =
                        updateBirthdayUseCase.updateBirthdayPartial(
                            birthdayId = birthdayId,
                            notificationsEnabled = enabled,
                        )
                ) {
                    is UpdateBirthdayResult.Success -> {
                        // Notification setting updated successfully
                    }
                    is UpdateBirthdayResult.ValidationError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = result.errors.joinToString(", "),
                            )
                    }
                    is UpdateBirthdayResult.NotFound -> {
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = result.message,
                            )
                    }
                    is UpdateBirthdayResult.DatabaseError -> {
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = result.message,
                            )
                    }
                }
            }
        }
    }

/**
 * UI state for the birthday list screen.
 */
data class BirthdayListUiState(
    val birthdays: List<BirthdayWithCountdown> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val operationInProgress: Boolean = false,
) {
    val isEmpty: Boolean get() = birthdays.isEmpty() && !isLoading && !isRefreshing
    val hasError: Boolean get() = errorMessage != null
    val showEmptyState: Boolean get() = isEmpty && !hasError
}
