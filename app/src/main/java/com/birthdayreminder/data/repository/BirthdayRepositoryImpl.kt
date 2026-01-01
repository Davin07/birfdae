package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.error.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
                .catch { exception ->
                    Timber.e(exception, "Failed to get all birthdays")
                    emit(emptyList())
                }
        }

        override fun getAllBirthdaysSortedByNextOccurrence(): Flow<List<Birthday>> {
            return birthdayDao.getAllBirthdaysSortedByDate()
                .map { birthdays ->
                    try {
                        sortBirthdaysByNextOccurrence(birthdays)
                    } catch (e: Exception) {
                        // If sorting fails, return unsorted list as fallback
                        birthdays
                    }
                }
                .catch { exception ->
                    Timber.e(exception, "Failed to get birthdays sorted by next occurrence")
                    emit(emptyList())
                }
        }

        override fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>> {
            return birthdayDao.getBirthdaysForDate(monthDay)
                .catch { exception ->
                    Timber.e(exception, "Failed to get birthdays for date $monthDay")
                    emit(emptyList())
                }
        }

        override fun getBirthdaysForMonth(month: String): Flow<List<Birthday>> {
            return birthdayDao.getBirthdaysForMonth(month)
                .catch { exception ->
                    Timber.e(exception, "Failed to get birthdays for month $month")
                    emit(emptyList())
                }
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
                .catch { exception ->
                    Timber.e(exception, "Failed to get birthdays with notifications")
                    emit(emptyList())
                }
        }

        override suspend fun addBirthday(birthday: Birthday): Long {
            return try {
                birthdayDao.insertBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to add birthday: ${birthday.name}")
                throw Exception(errorHandler.handleDatabaseError(e))
            }
        }

        override suspend fun updateBirthday(birthday: Birthday) {
            try {
                birthdayDao.updateBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update birthday: ${birthday.id}")
                throw Exception(errorHandler.handleDatabaseError(e))
            }
        }

        override suspend fun deleteBirthday(birthday: Birthday) {
            try {
                birthdayDao.deleteBirthday(birthday)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete birthday: ${birthday.id}")
                throw Exception(errorHandler.handleDatabaseError(e))
            }
        }

        override suspend fun deleteBirthdayById(id: Long) {
            try {
                birthdayDao.deleteBirthdayById(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete birthday by id: $id")
                throw Exception(errorHandler.handleDatabaseError(e))
            }
        }

        override suspend fun getBirthdayCount(): Int {
            return birthdayDao.getBirthdayCount()
        }

        override fun searchBirthdaysByName(searchQuery: String): Flow<List<Birthday>> {
            return birthdayDao.searchBirthdaysByName(searchQuery)
                .catch { exception ->
                    Timber.e(exception, "Failed to search birthdays for query: $searchQuery")
                    emit(emptyList())
                }
        }

        /**
         * Sorts birthdays by their next occurrence date.
         * Handles year transitions and calculates the next birthday date for each person.
         */
        private fun sortBirthdaysByNextOccurrence(birthdays: List<Birthday>): List<Birthday> {
            val today = LocalDate.now()

            return birthdays.sortedBy { birthday ->
                calculateNextOccurrence(birthday.birthDate, today)
            }
        }

        /**
         * Calculates the next occurrence date for a given birthday.
         * If the birthday has already passed this year, returns next year's date.
         */
        private fun calculateNextOccurrence(
            birthDate: LocalDate,
            today: LocalDate,
        ): LocalDate {
            val thisYearBirthday = birthDate.withYear(today.year)

            return if (thisYearBirthday.isBefore(today) || thisYearBirthday.isEqual(today)) {
                // Birthday has passed this year or is today, so next occurrence is next year
                birthDate.withYear(today.year + 1)
            } else {
                // Birthday hasn't happened yet this year
                thisYearBirthday
            }
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
