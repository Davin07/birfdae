package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.dao.ReminderEventDao
import com.birthdayreminder.data.local.entity.ReminderEvent
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed [ReminderEventRepository].
 *
 * Writes are deliberately non-fatal. A streak is a small piece of garnish on
 * the home screen, and failing to record one must never interrupt the user or
 * surface an error dialog; the worst case of a lost write is a streak that is
 * one short, which is a far better outcome than a crash on a tap.
 *
 * The failure is logged rather than swallowed, though, because it is otherwise
 * invisible: a reminder that stops recording looks exactly like a user who
 * stopped opening reminders, and there is no way to tell those apart from the
 * database.
 */
@Singleton
class ReminderEventRepositoryImpl
    @Inject
    constructor(
        private val reminderEventDao: ReminderEventDao,
    ) : ReminderEventRepository {
        override fun getEventsForBirthday(birthdayId: Long): Flow<List<ReminderEvent>> =
            reminderEventDao.getEventsForBirthday(birthdayId)

        override fun getAllEvents(): Flow<List<ReminderEvent>> = reminderEventDao.getAllEvents()

        override suspend fun getEventsForBirthdaySnapshot(birthdayId: Long): List<ReminderEvent> =
            reminderEventDao.getEventsForBirthdaySnapshot(birthdayId)

        override suspend fun recordAcknowledged(
            birthdayId: Long,
            year: Int,
            at: LocalDateTime,
        ) {
            runCatching { reminderEventDao.recordAcknowledged(birthdayId, year, at) }
                .onFailure { Timber.e(it, "Could not record acknowledgement for $birthdayId in $year") }
        }

        override suspend fun recordShared(
            birthdayId: Long,
            year: Int,
            at: LocalDateTime,
        ) {
            runCatching { reminderEventDao.recordShared(birthdayId, year, at) }
                .onFailure { Timber.e(it, "Could not record share for $birthdayId in $year") }
        }
    }
