package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.usecase.UpdateBirthdayResult
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * One person's reminder overrides.
 *
 * Both the on/off switch and the lead time are per person in the data model
 * rather than global, so the two Settings rows that mention them land here.
 * There is no separate lead-time screen: it is this list, one column narrower.
 */
@HiltViewModel
class PerPersonReminderViewModel
    @Inject
    constructor(
        birthdayRepository: BirthdayRepository,
        private val updateBirthdayUseCase: UpdateBirthdayUseCase,
    ) : ViewModel() {
        private val _errors = MutableStateFlow<Map<Long, String>>(emptyMap())

        /**
         * The people whose reminders can be overridden, in name order.
         *
         * Reads straight from the database so a write shows up without this
         * class keeping a second copy that can drift from it.
         */
        val people: StateFlow<List<PerPersonReminderRow>> =
            birthdayRepository
                .getAllBirthdays()
                .map { birthdays ->
                    birthdays
                        .sortedBy { it.name.lowercase() }
                        .map { person ->
                            PerPersonReminderRow(
                                id = person.id,
                                name = person.name,
                                birthDate = person.birthDate,
                                leadTimeDays = person.advanceNotificationDays,
                                notificationsEnabled = person.notificationsEnabled,
                                reminderTime = person.notificationTime,
                            )
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        /** Per-row failure messages, so a failed write is never silent. */
        val errors: StateFlow<Map<Long, String>> = _errors.asStateFlow()

        /**
         * Sets how many days before the birthday to notify this person.
         *
         * @param birthdayId the person being changed
         * @param days 0 for the day itself, or 1, 3 or 7 for an advance reminder
         */
        fun setLeadTime(
            birthdayId: Long,
            days: Int,
        ) {
            persist(birthdayId) {
                updateBirthdayUseCase.updateBirthdayPartial(
                    birthdayId = birthdayId,
                    advanceNotificationDays = days,
                )
            }
        }

        /**
         * Turns reminders on or off for one person.
         *
         * @param birthdayId the person being changed
         * @param enabled whether to notify about this birthday
         */
        fun setNotificationsEnabled(
            birthdayId: Long,
            enabled: Boolean,
        ) {
            persist(birthdayId) {
                updateBirthdayUseCase.updateBirthdayPartial(
                    birthdayId = birthdayId,
                    notificationsEnabled = enabled,
                )
            }
        }

        /**
         * Sets the time of day this person's reminder arrives.
         *
         * @param birthdayId the person being changed
         * @param time when to deliver the reminder
         */
        fun setReminderTime(
            birthdayId: Long,
            time: java.time.LocalTime,
        ) {
            persist(birthdayId) {
                updateBirthdayUseCase.updateBirthdayPartial(
                    birthdayId = birthdayId,
                    notificationTime = time,
                )
            }
        }

        /** Clears the message for one row once it has been read. */
        fun clearError(birthdayId: Long) {
            _errors.value = _errors.value - birthdayId
        }

        /**
         * Issues a partial update and records why it failed, if it did.
         *
         * A write here is a one-tap adjustment to an already-saved person, and
         * the list is driven by the database, so a silent failure would leave a
         * control showing a value the database does not have. The message sits
         * on the affected row rather than in a dialog, because what the user
         * needs to know is which row did not save.
         */
        private fun persist(
            birthdayId: Long,
            update: suspend () -> UpdateBirthdayResult,
        ) {
            viewModelScope.launch {
                when (val result = update()) {
                    is UpdateBirthdayResult.Success -> _errors.value = _errors.value - birthdayId
                    is UpdateBirthdayResult.ValidationError ->
                        fail(birthdayId, result.errors.joinToString(", "))
                    is UpdateBirthdayResult.NotFound -> fail(birthdayId, result.message)
                    is UpdateBirthdayResult.DatabaseError ->
                        fail(birthdayId, result.exception.message ?: "Could not save")
                    is UpdateBirthdayResult.ExactAlarmPermissionNotGranted ->
                        fail(birthdayId, "Birf Dae needs alarm permission to send reminders")
                }
            }
        }

        private fun fail(
            birthdayId: Long,
            message: String,
        ) {
            _errors.value = _errors.value + (birthdayId to message)
        }
    }

/**
 * A person as shown on the override list.
 *
 * Flat fields rather than the entity, so the screen cannot write back a stale
 * copy of an unrelated field.
 */
data class PerPersonReminderRow(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val leadTimeDays: Int,
    val notificationsEnabled: Boolean,
    /**
     * When this person's reminder arrives, or null when the app default
     * applies. Null is not the same as 9:00 AM: the global setting owns the
     * default, and writing 9:00 AM here would silently detach this person from
     * a change made in Settings.
     */
    val reminderTime: java.time.LocalTime?,
)

/** The lead-time choices the app offers, in the order they are shown. */
val LEAD_TIME_CHOICES = listOf(0, 1, 3, 7)

/**
 * Describes a lead time the way the row does.
 *
 * @param days days before the birthday
 */
fun leadTimeLabel(days: Int): String =
    when (days) {
        0 -> "On the day"
        1 -> "1 day before"
        7 -> "1 week before"
        else -> "$days days before"
    }
