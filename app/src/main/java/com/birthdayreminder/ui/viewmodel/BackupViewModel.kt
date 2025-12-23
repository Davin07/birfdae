package com.birthdayreminder.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.backup.BackupManager
import com.birthdayreminder.data.backup.ConflictStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the backup screen.
 * Manages UI state for backup and restore operations.
 */
@HiltViewModel
class BackupViewModel
    @Inject
    constructor(
        private val backupManager: BackupManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BackupUiState())
        val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

        /**
         * Exports birthdays to a backup file.
         *
         * @param uri The URI where the backup file should be created
         */
        fun exportBirthdays(uri: Uri) {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        isExporting = true,
                        exportError = null,
                    )

                val result = backupManager.exportBirthdays(uri)

                _uiState.value =
                    _uiState.value.copy(
                        isExporting = false,
                        exportSuccess = result.isSuccess,
                        exportError = result.exceptionOrNull()?.message,
                    )
            }
        }

        /**
         * Imports birthdays from a backup file.
         *
         * @param uri The URI of the backup file to import
         * @param conflictStrategy How to handle conflicts with existing birthdays
         */
        fun importBirthdays(
            uri: Uri,
            conflictStrategy: ConflictStrategy,
        ) {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        isImporting = true,
                        importError = null,
                    )

                val result = backupManager.importBirthdays(uri, conflictStrategy)

                _uiState.value =
                    _uiState.value.copy(
                        isImporting = false,
                        importSuccess = result.isSuccess,
                        importedCount = result.getOrNull(),
                        importError = result.exceptionOrNull()?.message,
                    )
            }
        }

        /**
         * Validates a backup file.
         *
         * @param uri The URI of the backup file to validate
         */
        fun validateBackupFile(uri: Uri) {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        isValidating = true,
                        validationError = null,
                    )

                val result = backupManager.validateBackupFile(uri)

                _uiState.value =
                    _uiState.value.copy(
                        isValidating = false,
                        validationSuccess = result.isSuccess,
                        isFileValid = result.getOrNull(),
                        validationError = result.exceptionOrNull()?.message,
                    )
            }
        }

        /**
         * Generates a default backup file name.
         *
         * @return The default backup file name
         */
        fun generateDefaultBackupFileName(): String {
            return backupManager.generateDefaultBackupFileName()
        }

        /**
         * Clears export success state.
         */
        fun clearExportSuccess() {
            _uiState.value = _uiState.value.copy(exportSuccess = false)
        }

        /**
         * Clears import success state.
         */
        fun clearImportSuccess() {
            _uiState.value = _uiState.value.copy(importSuccess = false)
        }

        /**
         * Clears validation success state.
         */
        fun clearValidationSuccess() {
            _uiState.value = _uiState.value.copy(validationSuccess = false)
        }

        /**
         * Clears all error states.
         */
        fun clearErrors() {
            _uiState.value =
                _uiState.value.copy(
                    exportError = null,
                    importError = null,
                    validationError = null,
                )
        }
    }

/**
 * UI state for the backup screen.
 */
data class BackupUiState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val importedCount: Int? = null,
    val importError: String? = null,
    val isValidating: Boolean = false,
    val validationSuccess: Boolean = false,
    val isFileValid: Boolean? = null,
    val validationError: String? = null,
)
