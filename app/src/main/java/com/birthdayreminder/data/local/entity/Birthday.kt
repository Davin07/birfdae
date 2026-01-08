package com.birthdayreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Room entity representing a birthday entry in the local database.
 * Contains all necessary fields for birthday tracking including notification preferences.
 */
@Entity(tableName = "birthdays")
@Serializable
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * Name of the person whose birthday this is.
     * Required field for birthday identification.
     */
    val name: String,
    /**
     * The birth date of the person.
     * Used for calculating countdowns and next occurrence.
     */
    @Contextual
    val birthDate: LocalDate,
    /**
     * Optional notes about the birthday or person.
     * Can include gift ideas, relationship info, etc.
     */
    val notes: String? = null,
    /**
     * Whether notifications are enabled for this specific birthday.
     * Allows per-birthday notification control.
     */
    val notificationsEnabled: Boolean = true,
    /**
     * Number of days before the birthday to send advance notification.
     * 0 = day of birthday only, 1 = 1 day before, 3 = 3 days before, 7 = 7 days before
     */
    val advanceNotificationDays: Int = 0,
    /**
     * Hour of the day to send notifications (0-23).
     * If null, defaults to 9 AM.
     */
    val notificationHour: Int? = null,
    /**
     * Minute of the hour to send notifications (0-59).
     * If null, defaults to 0 minutes.
     */
    val notificationMinute: Int? = null,
    /**
     * URI string for the contact's image.
     */
    val imageUri: String? = null,
    /**
     * Relationship type (e.g., Family, Friend, Work).
     */
    val relationship: String? = null,
    /**
     * Whether this birthday is pinned to the top of the list.
     */
    @androidx.room.ColumnInfo(defaultValue = "0")
    val isPinned: Boolean = false,
    /**
     * List of day offsets for notifications (e.g., [0, 3] for on-day and 3 days before).
     */
    @androidx.room.ColumnInfo(defaultValue = "")
    val notificationOffsets: List<Int> = emptyList(),
    /**
     * Specific time to send the notification.
     * Replaces notificationHour/Minute.
     */
    @Contextual
    val notificationTime: LocalTime? = null,
    /**
     * Timestamp when this birthday entry was created.
     * Used for sorting and data management.
     */
    @Contextual
    val createdAt: LocalDateTime = java.time.Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
)
