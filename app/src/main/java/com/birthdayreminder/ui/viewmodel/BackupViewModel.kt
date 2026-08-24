package com.birthdayreminder.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.backup.ConflictStrategy
import com.birthdayreminder.data.backup.ContentResolverBackupDataSource
import com.birthdayreminder.domain.usecase.ExportBirthdaysResult
import com.birthdayreminder.domain.usecase.ExportBirthdaysUseCase
import com.birthdayreminder.domain.usecase.ImportBirthdaysResult
import com.birthdayreminder.domain.usecase.ImportBirthdaysUseCase
import com.birthdayreminder.domain.usecase.ValidateBackupFileResult
import com.birthdayreminder.domain.usecase.ValidateBackupFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Typed backup error surfaced to the UI layer.
 */
sealed class BackupError {
    /**
     * Storage access failed, e.g. the file could not be opened.
     */
    data class Storage(val detail: String? = null) : BackupError()

    /**
     * Applying changes to the database failed.
     */
    data class Database(val detail: String? = null) : BackupError()

    /**
     * The selected file is not a valid backup file.
     */
    data class InvalidFile(val reason: String? = null) : BackupError()

    /**
     * The file was created by a newer app version with an unsupported format.
     */
    data class UnsupportedVersion(val version: Int) : BackupError()
}

/**
 * Summary metadata of a validated backup file.
 */
data class BackupFileInfo(
    val version: Int,
    val exportDate: String,
    val birthdayCount: Int,
)

/**
 * Outcome of a successful import.
 */
data class ImportSummary(
    val importedCount: Int,
    val skippedCount: Int,
)

/**
 * State of the export operation.
 */
data class BackupExportState(
    val inProgress: Boolean = false,
    val exportedCount: Int? = null,
    val error: BackupError? = null,
)

/**
 * State of the import operation.
 */
data class BackupImportState(
    val inProgress: Boolean = false,
    val summary: ImportSummary? = null,
    val error: BackupError? = null,
)

/**
 * State of the validation operation.
 */
data class BackupValidationState(
    val inProgress: Boolean = false,
    val fileInfo: BackupFileInfo? = null,
    val invalidReason: String? = null,
    val error: BackupError? = null,
)

/**
 * UI state for the backup screen. Each operation has isolated state so updates to one operation
 * never disturb another.
 */
data class BackupUiState(
    val export: BackupExportState = BackupExportState(),
    val import: BackupImportState = BackupImportState(),
    val validation: BackupValidationState = BackupValidationState(),
) {
    /**
     * True while any backup operation is running; used to prevent concurrent operations.
     */
    val isBusy: Boolean
        get() = export.inProgress || import.inProgress || validation.inProgress
}

/**
 * ViewModel for the backup screen.
 * Manages UI state for backup and restore operations and guards against concurrent operations.
 */
@HiltViewModel
class BackupViewModel
    @Inject
    constructor(
        private val exportBirthdaysUseCase: ExportBirthdaysUseCase,
        private val importBirthdaysUseCase: ImportBirthdaysUseCase,
        private val validateBackupFileUseCase: ValidateBackupFileUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BackupUiState())
        val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

        /**
         * Exports birthdays to a backup file.
         *
         * @param uri The URI where the backup file should be created
         */
        fun exportBirthdays(uri: Uri) {
            launchIfIdle {
                _uiState.update {
                    it.copy(export = BackupExportState(inProgress = true))
                }

                when (val result = exportBirthdaysUseCase.exportBirthdays(uri)) {
                    is ExportBirthdaysResult.Success -> {
                        _uiState.update { state ->
                            state.copy(export = BackupExportState(exportedCount = result.count))
                        }
                    }
                    is ExportBirthdaysResult.StorageError -> {
                        _uiState.update { state ->
                            state.copy(
                                export =
                                    BackupExportState(
                                        error = BackupError.Storage(result.exception.message),
                                    ),
                            )
                        }
                    }
                }
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
            launchIfIdle {
                _uiState.update {
                    it.copy(import = BackupImportState(inProgress = true))
                }

                when (val result = importBirthdaysUseCase.importBirthdays(uri, conflictStrategy)) {
                    is ImportBirthdaysResult.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                import =
                                    BackupImportState(
                                        summary =
                                            ImportSummary(
                                                importedCount = result.importedCount,
                                                skippedCount = result.skippedCount,
                                            ),
                                    ),
                            )
                        }
                    }
                    is ImportBirthdaysResult.InvalidFile -> {
                        _uiState.update { state ->
                            state.copy(
                                import =
                                    BackupImportState(
                                        error = BackupError.InvalidFile(result.reason),
                                    ),
                            )
                        }
                    }
                    is ImportBirthdaysResult.UnsupportedVersion -> {
                        _uiState.update { state ->
                            state.copy(
                                import =
                                    BackupImportState(
                                        error = BackupError.UnsupportedVersion(result.version),
                                    ),
                            )
                        }
                    }
                    is ImportBirthdaysResult.StorageError -> {
                        _uiState.update { state ->
                            state.copy(
                                import =
                                    BackupImportState(
                                        error = BackupError.Storage(result.exception.message),
                                    ),
                            )
                        }
                    }
                    is ImportBirthdaysResult.DatabaseError -> {
                        _uiState.update { state ->
                            state.copy(
                                import =
                                    BackupImportState(
                                        error = BackupError.Database(result.exception.message),
                                    ),
                            )
                        }
                    }
                }
            }
        }

        /**
         * Validates a backup file before import.
         *
         * @param uri The URI of the backup file to validate
         */
        fun validateBackupFile(uri: Uri) {
            launchIfIdle {
                _uiState.update {
                    it.copy(validation = BackupValidationState(inProgress = true))
                }

                when (val result = validateBackupFileUseCase.validateBackupFile(uri)) {
                    is ValidateBackupFileResult.Valid -> {
                        _uiState.update { state ->
                            state.copy(
                                validation =
                                    BackupValidationState(
                                        fileInfo =
                                            BackupFileInfo(
                                                version = result.version,
                                                exportDate = result.exportDate,
                                                birthdayCount = result.birthdayCount,
                                            ),
                                    ),
                            )
                        }
                    }
                    is ValidateBackupFileResult.Invalid -> {
                        _uiState.update { state ->
                            state.copy(
                                validation =
                                    BackupValidationState(invalidReason = result.reason),
                            )
                        }
                    }
                    is ValidateBackupFileResult.StorageError -> {
                        _uiState.update { state ->
                            state.copy(
                                validation =
                                    BackupValidationState(
                                        error = BackupError.Storage(result.exception.message),
                                    ),
                            )
                        }
                    }
                }
            }
        }

        /**
         * Generates a default backup file name.
         *
         * @return The default backup file name
         */
        fun generateDefaultBackupFileName(): String {
            return ContentResolverBackupDataSource.generateDefaultBackupFileName()
        }

        /**
         * Clears the export success message.
         */
        fun clearExportSuccess() {
            _uiState.update { it.copy(export = it.export.copy(exportedCount = null)) }
        }

        /**
         * Clears the import success message.
         */
        fun clearImportSuccess() {
            _uiState.update { it.copy(import = it.import.copy(summary = null)) }
        }

        /**
         * Clears the validation result.
         */
        fun clearValidationResult() {
            _uiState.update { it.copy(validation = BackupValidationState()) }
        }

        /**
         * Clears all error states.
         */
        fun clearErrors() {
            _uiState.update {
                it.copy(
                    export = it.export.copy(error = null),
                    import = it.import.copy(error = null),
                    validation = it.validation.copy(error = null),
                )
            }
        }

        /**
         * Launches a coroutine only when no backup operation is currently running, preventing
         * concurrent operations from interleaving state updates.
         */
        private fun launchIfIdle(block: suspend () -> Unit) {
            if (_uiState.value.isBusy) return

            viewModelScope.launch {
                try {
                    block()
                } finally {
                    // Defensive reset so a crash path can never leave a spinner running forever.
                    _uiState.update {
                        it.copy(
                            export = it.export.copy(inProgress = false),
                            import = it.import.copy(inProgress = false),
                            validation = it.validation.copy(inProgress = false),
                        )
                    }
                }
            }
        }
    }
