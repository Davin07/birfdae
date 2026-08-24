package com.birthdayreminder.domain.usecase

import android.net.Uri
import com.birthdayreminder.data.backup.BACKUP_FORMAT_VERSION
import com.birthdayreminder.data.backup.BackupDataSource
import com.birthdayreminder.data.backup.BackupFileDto
import com.birthdayreminder.data.backup.toBackupDto
import com.birthdayreminder.data.repository.BirthdayRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of exporting birthdays to a backup file.
 */
sealed class ExportBirthdaysResult {
    /**
     * Export completed successfully.
     *
     * @param count Number of birthdays written to the backup file
     */
    data class Success(val count: Int) : ExportBirthdaysResult()

    /**
     * Writing to the destination failed, e.g. storage unavailable or file not writable.
     */
    data class StorageError(val exception: Throwable) : ExportBirthdaysResult()
}

/**
 * Use case for exporting all birthdays to a backup file.
 * Reads a consistent snapshot of birthdays and delegates atomic file writing to [BackupDataSource].
 */
@Singleton
class ExportBirthdaysUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val backupDataSource: BackupDataSource,
    ) {
        /**
         * Exports all birthdays to a JSON backup file at the specified URI.
         *
         * @param uri The URI where the backup file should be created
         * @return Result indicating success with the exported count, or a storage error
         */
        suspend fun exportBirthdays(uri: Uri): ExportBirthdaysResult {
            return try {
                val birthdays = birthdayRepository.getAllBirthdaysSnapshot()

                val backupFile =
                    BackupFileDto(
                        version = BACKUP_FORMAT_VERSION,
                        exportDate =
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        birthdays = birthdays.map { it.toBackupDto() },
                    )

                backupDataSource.writeBackup(uri, backupFile).fold(
                    onSuccess = { ExportBirthdaysResult.Success(birthdays.size) },
                    onFailure = { ExportBirthdaysResult.StorageError(it) },
                )
            } catch (e: Exception) {
                ExportBirthdaysResult.StorageError(e)
            }
        }

        suspend operator fun invoke(uri: Uri): ExportBirthdaysResult {
            return exportBirthdays(uri)
        }
    }
