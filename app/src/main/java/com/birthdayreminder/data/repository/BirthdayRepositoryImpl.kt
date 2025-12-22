package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.error.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BirthdayRepository that uses Room database as the data source.
 * Handles all birthday data operations and provides business logic for sorting and filtering.
 */
@Singleton
class BirthdayRepositoryImpl @Inject constructor(
    private val birthdayDao: BirthdayDao,
    private val errorHandler: ErrorHandler
) : BirthdayRepository {
    
    override fun getAllBirthdays(): Flow<List<Birthday>> {
        return birthdayDao.getAllBirthdays()
            .catch { exception ->
                // Log the error and emit empty list as fallback
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
                // Log the error and emit empty list as fallback
                emit(emptyList())
            }
    }
    
    override fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>> {
        return birthdayDao.getBirthdaysForDate(monthDay)
    }
    
    override fun getBirthdaysForMonth(month: String): Flow<List<Birthday>> {
        return birthdayDao.getBirthdaysForMonth(month)
    }
    
    override suspend fun getBirthdayById(id: Long): Birthday? {
        return birthdayDao.getBirthdayById(id)
    }
    
    override fun getBirthdaysWithNotificationsEnabled(): Flow<List<Birthday>> {
        return birthdayDao.getBirthdaysWithNotificationsEnabled()
    }
    
    override suspend fun addBirthday(birthday: Birthday): Long {
        return try {
            birthdayDao.insertBirthday(birthday)
        } catch (e: Exception) {
            throw Exception(errorHandler.handleDatabaseError(e), e)
        }
    }
    
    override suspend fun updateBirthday(birthday: Birthday) {
        try {
            birthdayDao.updateBirthday(birthday)
        } catch (e: Exception) {
            throw Exception(errorHandler.handleDatabaseError(e), e)
        }
    }
    
    override suspend fun deleteBirthday(birthday: Birthday) {
        try {
            birthdayDao.deleteBirthday(birthday)
        } catch (e: Exception) {
            throw Exception(errorHandler.handleDatabaseError(e), e)
        }
    }
    
    override suspend fun deleteBirthdayById(id: Long) {
        try {
            birthdayDao.deleteBirthdayById(id)
        } catch (e: Exception) {
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
    private fun calculateNextOccurrence(birthDate: LocalDate, today: LocalDate): LocalDate {
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