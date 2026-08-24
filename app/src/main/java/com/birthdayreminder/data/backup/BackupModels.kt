package com.birthdayreminder.data.backup

import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Current backup file format version.
 * Version 2 introduced a dedicated backup DTO decoupled from the database schema.
 */
const val BACKUP_FORMAT_VERSION = 2

/**
 * Oldest backup format version that can still be imported.
 * Version 1 files serialized the Room entity directly and remain readable.
 */
const val MIN_SUPPORTED_BACKUP_VERSION = 1

/**
 * JSON configuration for backup serialization.
 *
 * Lenient towards unknown fields so that backups created by newer or older app versions,
 * including legacy version 1 entity-shaped payloads, can still be parsed.
 */
val BackupJson: Json =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

/**
 * Root structure of a backup file.
 *
 * @param version Backup format version of the file
 * @param exportDate ISO-8601 timestamp of when the backup was created
 * @param birthdays All birthdays contained in the backup
 */
@Serializable
data class BackupFileDto(
    val version: Int = BACKUP_FORMAT_VERSION,
    val exportDate: String = "",
    val birthdays: List<BirthdayBackupDto> = emptyList(),
)

/**
 * Serializable representation of a single birthday in a backup file.
 *
 * Deliberately decoupled from [Birthday]: dates are ISO strings, IDs are excluded, and every
 * field except identifying data has a default so legacy version 1 payloads still deserialize.
 *
 * @param name Name of the person
 * @param birthDate Birth date as ISO-8601 local date string (yyyy-MM-dd)
 * @param notes Optional notes
 * @param notificationsEnabled Whether notifications were enabled for this birthday
 * @param advanceNotificationDays Days before the birthday to send an advance notification
 * @param notificationHour Optional legacy hour of day for notifications (0-23)
 * @param notificationMinute Optional legacy minute of day for notifications (0-59)
 * @param imageUri Optional image URI string
 * @param relationship Optional relationship type
 * @param isPinned Whether the birthday was pinned
 * @param notificationOffsets Day offsets for additional notifications
 * @param notificationTime Optional specific notification time as ISO-8601 local time string
 * @param createdAt Optional creation timestamp as ISO-8601 local date-time string
 */
@Serializable
data class BirthdayBackupDto(
    val name: String,
    @SerialName("birthDate") val birthDate: String,
    val notes: String? = null,
    val notificationsEnabled: Boolean = true,
    val advanceNotificationDays: Int = 0,
    val notificationHour: Int? = null,
    val notificationMinute: Int? = null,
    val imageUri: String? = null,
    val relationship: String? = null,
    val isPinned: Boolean = false,
    val notificationOffsets: List<Int> = emptyList(),
    val notificationTime: String? = null,
    val createdAt: String? = null,
)

/**
 * Parses a raw backup file payload into a [BackupFileDto].
 *
 * Both current (version 2) and legacy (version 1) payloads are accepted; legacy payloads simply
 * carry extra ignored fields such as the record id.
 *
 * @param jsonString Raw JSON contents of a backup file
 * @return Parsed backup file
 * @throws kotlinx.serialization.SerializationException if the payload is not a valid backup file
 */
fun parseBackupFile(jsonString: String): BackupFileDto {
    val backupFile = BackupJson.decodeFromString(BackupFileDto.serializer(), jsonString)
    return if (backupFile.version < MIN_SUPPORTED_BACKUP_VERSION) {
        backupFile.copy(version = MIN_SUPPORTED_BACKUP_VERSION)
    } else {
        backupFile
    }
}

/**
 * Serializes a [BackupFileDto] to its JSON representation.
 *
 * @return Pretty-printed JSON string of the backup file
 */
fun BackupFileDto.toJsonString(): String {
    return BackupJson.encodeToString(BackupFileDto.serializer(), this)
}

/**
 * Maps a domain birthday entity to its backup representation.
 */
fun Birthday.toBackupDto(): BirthdayBackupDto {
    return BirthdayBackupDto(
        name = name,
        birthDate = birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        notes = notes,
        notificationsEnabled = notificationsEnabled,
        advanceNotificationDays = advanceNotificationDays,
        notificationHour = notificationHour,
        notificationMinute = notificationMinute,
        imageUri = imageUri,
        relationship = relationship,
        isPinned = isPinned,
        notificationOffsets = notificationOffsets,
        notificationTime = notificationTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
        createdAt = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
}

/**
 * Maps a backed-up birthday back to a domain entity with a fresh database identity.
 *
 * The id is always reset to 0 so Room assigns a new auto-generated key on insert.
 */
fun BirthdayBackupDto.toEntity(): Birthday {
    return Birthday(
        id = 0,
        name = name,
        birthDate = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE),
        notes = notes,
        notificationsEnabled = notificationsEnabled,
        advanceNotificationDays = advanceNotificationDays,
        notificationHour = notificationHour,
        notificationMinute = notificationMinute,
        imageUri = imageUri,
        relationship = relationship,
        isPinned = isPinned,
        notificationOffsets = notificationOffsets,
        notificationTime = notificationTime?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) },
        createdAt =
            createdAt?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
                ?: LocalDateTime.now(),
    )
}

/**
 * Natural key used to detect whether two birthday records refer to the same person.
 *
 * Auto-generated primary keys are device-local, so identity is matched by normalized name plus
 * birth date instead. This makes restores behave correctly across devices and fresh installs.
 *
 * @return Normalized natural key for this birthday
 */
fun Birthday.naturalKey(): String {
    return "${name.trim().lowercase()}|${birthDate.format(DateTimeFormatter.BASIC_ISO_DATE)}"
}

/**
 * Natural key used to detect whether two birthday records refer to the same person.
 *
 * @see naturalKey for why identity matching does not rely on database ids
 * @return Normalized natural key for this backed-up birthday
 */
fun BirthdayBackupDto.naturalKey(): String {
    val date = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE)
    return "${name.trim().lowercase()}|${date.format(DateTimeFormatter.BASIC_ISO_DATE)}"
}

/**
 * Strategy for handling conflicts during import when a backed-up birthday already exists locally.
 */
enum class ConflictStrategy {
    /**
     * Skip importing birthdays that already exist locally (matched by natural key).
     */
    SKIP,

    /**
     * Delete the existing local birthday and replace it with the imported one.
     */
    OVERWRITE,

    /**
     * Keep the existing record's identity but overwrite its data with the imported values.
     */
    MERGE,
}
