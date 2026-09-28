package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.error.ErrorResult
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.model.OverdueBirthday
import com.birthdayreminder.domain.model.OverdueCalculator
import com.birthdayreminder.domain.usecase.AddBirthdayResult
import com.birthdayreminder.domain.usecase.AddBirthdayUseCase
import com.birthdayreminder.domain.usecase.DeleteBirthdayResult
import com.birthdayreminder.domain.usecase.DeleteBirthdayUseCase
import com.birthdayreminder.domain.usecase.GetAllBirthdaysUseCase
import com.birthdayreminder.domain.usecase.SkipBirthdayForYearUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
        private val skipBirthdayForYearUseCase: SkipBirthdayForYearUseCase,
        private val errorHandler: ErrorHandler,
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
                    _uiState.value = _uiState.value.copy(isLoading = true, errorResult = null)

                    getAllBirthdaysUseCase.getAllBirthdaysSortedByNextOccurrence()
                        .distinctUntilChanged() // Only emit when data actually changes
                        .flowOn(Dispatchers.IO) // Execute on background thread
                        .catch { exception ->
                            Timber.e(exception, "Failed to load birthdays")
                            val errorResult = errorHandler.createErrorResult(exception, "load birthdays")
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        .collect { birthdays ->
                            _uiState.value =
                                _uiState.value.copy(
                                    birthdays = birthdays,
                                    overdue = computeOverdue(birthdays),
                                    isLoading = false,
                                    errorResult = null,
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
                    _uiState.value = _uiState.value.copy(isRefreshing = true, errorResult = null)

                    getAllBirthdaysUseCase.getAllBirthdaysSortedByNextOccurrence()
                        .distinctUntilChanged()
                        .flowOn(Dispatchers.IO)
                        .catch { exception ->
                            Timber.e(exception, "Failed to refresh birthdays")
                            val errorResult = errorHandler.createErrorResult(exception, "refresh birthdays")
                            _uiState.value =
                                _uiState.value.copy(
                                    isRefreshing = false,
                                    errorResult = errorResult,
                                )
                        }
                        .collect { birthdays ->
                            _uiState.value =
                                _uiState.value.copy(
                                    birthdays = birthdays,
                                    overdue = computeOverdue(birthdays),
                                    isRefreshing = false,
                                    errorResult = null,
                                )
                        }
                }
        }

        /**
         * Clears any error messages.
         */
        fun clearError() {
            _uiState.value = _uiState.value.copy(errorResult = null)
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorResult = null)

                try {
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
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalArgumentException(result.errors.joinToString(", ")),
                                    "add birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is AddBirthdayResult.DatabaseError -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    result.exception,
                                    "add birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is AddBirthdayResult.ExactAlarmPermissionNotGranted -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    SecurityException("Exact alarm permission is required for notifications"),
                                    "add birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to add birthday")
                    val errorResult = errorHandler.createErrorResult(e, "add birthday")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorResult = errorResult,
                        )
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorResult = null)

                try {
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
                            // Birthday updated successfully, flow will automatically update
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is UpdateBirthdayResult.ValidationError -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalArgumentException(result.errors.joinToString(", ")),
                                    "update birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is UpdateBirthdayResult.NotFound -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalStateException(result.message),
                                    "update birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is UpdateBirthdayResult.DatabaseError -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    result.exception,
                                    "update birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is UpdateBirthdayResult.ExactAlarmPermissionNotGranted -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    SecurityException("Exact alarm permission is required for notifications"),
                                    "update birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to update birthday")
                    val errorResult = errorHandler.createErrorResult(e, "update birthday")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorResult = errorResult,
                        )
                }
            }
        }

        /**
         * Deletes a birthday by ID.
         */
        fun deleteBirthday(birthdayId: Long) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorResult = null)

                try {
                    when (val result = deleteBirthdayUseCase.deleteBirthdayById(birthdayId)) {
                        is DeleteBirthdayResult.Success -> {
                            // Birthday deleted successfully, the flow will automatically update
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is DeleteBirthdayResult.NotFound -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalStateException(result.message),
                                    "delete birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                        is DeleteBirthdayResult.DatabaseError -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    Exception(result.message),
                                    "delete birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete birthday")
                    val errorResult = errorHandler.createErrorResult(e, "delete birthday")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorResult = errorResult,
                        )
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
                try {
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
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalArgumentException(result.errors.joinToString(", ")),
                                    "toggle notifications",
                                )
                            _uiState.value =
                                _uiState.value.copy(errorResult = errorResult)
                        }
                        is UpdateBirthdayResult.NotFound -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalStateException(result.message),
                                    "toggle notifications",
                                )
                            _uiState.value =
                                _uiState.value.copy(errorResult = errorResult)
                        }
                        is UpdateBirthdayResult.DatabaseError -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    result.exception,
                                    "toggle notifications",
                                )
                            _uiState.value =
                                _uiState.value.copy(errorResult = errorResult)
                        }
                        is UpdateBirthdayResult.ExactAlarmPermissionNotGranted -> {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    SecurityException("Exact alarm permission is required for notifications"),
                                    "toggle notifications",
                                )
                            _uiState.value =
                                _uiState.value.copy(errorResult = errorResult)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to toggle notifications")
                    val errorResult = errorHandler.createErrorResult(e, "toggle notifications")
                    _uiState.value =
                        _uiState.value.copy(errorResult = errorResult)
                }
            }
        }

        /**
         * Records that the user is not celebrating this birthday this year.
         *
         * The overdue card has no dismiss, because a dismiss that does not
         * persist brings the same prompt back on the next launch.
         *
         * @param birthdayId the person being skipped
         */
        fun skipForThisYear(birthdayId: Long) {
            viewModelScope.launch {
                try {
                    if (skipBirthdayForYearUseCase(birthdayId)) {
                        // Drop it from the prompt immediately rather than
                        // waiting for the database flow to round-trip.
                        _uiState.update { it.copy(overdue = it.overdue.filterNot { o -> o.id == birthdayId }) }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to skip birthday for this year")
                    _uiState.update { it.copy(errorResult = errorHandler.createErrorResult(e, "skip birthday")) }
                }
            }
        }

        /**
         * Works out which birthdays are currently overdue.
         *
         * The countdown flow always rolls forward to the next occurrence, so a
         * date that has already passed is invisible to it. Overdue is derived
         * here from the same rows, and is sorted most-recently-missed first.
         *
         * @param birthdays the loaded birthdays with countdowns
         * @return the ones needing acknowledgement, oldest miss first
         */
        private fun computeOverdue(birthdays: List<BirthdayWithCountdown>): List<OverdueBirthday> =
            birthdays
                .mapNotNull { OverdueCalculator.overdueFor(it.birthday) }
                .sortedByDescending { it.daysOverdue }

        fun togglePin(birthdayId: Long) {
            viewModelScope.launch {
                try {
                    val currentPinnedId = _uiState.value.birthdays.find { it.birthday.isPinned }?.id
                    val isCurrentlyPinned = currentPinnedId == birthdayId

                    // Unpin existing if pinning a new one
                    if (currentPinnedId != null && !isCurrentlyPinned) {
                        updateBirthdayUseCase.updateBirthdayPartial(
                            birthdayId = currentPinnedId,
                            isPinned = false,
                        )
                    }

                    // Toggle the target birthday
                    updateBirthdayUseCase.updateBirthdayPartial(
                        birthdayId = birthdayId,
                        isPinned = !isCurrentlyPinned,
                    )

                    // refresh() // Removed to rely on active Flow observation
                } catch (e: Exception) {
                    Timber.e(e, "Failed to toggle pin")
                }
            }
        }
    }

/**
 * UI state for birthday list screen.
 */
data class BirthdayListUiState(
    val birthdays: List<BirthdayWithCountdown> = emptyList(),
    /**
     * Birthdays whose date has passed and which the user has not yet
     * acknowledged. Surfaced above the list, because a missed birthday is more
     * urgent than an upcoming one.
     */
    val overdue: List<OverdueBirthday> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorResult: ErrorResult? = null,
    val operationInProgress: Boolean = false,
) {
    val isEmpty: Boolean get() = birthdays.isEmpty() && overdue.isEmpty() && !isLoading && !isRefreshing
    val hasError: Boolean get() = errorResult != null
    val showEmptyState: Boolean get() = isEmpty && !hasError
    val errorMessage: String? get() = errorResult?.message
}
