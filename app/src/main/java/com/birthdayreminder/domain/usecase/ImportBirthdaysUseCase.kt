package com.birthdayreminder.domain.usecase

import android.net.Uri
import com.birthdayreminder.data.backup.BACKUP_FORMAT_VERSION
import com.birthdayreminder.data.backup.BackupDataSource
import com.birthdayreminder.data.backup.BirthdayBackupDto
import com.birthdayreminder.data.backup.ConflictStrategy
import com.birthdayreminder.data.backup.naturalKey
import com.birthdayreminder.data.backup.toEntity
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of importing birthdays from a backup file.
 */
sealed class ImportBirthdaysResult {
    /**
     * Import completed successfully and atomically.
     *
     * @param importedCount Number of birthdays inserted or updated
     * @param skippedCount Number of backup entries skipped due to conflicts
     */
    data class Success(
        val importedCount: Int,
        val skippedCount: Int,
    ) : ImportBirthdaysResult()

    /**
     * The selected file is not a valid or readable backup file.
     */
    data class InvalidFile(val reason: String? = null) : ImportBirthdaysResult()

    /**
     * The backup file was written by a newer, unsupported format version.
     */
    data class UnsupportedVersion(val version: Int) : ImportBirthdaysResult()

    /**
     * Reading the file from storage failed.
     */
    data class StorageError(val exception: Throwable) : ImportBirthdaysResult()

    /**
     * Applying the import to the database failed; no partial changes were committed.
     */
    data class DatabaseError(val exception: Throwable) : ImportBirthdaysResult()
}

/**
 * Use case for importing birthdays from a backup file.
 *
 * Conflict detection uses a natural key (normalized name + birth date) instead of auto-generated
 * database ids, so restores behave correctly across devices and fresh installs. The entire import
 * runs inside a single database transaction: either every change commits or none do.
 */
@Singleton
class ImportBirthdaysUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val backupDataSource: BackupDataSource,
        private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
        private val cancelNotificationUseCase: CancelNotificationUseCase,
    ) {
        /**
         * Imports birthdays from a JSON backup file at the specified URI.
         *
         * @param uri The URI of the backup file to import
         * @param conflictStrategy How to handle birthdays that already exist locally
         * @return Result describing the outcome of the import
         */
        suspend fun importBirthdays(
            uri: Uri,
            conflictStrategy: ConflictStrategy,
        ): ImportBirthdaysResult {
            val backupFile =
                backupDataSource.readBackup(uri).getOrElse { e ->
                    return ImportBirthdaysResult.StorageError(e)
                }

            if (backupFile.version > BACKUP_FORMAT_VERSION) {
                return ImportBirthdaysResult.UnsupportedVersion(backupFile.version)
            }

            return try {
                // Alarms are rescheduled only after the transaction commits so notifications are
                // never left pointing at rolled-back data.
                var skippedCount = 0
                val deletedIds = mutableListOf<Long>()
                val upserted = mutableListOf<Birthday>()

                birthdayRepository.runInTransaction {
                    // Room transactions execute sequentially on a single dispatcher, so mutating
                    // the collections captured above is safe here.
                    skippedCount = 0
                    deletedIds.clear()
                    upserted.clear()

                    val localByKey =
                        birthdayRepository.getAllBirthdaysSnapshot().associateBy { it.naturalKey() }

                    for (entry in backupFile.birthdays) {
                        val existing = localByKey[entry.naturalKey()]
                        when (conflictStrategy) {
                            ConflictStrategy.SKIP -> {
                                if (existing == null) {
                                    insertAndTrack(entry, upserted)
                                } else {
                                    skippedCount++
                                }
                            }
                            ConflictStrategy.OVERWRITE -> {
                                existing?.let {
                                    birthdayRepository.deleteBirthdayById(it.id)
                                    deletedIds.add(it.id)
                                }
                                insertAndTrack(entry, upserted)
                            }
                            ConflictStrategy.MERGE -> {
                                if (existing == null) {
                                    insertAndTrack(entry, upserted)
                                } else {
                                    val merged = mergeInto(existing, entry)
                                    birthdayRepository.updateBirthday(merged)
                                    upserted.add(merged)
                                }
                            }
                        }
                    }
                }

                for (id in deletedIds) {
                    cancelNotificationUseCase.cancelNotification(id)
                }
                for (birthday in upserted) {
                    if (birthday.notificationsEnabled) {
                        scheduleNotificationUseCase.scheduleNotification(birthday)
                    }
                }

                ImportBirthdaysResult.Success(
                    importedCount = backupFile.birthdays.size - skippedCount,
                    skippedCount = skippedCount,
                )
            } catch (e: Exception) {
                ImportBirthdaysResult.DatabaseError(e)
            }
        }

        suspend operator fun invoke(
            uri: Uri,
            conflictStrategy: ConflictStrategy,
        ): ImportBirthdaysResult {
            return importBirthdays(uri, conflictStrategy)
        }

        /**
         * Inserts a backed-up entry as a new record and tracks the persisted entity for
         * post-commit notification scheduling.
         */
        private suspend fun insertAndTrack(
            entry: BirthdayBackupDto,
            upserted: MutableList<Birthday>,
        ) {
            val newId = birthdayRepository.addBirthday(entry.toEntity())
            upserted.add(entry.toEntity().copy(id = newId))
        }

        /**
         * Applies imported values onto an existing record while keeping its local identity.
         * Imported data wins on conflict.
         */
        private fun mergeInto(
            existing: Birthday,
            entry: BirthdayBackupDto,
        ): Birthday {
            val imported = entry.toEntity()
            return existing.copy(
                name = imported.name,
                birthDate = imported.birthDate,
                notes = imported.notes,
                notificationsEnabled = imported.notificationsEnabled,
                advanceNotificationDays = imported.advanceNotificationDays,
                notificationHour = imported.notificationHour,
                notificationMinute = imported.notificationMinute,
                imageUri = imported.imageUri,
                relationship = imported.relationship,
                isPinned = imported.isPinned,
                notificationOffsets = imported.notificationOffsets,
                notificationTime = imported.notificationTime,
            )
        }
    }
