package com.birthdayreminder.data.backup

import android.content.Context
import android.net.Uri
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.notification.AlarmScheduler
import com.birthdayreminder.data.repository.BirthdayRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling backup and restore operations for birthday data.
 */
@Singleton
class BackupManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val birthdayRepository: BirthdayRepository,
        private val alarmScheduler: AlarmScheduler,
    ) {
        private val json =
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                serializersModule =
                    SerializersModule {
                        contextual(LocalDate::class, LocalDateSerializer)
                        contextual(LocalDateTime::class, LocalDateTimeSerializer)
                    }
            }

        companion object {
            private const val BACKUP_FILE_PREFIX = "birthday_reminder_backup_"
            private const val BACKUP_FILE_EXTENSION = ".json"
        }

        /**
         * Data class representing the backup file structure.
         */
        @Serializable
        data class BackupData(
            val version: Int = 1,
            val exportDate: String,
            val birthdays: List<Birthday>,
        )

        /**
         * Exports all birthdays to a JSON file at the specified URI.
         *
         * @param uri The URI where the backup file should be created
         * @return Result indicating success or failure
         */
        suspend fun exportBirthdays(uri: Uri): Result<Unit> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    // Get all birthdays from repository
                    val birthdays = birthdayRepository.getAllBirthdays().first()

                    // Create backup data
                    val exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val backupData =
                        BackupData(
                            exportDate = exportDate,
                            birthdays = birthdays,
                        )

                    // Write to file
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json.encodeToString(backupData))
                        }
                    } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

                    Result.success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error exporting birthdays")
                    Result.failure(e)
                }
            }

        /**
         * Imports birthdays from a JSON file at the specified URI.
         *
         * @param uri The URI of the backup file to import
         * @param conflictStrategy How to handle conflicts with existing birthdays
         * @return Result containing the number of birthdays imported
         */
        suspend fun importBirthdays(
            uri: Uri,
            conflictStrategy: ConflictStrategy,
        ): Result<Int> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    // Read from file
                    val jsonString =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                reader.readText()
                            }
                        } ?: return@withContext Result.failure(Exception("Failed to open input stream"))

                    // Parse JSON
                    val backupData = json.decodeFromString<BackupData>(jsonString)

                    // Handle conflicts based on strategy
                    var importedCount = 0
                    for (birthday in backupData.birthdays) {
                        when (conflictStrategy) {
                            ConflictStrategy.SKIP -> {
                                // Check if birthday already exists
                                val existing = birthdayRepository.getBirthdayById(birthday.id)
                                if (existing == null) {
                                    // Add new birthday with a new ID
                                    val newBirthday = birthday.copy(id = 0)
                                    val newId = birthdayRepository.addBirthday(newBirthday)
                                    
                                    if (newBirthday.notificationsEnabled) {
                                        alarmScheduler.scheduleNotification(newBirthday.copy(id = newId))
                                    }
                                    importedCount++
                                }
                            }
                            ConflictStrategy.OVERWRITE -> {
                                // Delete existing if it exists
                                val existing = birthdayRepository.getBirthdayById(birthday.id)
                                if (existing != null) {
                                    birthdayRepository.deleteBirthdayById(birthday.id)
                                    alarmScheduler.cancelNotification(birthday.id)
                                }
                                // Add the imported birthday (with original ID)
                                birthdayRepository.addBirthday(birthday)
                                
                                if (birthday.notificationsEnabled) {
                                    alarmScheduler.scheduleNotification(birthday)
                                }
                                importedCount++
                            }
                            ConflictStrategy.MERGE -> {
                                // Check if birthday already exists
                                val existing = birthdayRepository.getBirthdayById(birthday.id)
                                if (existing == null) {
                                    // Add new birthday with a new ID
                                    val newBirthday = birthday.copy(id = 0)
                                    val newId = birthdayRepository.addBirthday(newBirthday)
                                    
                                    if (newBirthday.notificationsEnabled) {
                                        alarmScheduler.scheduleNotification(newBirthday.copy(id = newId))
                                    }
                                    importedCount++
                                } else {
                                    // Update existing with imported data (except ID)
                                    val updatedBirthday =
                                        existing.copy(
                                            name = birthday.name,
                                            birthDate = birthday.birthDate,
                                            notes = birthday.notes,
                                            notificationsEnabled = birthday.notificationsEnabled,
                                            advanceNotificationDays = birthday.advanceNotificationDays,
                                            notificationHour = birthday.notificationHour,
                                            notificationMinute = birthday.notificationMinute,
                                        )
                                    birthdayRepository.updateBirthday(updatedBirthday)
                                    
                                    // Re-schedule notification
                                    alarmScheduler.cancelNotification(updatedBirthday.id)
                                    if (updatedBirthday.notificationsEnabled) {
                                        alarmScheduler.scheduleNotification(updatedBirthday)
                                    }
                                    importedCount++
                                }
                            }
                        }
                    }

                    Result.success(importedCount)
                } catch (e: Exception) {
                    Timber.e(e, "Error importing birthdays")
                    Result.failure(e)
                }
            }

        /**
         * Generates a default backup file name with timestamp.
         *
         * @return The default backup file name
         */
        fun generateDefaultBackupFileName(): String {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            return "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
        }

        /**
         * Validates a backup file by checking its structure.
         *
         * @param uri The URI of the backup file to validate
         * @return Result indicating if the file is valid
         */
        suspend fun validateBackupFile(uri: Uri): Result<Boolean> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    // Read from file
                    val jsonString =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                reader.readText()
                            }
                        } ?: return@withContext Result.failure(Exception("Failed to open input stream"))

                    // Try to parse JSON
                    json.decodeFromString<BackupData>(jsonString)

                    Result.success(true)
                } catch (e: Exception) {
                    Timber.e(e, "Error validating backup file")
                    Result.success(false)
                }
            }
    }

/**
 * Strategy for handling conflicts during import.
 */
enum class ConflictStrategy {
    /**
     * Skip importing birthdays that already exist (based on ID).
     */
    SKIP,

    /**
     * Overwrite existing birthdays with imported ones.
     */
    OVERWRITE,

    /**
     * Merge imported birthdays with existing ones, updating existing data.
     */
    MERGE,
}

/**
 * Custom serializer for LocalDate
 */
object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: LocalDate,
    ) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}

/**
 * Custom serializer for LocalDateTime
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: LocalDateTime,
    ) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}
