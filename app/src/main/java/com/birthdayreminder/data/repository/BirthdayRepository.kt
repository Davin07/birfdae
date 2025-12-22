package com.birthdayreminder.data.repository

import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for birthday data operations.
 * Provides a clean API for accessing birthday data from various sources.
 */
interface BirthdayRepository {
    
    /**
     * Retrieves all birthdays from the data source.
     * Returns a Flow for reactive updates.
     */
    fun getAllBirthdays(): Flow<List<Birthday>>
    
    /**
     * Retrieves all birthdays sorted by their next occurrence date.
     * The sorting logic handles year transitions and calculates next birthday dates.
     */
    fun getAllBirthdaysSortedByNextOccurrence(): Flow<List<Birthday>>
    
    /**
     * Retrieves birthdays for a specific date (month and day).
     * Used for finding birthdays on a particular calendar date.
     */
    fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>>
    
    /**
     * Retrieves birthdays that occur within a specific month.
     * Used for calendar view functionality.
     */
    fun getBirthdaysForMonth(month: String): Flow<List<Birthday>>
    
    /**
     * Retrieves a single birthday by its ID.
     */
    suspend fun getBirthdayById(id: Long): Birthday?
    
    /**
     * Retrieves birthdays that have notifications enabled.
     * Used for notification scheduling.
     */
    fun getBirthdaysWithNotificationsEnabled(): Flow<List<Birthday>>
    
    /**
     * Adds a new birthday to the data source.
     * Returns the ID of the newly created birthday.
     */
    suspend fun addBirthday(birthday: Birthday): Long
    
    /**
     * Updates an existing birthday in the data source.
     */
    suspend fun updateBirthday(birthday: Birthday)
    
    /**
     * Deletes a birthday from the data source.
     */
    suspend fun deleteBirthday(birthday: Birthday)
    
    /**
     * Deletes a birthday by its ID.
     */
    suspend fun deleteBirthdayById(id: Long)
    
    /**
     * Gets the total count of birthdays in the data source.
     */
    suspend fun getBirthdayCount(): Int
    
    /**
     * Searches for birthdays by name.
     * Returns birthdays that match the search query (case-insensitive).
     */
    fun searchBirthdaysByName(searchQuery: String): Flow<List<Birthday>>
}