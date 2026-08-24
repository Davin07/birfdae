package com.birthdayreminder.domain.usecase

import android.net.Uri
import com.birthdayreminder.data.backup.BackupDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of validating a backup file.
 */
sealed class ValidateBackupFileResult {
    /**
     * The file is a structurally valid backup.
     *
     * @param version Backup format version found in the file
     * @param exportDate ISO-8601 timestamp of when the backup was created, if present
     * @param birthdayCount Number of birthdays contained in the backup
     */
    data class Valid(
        val version: Int,
        val exportDate: String,
        val birthdayCount: Int,
    ) : ValidateBackupFileResult()

    /**
     * The file is readable but not a valid backup file.
     */
    data class Invalid(val reason: String? = null) : ValidateBackupFileResult()

    /**
     * The file could not be read from storage at all.
     */
    data class StorageError(val exception: Throwable) : ValidateBackupFileResult()
}

/**
 * Use case for validating a backup file before import.
 *
 * Distinguishes genuine storage failures from invalid content so the UI can show an accurate
 * message, and surfaces backup metadata (version, export date, count) for user confirmation.
 */
@Singleton
class ValidateBackupFileUseCase
    @Inject
    constructor(
        private val backupDataSource: BackupDataSource,
    ) {
        /**
         * Validates a backup file by parsing its structure and extracting summary metadata.
         *
         * @param uri The URI of the backup file to validate
         * @return Result describing whether the file is valid, invalid, or unreadable
         */
        suspend fun validateBackupFile(uri: Uri): ValidateBackupFileResult {
            return backupDataSource.readBackup(uri).fold(
                onSuccess = { backupFile ->
                    if (backupFile.exportDate.isBlank() && backupFile.birthdays.isEmpty()) {
                        ValidateBackupFileResult.Invalid("Backup file contains no birthdays")
                    } else {
                        ValidateBackupFileResult.Valid(
                            version = backupFile.version,
                            exportDate = backupFile.exportDate,
                            birthdayCount = backupFile.birthdays.size,
                        )
                    }
                },
                onFailure = { e ->
                    if (isStorageFailure(e)) {
                        ValidateBackupFileResult.StorageError(e)
                    } else {
                        ValidateBackupFileResult.Invalid(e.message)
                    }
                },
            )
        }

        suspend operator fun invoke(uri: Uri): ValidateBackupFileResult {
            return validateBackupFile(uri)
        }

        /**
         * Storage access failures (missing streams) are infrastructure problems, whereas
         * serialization failures indicate invalid backup content.
         */
        private fun isStorageFailure(exception: Throwable): Boolean {
            return exception is java.io.IOException ||
                exception is IllegalStateException ||
                exception is SecurityException
        }
    }
