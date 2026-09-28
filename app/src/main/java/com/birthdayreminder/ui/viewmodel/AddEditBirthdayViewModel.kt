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
import com.birthdayreminder.ui.card.CardTone
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
                    _uiState.value =
                        AddEditBirthdayUiState(
                            isEditMode = false,
                            notificationHour = h,
                            notificationMinute = m,
                            notificationTime = LocalTime.of(h, m),
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
                                    // Kept verbatim so an untagged or
                                    // legacy value still round-trips; the chip row
                                    // resolves it for display, and a Save with no
                                    // change must not quietly retag the person.
                                    relationship = birthday.relationship.orEmpty(),
                                    cardTone = CardTone.fromName(birthday.cardTone),
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

        /**
         * Turns reminders for this person on or off.
         *
         * @param enabled whether a reminder should be delivered
         */
        fun updateNotificationsEnabled(enabled: Boolean) {
            _uiState.update { it.copy(notificationsEnabled = enabled) }
        }

        /**
         * Chooses the card's message tone.
         *
         * @param tone the tone to write the message in
         */
        fun updateCardTone(tone: CardTone) {
            _uiState.update { it.copy(cardTone = tone) }
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

        /**
         * Advances the wizard, validating the step being left first.
         *
         * Previously this just incremented, so a user who typed an invalid name
         * either met a disabled Continue with no explanation, or discovered the
         * problem on step 3 when the use case rejected the save. Validation now
         * runs per step and lands inline on the field that caused it.
         *
         * Form data is never cleared here, so backing out of a step preserves
         * everything already entered.
         */
        fun nextStep() {
            val current = _uiState.value
            val stepError = validateStep(current.step, current)
            if (stepError != null) {
                _uiState.update { it.withFieldError(current.step, stepError) }
                return
            }
            _uiState.update { it.copy(step = it.step + 1, nameError = null, birthDateError = null, notesError = null) }
        }

        fun previousStep() {
            _uiState.update { if (it.step > 1) it.copy(step = it.step - 1) else it }
        }

        /**
         * Why the wizard cannot leave the current step, or null if it can.
         *
         * Surfaced next to the Continue button so a disabled control always
         * comes with a reason.
         */
        fun currentStepError(): String? = _uiState.value.currentStepError(birthdayValidator)

        /**
         * Validates one step of the wizard.
         *
         * @param step the 1-based step number
         * @param state the current form state
         * @return an error to show, or null when the step may be left
         */
        private fun validateStep(
            step: Int,
            state: AddEditBirthdayUiState,
        ): String? =
            when (step) {
                1 -> birthdayValidator.validateName(state.name)
                2 -> birthdayValidator.validateBirthDate(state.birthDate)
                3 -> birthdayValidator.validateNotes(state.notes)
                else -> null
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
                                notificationTime = currentState.notificationTime,
                                cardTone = currentState.cardTone.name,
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
                                val errorResult =
                                    errorHandler.createErrorResult(
                                        IllegalStateException(result.message),
                                        "update birthday",
                                    )
                                _uiState.update { it.copy(isSaving = false, errorResult = errorResult) }
                            }
                            is UpdateBirthdayResult.ExactAlarmPermissionNotGranted -> {
                                val errorResult =
                                    errorHandler.createErrorResult(
                                        SecurityException("Exact alarm permission is required"),
                                        "schedule notification",
                                    )
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
                                notificationTime = currentState.notificationTime,
                                cardTone = currentState.cardTone.name,
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
                                val errorResult =
                                    errorHandler.createErrorResult(
                                        SecurityException("Exact alarm permission is required"),
                                        "schedule notification",
                                    )
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
    /**
     * Empty until the user picks a chip. The wizard used to default this to
     * "Friend", which pre-selected a relationship nobody had chosen and
     * gave every person saved without touching the chips a relationship.
     */
    val relationship: String = "",
    val isPinned: Boolean = false,
    val notificationOffsets: List<Int> = listOf(0),
    val notificationTime: LocalTime? = null,
    /**
     * The tone the card's message should be written in. Chosen on the wizard's
     * last step and persisted, so the card does not re-ask on every open.
     */
    val cardTone: CardTone = CardTone.DEFAULT,
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

    /**
     * Routes a step's validation failure to the field that caused it.
     *
     * Step 1 owns the name, step 2 the birth date, step 3 the notes, so the
     * error lands under the right input instead of in a generic banner.
     *
     * @param step the 1-based step that failed
     * @param message the message from `BirthdayValidator`
     */
    fun withFieldError(
        step: Int,
        message: String,
    ): AddEditBirthdayUiState =
        when (step) {
            1 -> copy(nameError = message)
            2 -> copy(birthDateError = message)
            3 -> copy(notesError = message)
            else -> this
        }

    /**
     * The validation error for the current step, if any.
     *
     * Used by the wizard's Continue button so it can explain itself instead of
     * sitting disabled with no reason.
     */
    fun currentStepError(validator: BirthdayValidator): String? =
        when (step) {
            1 -> validator.validateName(name)
            2 -> validator.validateBirthDate(birthDate)
            3 -> validator.validateNotes(notes)
            else -> null
        }
}
