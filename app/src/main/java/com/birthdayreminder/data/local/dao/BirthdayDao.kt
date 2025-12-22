package com.birthdayreminder.data.local.dao

import androidx.room.*
import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object (DAO) for Birthday entities.
 * Provides database operations for birthday management with reactive Flow-based queries.
 */
@Dao
interface BirthdayDao {
    
    /**
     * Retrieves all birthdays from the database.
     * Returns a Flow for reactive updates when data changes.
     */
    @Query("SELECT * FROM birthdays ORDER BY name ASC")
    fun getAllBirthdays(): Flow<List<Birthday>>
    
    /**
     * Retrieves all birthdays sorted by their next occurrence date.
     * This query calculates which birthday comes next chronologically.
     * Note: The actual sorting by next occurrence will be handled in the repository layer
     * since Room doesn't support complex date calculations with LocalDate.
     */
    @Query("SELECT * FROM birthdays ORDER BY birthDate ASC")
    fun getAllBirthdaysSortedByDate(): Flow<List<Birthday>>
    
    /**
     * Retrieves birthdays for a specific month and day.
     * Useful for finding birthdays on a particular date.
     */
    @Query("SELECT * FROM birthdays WHERE strftime('%m-%d', birthDate) = :monthDay")
    fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>>
    
    /**
     * Retrieves birthdays that occur within a specific month.
     * Used for calendar view to show birthdays in the current month.
     */
    @Query("SELECT * FROM birthdays WHERE strftime('%m', birthDate) = :month")
    fun getBirthdaysForMonth(month: String): Flow<List<Birthday>>
    
    /**
     * Retrieves a single birthday by its ID.
     */
    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthdayById(id: Long): Birthday?
    
    /**
     * Retrieves birthdays that have notifications enabled.
     * Used for scheduling notification workers.
     */
    @Query("SELECT * FROM birthdays WHERE notificationsEnabled = 1")
    fun getBirthdaysWithNotificationsEnabled(): Flow<List<Birthday>>
    
    /**
     * Inserts a new birthday into the database.
     * Returns the ID of the newly inserted birthday.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: Birthday): Long
    
    /**
     * Inserts multiple birthdays into the database.
     * Useful for bulk operations or data import.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthdays(birthdays: List<Birthday>)
    
    /**
     * Updates an existing birthday in the database.
     */
    @Update
    suspend fun updateBirthday(birthday: Birthday)
    
    /**
     * Deletes a birthday from the database.
     */
    @Delete
    suspend fun deleteBirthday(birthday: Birthday)
    
    /**
     * Deletes a birthday by its ID.
     */
    @Query("DELETE FROM birthdays WHERE id = :id")
    suspend fun deleteBirthdayById(id: Long)
    
    /**
     * Deletes all birthdays from the database.
     * Useful for testing or data reset scenarios.
     */
    @Query("DELETE FROM birthdays")
    suspend fun deleteAllBirthdays()
    
    /**
     * Gets the count of all birthdays in the database.
     */
    @Query("SELECT COUNT(*) FROM birthdays")
    suspend fun getBirthdayCount(): Int
    
    /**
     * Searches for birthdays by name (case-insensitive).
     * Useful for implementing search functionality.
     */
    @Query("SELECT * FROM birthdays WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchBirthdaysByName(searchQuery: String): Flow<List<Birthday>>
}