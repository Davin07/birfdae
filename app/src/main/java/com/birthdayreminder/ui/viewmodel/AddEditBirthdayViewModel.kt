package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.data.settings.SettingsRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.error.ErrorResult
import com.birthdayreminder.domain.usecase.AddBirthdayResult
import com.birthdayreminder.domain.usecase.AddBirthdayUseCase
import com.birthdayreminder.domain.usecase.UpdateBirthdayResult
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import com.birthdayreminder.domain.validation.BirthdayValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
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
        private val settingsRepository: SettingsRepository,
        private val errorHandler: ErrorHandler,
        private val birthdayValidator: BirthdayValidator,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddEditBirthdayUiState())
        val uiState: StateFlow<AddEditBirthdayUiState> = _uiState.asStateFlow()

        /**
         * Initializes the form for editing an existing birthday.
         * If birthdayId is null, the form is initialized for adding a new birthday.
         */
        fun initializeForm(birthdayId: Long?) {
            if (birthdayId == null) {
                viewModelScope.launch {
                    val (h, m) = settingsRepository.defaultNotificationTime.first()
                    _uiState.value = AddEditBirthdayUiState(
                        isEditMode = false,
                        notificationHour = h,
                        notificationMinute = m,
                        notificationTime = LocalTime.of(h, m)
                    )
                }
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
                                    // New fields
                                    imageUri = birthday.imageUri,
                                    relationship = birthday.relationship ?: "Friend",
                                    isPinned = birthday.isPinned,
                                    notificationOffsets = birthday.notificationOffsets.ifEmpty { listOf(0) },
                                    notificationTime = birthday.notificationTime,
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

        fun updateName(name: String) {
            _uiState.update { it.copy(name = name, nameError = null) }
        }

        fun updateBirthDate(birthDate: LocalDate?) {
            _uiState.update { it.copy(birthDate = birthDate, birthDateError = null) }
        }

        fun updateNotes(notes: String) {
            _uiState.update { it.copy(notes = notes, notesError = null) }
        }

        fun toggleNotifications(enabled: Boolean) {
            _uiState.update { it.copy(notificationsEnabled = enabled) }
        }

        fun updateAdvanceNotificationDays(days: Int) {
            _uiState.update { it.copy(advanceNotificationDays = days) }
        }

        fun updateNotificationHour(hour: Int) {
            _uiState.update { it.copy(notificationHour = hour) }
        }

        fun updateNotificationMinute(minute: Int) {
            _uiState.update { it.copy(notificationMinute = minute) }
        }

        // New Update Functions
        fun updateImageUri(uri: String?) {
            _uiState.update { it.copy(imageUri = uri) }
        }

        fun updateRelationship(relationship: String) {
            _uiState.update { it.copy(relationship = relationship) }
        }

        fun updateIsPinned(isPinned: Boolean) {
            _uiState.update { it.copy(isPinned = isPinned) }
        }

        fun updateNotificationOffsets(offsets: List<Int>) {
            _uiState.update { it.copy(notificationOffsets = offsets) }
        }

        fun updateNotificationTime(time: LocalTime?) {
            _uiState.update { it.copy(notificationTime = time) }
        }

        fun nextStep() {
            _uiState.update { it.copy(step = it.step + 1) }
        }

        fun previousStep() {
            _uiState.update { if (it.step > 1) it.copy(step = it.step - 1) else it }
        }

        fun saveBirthday() {
            val currentState = _uiState.value
            val birthDate = currentState.birthDate ?: return

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorResult = null) }

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
                                imageUri = currentState.imageUri,
                                relationship = currentState.relationship,
                                isPinned = currentState.isPinned,
                                notificationOffsets = currentState.notificationOffsets,
                                notificationTime = currentState.notificationTime
                            )

                        when (result) {
                            is UpdateBirthdayResult.Success -> {
                                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                            }
                            is UpdateBirthdayResult.ValidationError -> {
                                handleValidationErrors(result.errors)
                            }
                            is UpdateBirthdayResult.DatabaseError -> {
                                val errorResult = errorHandler.createErrorResult(result.exception, "update birthday")
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                            }
                            is UpdateBirthdayResult.NotFound -> {
                                val errorResult = errorHandler.createErrorResult(IllegalStateException(result.message), "update birthday")
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                            }
                            is UpdateBirthdayResult.ExactAlarmPermissionNotGranted -> {
                                val errorResult = errorHandler.createErrorResult(SecurityException("Exact alarm permission is required"), "schedule notification")
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
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
                                imageUri = currentState.imageUri,
                                relationship = currentState.relationship,
                                isPinned = currentState.isPinned,
                                notificationOffsets = currentState.notificationOffsets,
                                notificationTime = currentState.notificationTime
                            )

                        when (result) {
                            is AddBirthdayResult.Success -> {
                                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                            }
                            is AddBirthdayResult.ValidationError -> {
                                handleValidationErrors(result.errors)
                            }
                            is AddBirthdayResult.DatabaseError -> {
                                val errorResult = errorHandler.createErrorResult(result.exception, "add birthday")
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                            }
                            is AddBirthdayResult.ExactAlarmPermissionNotGranted -> {
                                val errorResult = errorHandler.createErrorResult(SecurityException("Exact alarm permission is required"), "schedule notification")
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorResult = errorHandler.createErrorResult(e, "save birthday")
                    _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                }
            }
        }

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

        fun clearError() {
            _uiState.update { it.copy(errorResult = null) }
        }

        fun resetSaveSuccess() {
            _uiState.update { it.copy(saveSuccess = false) }
        }
    }

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
    // New Fields
    val imageUri: String? = null,
    val relationship: String = "Friend",
    val isPinned: Boolean = false,
    val notificationOffsets: List<Int> = listOf(0),
    val notificationTime: LocalTime? = null,
    val step: Int = 1,
    // State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorResult: ErrorResult? = null,
    val nameError: String? = null,
    val birthDateError: String? = null,
    val notesError: String? = null,
) {
    val hasErrors: Boolean
        get() = nameError != null || birthDateError != null || notesError != null

    val canSave: Boolean
        get() = name.isNotBlank() && birthDate != null && !hasErrors && !isSaving

    val errorMessage: String? get() = errorResult?.message
    val hasError: Boolean get() = errorResult != null
}
