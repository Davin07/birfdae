package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.error.ErrorResult
import com.birthdayreminder.domain.usecase.AddBirthdayResult
import com.birthdayreminder.domain.usecase.AddBirthdayUseCase
import com.birthdayreminder.domain.usecase.UpdateBirthdayResult
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Add/Edit Birthday screen.
 * Manages form state, validation, and birthday operations.
 */
@HiltViewModel
class AddEditBirthdayViewModel
    @Inject
    constructor(
        private val addBirthdayUseCase: AddBirthdayUseCase,
        private val updateBirthdayUseCase: UpdateBirthdayUseCase,
        private val birthdayRepository: BirthdayRepository,
        private val errorHandler: ErrorHandler,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddEditBirthdayUiState())
        val uiState: StateFlow<AddEditBirthdayUiState> = _uiState.asStateFlow()

        /**
         * Initializes the form for editing an existing birthday.
         * If birthdayId is null, the form is initialized for adding a new birthday.
         */
        fun initializeForm(birthdayId: Long?) {
            if (birthdayId == null) {
                _uiState.value = AddEditBirthdayUiState(isEditMode = false)
            } else {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    try {
                        val birthday = birthdayRepository.getBirthdayById(birthdayId)
                        if (birthday != null) {
                            _uiState.value =
                                AddEditBirthdayUiState(
                                    isEditMode = true,
                                    birthdayId = birthdayId,
                                    name = birthday.name,
                                    birthDate = birthday.birthDate,
                                    notes = birthday.notes ?: "",
                                    notificationsEnabled = birthday.notificationsEnabled,
                                    advanceNotificationDays = birthday.advanceNotificationDays,
                                    notificationHour = birthday.notificationHour ?: 9,
                                    notificationMinute = birthday.notificationMinute ?: 0,
                                    isLoading = false,
                                )
                        } else {
                            val errorResult =
                                errorHandler.createErrorResult(
                                    IllegalStateException("Birthday not found"),
                                    "load birthday",
                                )
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorResult = errorResult,
                                )
                        }
                    } catch (e: Exception) {
                        val errorResult = errorHandler.createErrorResult(e, "load birthday")
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorResult = errorResult,
                            )
                    }
                }
            }
        }

        /**
         * Updates the name field and clears related validation errors.
         */
        fun updateName(name: String) {
            _uiState.value =
                _uiState.value.copy(
                    name = name,
                    nameError = null,
                )
        }

        /**
         * Updates the birth date field and clears related validation errors.
         */
        fun updateBirthDate(birthDate: LocalDate) {
            _uiState.value =
                _uiState.value.copy(
                    birthDate = birthDate,
                    birthDateError = null,
                )
        }

        /**
         * Updates the notes field and clears related validation errors.
         */
        fun updateNotes(notes: String) {
            _uiState.value =
                _uiState.value.copy(
                    notes = notes,
                    notesError = null,
                )
        }

        /**
         * Toggles the notifications enabled setting.
         */
        fun toggleNotifications(enabled: Boolean) {
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }

        /**
         * Updates the advance notification days setting.
         */
        fun updateAdvanceNotificationDays(days: Int) {
            _uiState.value = _uiState.value.copy(advanceNotificationDays = days)
        }

        /**
         * Updates the notification hour setting.
         */
        fun updateNotificationHour(hour: Int) {
            _uiState.value = _uiState.value.copy(notificationHour = hour)
        }

        /**
         * Updates the notification minute setting.
         */
        fun updateNotificationMinute(minute: Int) {
            _uiState.value = _uiState.value.copy(notificationMinute = minute)
        }

        /**
         * Validates the current form state and returns true if valid.
         */
        private fun validateForm(): Boolean {
            val currentState = _uiState.value
            var isValid = true
            var nameError: String? = null
            var birthDateError: String? = null
            var notesError: String? = null

            // Validate name
            if (currentState.name.isBlank()) {
                nameError = "Name is required"
                isValid = false
            } else if (currentState.name.trim().length > 100) {
                nameError = "Name must be 100 characters or less"
                isValid = false
            }

            // Validate birth date
            if (currentState.birthDate == null) {
                birthDateError = "Birth date is required"
                isValid = false
            } else if (currentState.birthDate.isAfter(LocalDate.now())) {
                birthDateError = "Birth date cannot be in the future"
                isValid = false
            }

            // Validate notes
            if (currentState.notes.length > 500) {
                notesError = "Notes must be 500 characters or less"
                isValid = false
            }

            // Update state with validation errors
            _uiState.value =
                currentState.copy(
                    nameError = nameError,
                    birthDateError = birthDateError,
                    notesError = notesError,
                )

            return isValid
        }

        /**
         * Saves the birthday (either creates new or updates existing).
         */
        fun saveBirthday() {
            if (!validateForm()) {
                return
            }

            val currentState = _uiState.value
            val birthDate = currentState.birthDate ?: return

            viewModelScope.launch {
                _uiState.value = currentState.copy(isSaving = true, errorResult = null)

                try {
                    if (currentState.isEditMode && currentState.birthdayId != null) {
                        val result =
                            updateBirthdayUseCase.updateBirthday(
                                birthdayId = currentState.birthdayId,
                                name = currentState.name,
                                birthDate = birthDate,
                                notes = currentState.notes.takeIf { it.isNotBlank() },
                                notificationsEnabled = currentState.notificationsEnabled,
                                advanceNotificationDays = currentState.advanceNotificationDays,
                                notificationHour = currentState.notificationHour,
                                notificationMinute = currentState.notificationMinute,
                            )

                        when (result) {
                            is UpdateBirthdayResult.Success -> {
                                _uiState.value =
                                    currentState.copy(
                                        isSaving = false,
                                        saveSuccess = true,
                                    )
                            }
                            is UpdateBirthdayResult.ValidationError -> {
                                handleValidationErrors(result.errors)
                            }
                            is UpdateBirthdayResult.DatabaseError -> {
                                val errorResult =
                                    errorHandler.createErrorResult(
                                        result.exception,
                                        "update birthday",
                                    )
                                _uiState.value =
                                    currentState.copy(
                                        isSaving = false,
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
                                    currentState.copy(
                                        isSaving = false,
                                        errorResult = errorResult,
                                    )
                            }
                        }
                    } else {
                        val result =
                            addBirthdayUseCase.addBirthday(
                                name = currentState.name,
                                birthDate = birthDate,
                                notes = currentState.notes.takeIf { it.isNotBlank() },
                                notificationsEnabled = currentState.notificationsEnabled,
                                advanceNotificationDays = currentState.advanceNotificationDays,
                                notificationHour = currentState.notificationHour,
                                notificationMinute = currentState.notificationMinute,
                            )

                        when (result) {
                            is AddBirthdayResult.Success -> {
                                _uiState.value =
                                    currentState.copy(
                                        isSaving = false,
                                        saveSuccess = true,
                                    )
                            }
                            is AddBirthdayResult.ValidationError -> {
                                handleValidationErrors(result.errors)
                            }
                            is AddBirthdayResult.DatabaseError -> {
                                val errorResult =
                                    errorHandler.createErrorResult(
                                        result.exception,
                                        "add birthday",
                                    )
                                _uiState.value =
                                    currentState.copy(
                                        isSaving = false,
                                        errorResult = errorResult,
                                    )
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorResult = errorHandler.createErrorResult(e, "save birthday")
                    _uiState.value =
                        currentState.copy(
                            isSaving = false,
                            errorResult = errorResult,
                        )
                }
            }
        }

        /**
         * Handles validation errors from use cases by mapping them to form field errors.
         */
        private fun handleValidationErrors(errors: List<String>) {
            val currentState = _uiState.value
            var nameError: String? = null
            var birthDateError: String? = null
            var notesError: String? = null
            var generalError: String? = null

            errors.forEach { error ->
                when {
                    error.contains("Name", ignoreCase = true) -> nameError = error
                    error.contains("Birth date", ignoreCase = true) ||
                        error.contains("date", ignoreCase = true) -> birthDateError = error
                    error.contains("Notes", ignoreCase = true) -> notesError = error
                    else -> generalError = error
                }
            }

            val errorResult =
                if (generalError != null) {
                    errorHandler.createErrorResult(
                        IllegalArgumentException(generalError),
                        "validate birthday",
                    )
                } else {
                    null
                }

            _uiState.value =
                currentState.copy(
                    isSaving = false,
                    nameError = nameError,
                    birthDateError = birthDateError,
                    notesError = notesError,
                    errorResult = errorResult,
                )
        }

        /**
         * Clears the error message.
         */
        fun clearError() {
            _uiState.value = _uiState.value.copy(errorResult = null)
        }

        /**
         * Resets the save success state.
         */
        fun resetSaveSuccess() {
            _uiState.value = _uiState.value.copy(saveSuccess = false)
        }
    }

/**
 * UI state for the Add/Edit Birthday screen.
 */
data class AddEditBirthdayUiState(
    val isEditMode: Boolean = false,
    val birthdayId: Long? = null,
    val name: String = "",
    val birthDate: LocalDate? = null,
    val notes: String = "",
    val notificationsEnabled: Boolean = true,
    val advanceNotificationDays: Int = 0,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorResult: ErrorResult? = null,
    val nameError: String? = null,
    val birthDateError: String? = null,
    val notesError: String? = null,
) {
    /**
     * Returns true if the form has any validation errors.
     */
    val hasErrors: Boolean
        get() = nameError != null || birthDateError != null || notesError != null

    /**
     * Returns true if the form is ready to be saved (has required fields and no errors).
     */
    val canSave: Boolean
        get() = name.isNotBlank() && birthDate != null && !hasErrors && !isSaving

    val errorMessage: String? get() = errorResult?.message
    val hasError: Boolean get() = errorResult != null
}
