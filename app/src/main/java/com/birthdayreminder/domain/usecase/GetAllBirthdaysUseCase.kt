package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for retrieving all birthdays with sorting and countdown calculations.
 * Provides different sorting options and filtering capabilities.
 */
@Singleton
class GetAllBirthdaysUseCase @Inject constructor(
    private val birthdayRepository: BirthdayRepository,
    private val calculateCountdownUseCase: CalculateCountdownUseCase
) {
    
    /**
     * Gets all birthdays sorted by next occurrence date.
     * This is the primary method for displaying birthdays in chronological order.
     * 
     * @return Flow of birthdays with countdown information, sorted by next occurrence
     */
    fun getAllBirthdaysSortedByNextOccurrence(): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getAllBirthdays()
            .map { birthdays ->
                calculateCountdownUseCase.calculateCountdowns(birthdays)
            }
    }
    
    /**
     * Gets all birthdays sorted alphabetically by name.
     * Useful for displaying birthdays in a directory-style format.
     * 
     * @return Flow of birthdays with countdown information, sorted by name
     */
    fun getAllBirthdaysSortedByName(): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getAllBirthdays()
            .map { birthdays ->
                birthdays
                    .map { calculateCountdownUseCase.calculateCountdown(it) }
                    .sortedBy { it.name.lowercase() }
            }
    }
    
    /**
     * Gets all birthdays sorted by birth date (chronological age order).
     * Useful for seeing who is oldest/youngest.
     * 
     * @return Flow of birthdays with countdown information, sorted by birth date
     */
    fun getAllBirthdaysSortedByBirthDate(): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getAllBirthdays()
            .map { birthdays ->
                birthdays
                    .map { calculateCountdownUseCase.calculateCountdown(it) }
                    .sortedBy { it.birthDate }
            }
    }
    
    /**
     * Gets birthdays that are happening today.
     * 
     * @return Flow of today's birthdays with countdown information
     */
    fun getTodaysBirthdays(): Flow<List<BirthdayWithCountdown>> {
        return getAllBirthdaysSortedByNextOccurrence()
            .map { birthdays ->
                birthdays.filter { it.isToday }
            }
    }
    
    /**
     * Gets birthdays happening within the next specified number of days.
     * 
     * @param days Number of days to look ahead (default: 7)
     * @return Flow of upcoming birthdays within the specified timeframe
     */
    fun getUpcomingBirthdays(days: Int = 7): Flow<List<BirthdayWithCountdown>> {
        return getAllBirthdaysSortedByNextOccurrence()
            .map { birthdays ->
                birthdays.filter { it.daysUntilNext <= days }
            }
    }
    
    /**
     * Gets birthdays for a specific month.
     * Useful for calendar view functionality.
     * 
     * @param month The month to filter by (1-12)
     * @param year The year to filter by (optional, defaults to current year)
     * @return Flow of birthdays occurring in the specified month
     */
    fun getBirthdaysForMonth(month: Int, year: Int = LocalDate.now().year): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getAllBirthdays()
            .map { birthdays ->
                birthdays
                    .filter { birthday ->
                        val nextOccurrence = calculateCountdownUseCase.calculateCountdown(birthday).nextOccurrence
                        nextOccurrence.monthValue == month && nextOccurrence.year == year
                    }
                    .map { calculateCountdownUseCase.calculateCountdown(it) }
                    .sortedBy { it.nextOccurrence.dayOfMonth }
            }
    }
    
    /**
     * Gets birthdays for a specific date (month and day).
     * 
     * @param month The month (1-12)
     * @param day The day of month (1-31)
     * @return Flow of birthdays occurring on the specified date
     */
    fun getBirthdaysForDate(month: Int, day: Int): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getAllBirthdays()
            .map { birthdays ->
                birthdays
                    .filter { birthday ->
                        birthday.birthDate.monthValue == month && birthday.birthDate.dayOfMonth == day
                    }
                    .map { calculateCountdownUseCase.calculateCountdown(it) }
                    .sortedBy { it.name.lowercase() }
            }
    }
    
    /**
     * Searches birthdays by name.
     * 
     * @param searchQuery The search query (case-insensitive)
     * @return Flow of birthdays matching the search query
     */
    fun searchBirthdays(searchQuery: String): Flow<List<BirthdayWithCountdown>> {
        return if (searchQuery.isBlank()) {
            getAllBirthdaysSortedByName()
        } else {
            birthdayRepository.searchBirthdaysByName(searchQuery)
                .map { birthdays ->
                    birthdays
                        .map { calculateCountdownUseCase.calculateCountdown(it) }
                        .sortedBy { it.name.lowercase() }
                }
        }
    }
    
    /**
     * Gets birthdays with notifications enabled.
     * Useful for notification scheduling.
     * 
     * @return Flow of birthdays that have notifications enabled
     */
    fun getBirthdaysWithNotificationsEnabled(): Flow<List<BirthdayWithCountdown>> {
        return birthdayRepository.getBirthdaysWithNotificationsEnabled()
            .map { birthdays ->
                calculateCountdownUseCase.calculateCountdowns(birthdays)
            }
    }
    
    /**
     * Gets the total count of birthdays.
     * 
     * @return The total number of birthdays in the database
     */
    suspend fun getBirthdayCount(): Int {
        return birthdayRepository.getBirthdayCount()
    }
}