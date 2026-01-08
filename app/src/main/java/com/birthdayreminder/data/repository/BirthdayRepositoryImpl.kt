package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.error.ErrorHandler
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BirthdayRepository that uses Room database as the data source.
 * Handles all birthday data operations and provides business logic for sorting and filtering.
 */
@Singleton
class BirthdayRepositoryImpl
    @Inject
    constructor(
        private val birthdayDao: BirthdayDao,
        private val errorHandler: ErrorHandler,
    ) : BirthdayRepository {
        override fun getAllBirthdays(): Flow<List<Birthday>> {
            return birthdayDao.getAllBirthdays()
        }

        override fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>> {
            return birthdayDao.getBirthdaysForDate(monthDay)
        }

        override fun getBirthdaysForMonth(month: String): Flow<List<Birthday>> {
            return birthdayDao.getBirthdaysForMonth(month)
        }

        override suspend fun getBirthdayById(id: Long): Birthday? {
            return try {
                birthdayDao.getBirthdayById(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get birthday by id $id")
                null
            }
        }

        override fun getBirthdaysWithNotificationsEnabled(): Flow<List<Birthday>> {
            return birthdayDao.getBirthdaysWithNotificationsEnabled()
        }

        override suspend fun addBirthday(birthday: Birthday): Long {
            return try {
                birthdayDao.insertBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to add birthday: ${birthday.name}")
                throw Exception(errorHandler.handleDatabaseError(e), e)
            }
        }

        override suspend fun updateBirthday(birthday: Birthday) {
            try {
                birthdayDao.updateBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update birthday: ${birthday.id}")
                throw Exception(errorHandler.handleDatabaseError(e), e)
            }
        }

        override suspend fun deleteBirthday(birthday: Birthday) {
            try {
                birthdayDao.deleteBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete birthday: ${birthday.id}")
                throw Exception(errorHandler.handleDatabaseError(e), e)
            }
        }

        override suspend fun deleteBirthdayById(id: Long) {
            try {
                birthdayDao.deleteBirthdayById(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete birthday by id: $id")
                throw Exception(errorHandler.handleDatabaseError(e), e)
            }
        }

        override suspend fun getBirthdayCount(): Int {
            return birthdayDao.getBirthdayCount()
        }

        override fun searchBirthdaysByName(searchQuery: String): Flow<List<Birthday>> {
            return birthdayDao.searchBirthdaysByName(searchQuery)
        }

        /**
         * Formats a LocalDate to MM-dd format for database queries.
         */
        private fun formatMonthDay(date: LocalDate): String {
            return date.format(DateTimeFormatter.ofPattern("MM-dd"))
        }

        /**
         * Formats a month number to MM format for database queries.
         */
        private fun formatMonth(month: Int): String {
            return String.format("%02d", month)
        }
    }
