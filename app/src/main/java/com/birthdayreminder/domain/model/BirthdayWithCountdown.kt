package com.birthdayreminder.domain.model

import com.birthdayreminder.data.local.entity.Birthday
import java.time.LocalDate

/**
 * Domain model representing a birthday with calculated countdown information.
 * This model combines the birthday entity with computed values for UI display.
 */
data class BirthdayWithCountdown(
    val birthday: Birthday,
    val daysUntilNext: Int,
    val nextOccurrence: LocalDate,
    val isToday: Boolean,
    val age: Int,
) {
    // Convenience properties for easier access
    val id: Long get() = birthday.id
    val name: String get() = birthday.name
    val birthDate: LocalDate get() = birthday.birthDate
    val notes: String? get() = birthday.notes
    val notificationsEnabled: Boolean get() = birthday.notificationsEnabled
    val advanceNotificationDays: Int get() = birthday.advanceNotificationDays

    /**
     * Returns a formatted countdown string for display.
     */
    val countdownText: String
        get() =
            when {
                isToday -> "Today!"
                daysUntilNext == 1 -> "Tomorrow"
                daysUntilNext < 7 -> "In $daysUntilNext days"
                daysUntilNext < 30 -> "In ${daysUntilNext / 7} weeks"
                else -> "In ${daysUntilNext / 30} months"
            }

    /**
     * Returns the age they will turn on their next birthday.
     */
    val nextAge: Int get() = age
}
