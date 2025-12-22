package com.birthdayreminder.domain.util

import com.birthdayreminder.domain.error.ErrorHandler
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Safe date calculation utility with fallback mechanisms.
 * Handles edge cases and provides graceful error recovery for date operations.
 */
@Singleton
class SafeDateCalculator @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    
    companion object {
        // Fallback values for when calculations fail
        const val FALLBACK_DAYS_UNTIL_BIRTHDAY = 0
        const val FALLBACK_AGE = 0
        val FALLBACK_DATE: LocalDate = LocalDate.now()
        
        // Reasonable limits for date calculations
        const val MAX_YEARS_IN_PAST = 150
        const val MAX_YEARS_IN_FUTURE = 100
    }
    
    /**
     * Safely calculates the next occurrence of a birthday.
     * Handles leap year edge cases and provides fallback behavior.
     * 
     * @param birthDate The original birth date
     * @param currentDate The current date (defaults to today)
     * @return The next occurrence date, or a fallback date if calculation fails
     */
    fun calculateNextOccurrence(
        birthDate: LocalDate,
        currentDate: LocalDate = LocalDate.now()
    ): DateCalculationResult<LocalDate> {
        return try {
            // Validate input dates
            if (!isValidDateRange(birthDate, currentDate)) {
                return DateCalculationResult.Error(
                    "Invalid date range",
                    currentDate
                )
            }
            
            val currentYear = currentDate.year
            
            // Try to create the birthday in the current year
            val birthdayThisYear = createBirthdayInYear(birthDate, currentYear)
                ?: return DateCalculationResult.Error(
                    "Failed to calculate birthday for current year",
                    currentDate
                )
            
            // If the birthday hasn't occurred this year, return this year's date
            val nextOccurrence = if (birthdayThisYear >= currentDate) {
                birthdayThisYear
            } else {
                // Birthday has passed this year, calculate next year's occurrence
                val nextYear = currentYear + 1
                createBirthdayInYear(birthDate, nextYear)
                    ?: return DateCalculationResult.Error(
                        "Failed to calculate birthday for next year",
                        birthdayThisYear
                    )
            }
            
            DateCalculationResult.Success(nextOccurrence)
            
        } catch (e: Exception) {
            val errorMessage = errorHandler.handleDateCalculationError(e)
            DateCalculationResult.Error(errorMessage, currentDate)
        }
    }
    
    /**
     * Safely calculates the number of days until the next birthday.
     * 
     * @param birthDate The birth date
     * @param currentDate The current date (defaults to today)
     * @return Number of days until next birthday, or fallback value if calculation fails
     */
    fun calculateDaysUntilNext(
        birthDate: LocalDate,
        currentDate: LocalDate = LocalDate.now()
    ): DateCalculationResult<Int> {
        return try {
            val nextOccurrenceResult = calculateNextOccurrence(birthDate, currentDate)
            
            when (nextOccurrenceResult) {
                is DateCalculationResult.Success -> {
                    val days = ChronoUnit.DAYS.between(currentDate, nextOccurrenceResult.value).toInt()
                    DateCalculationResult.Success(maxOf(0, days)) // Ensure non-negative
                }
                is DateCalculationResult.Error -> {
                    DateCalculationResult.Error(
                        nextOccurrenceResult.errorMessage,
                        FALLBACK_DAYS_UNTIL_BIRTHDAY
                    )
                }
            }
        } catch (e: Exception) {
            val errorMessage = errorHandler.handleDateCalculationError(e)
            DateCalculationResult.Error(errorMessage, FALLBACK_DAYS_UNTIL_BIRTHDAY)
        }
    }
    
    /**
     * Safely calculates the age a person will be on their next birthday.
     * 
     * @param birthDate The birth date
     * @param nextOccurrence The next occurrence of the birthday
     * @return The age on next birthday, or fallback value if calculation fails
     */
    fun calculateAge(
        birthDate: LocalDate,
        nextOccurrence: LocalDate = LocalDate.now()
    ): DateCalculationResult<Int> {
        return try {
            if (!isValidDateRange(birthDate, nextOccurrence)) {
                return DateCalculationResult.Error(
                    "Invalid date range for age calculation",
                    FALLBACK_AGE
                )
            }
            
            val age = Period.between(birthDate, nextOccurrence).years
            
            // Validate reasonable age range
            if (age < 0 || age > MAX_YEARS_IN_PAST) {
                return DateCalculationResult.Error(
                    "Calculated age is outside reasonable range",
                    FALLBACK_AGE
                )
            }
            
            DateCalculationResult.Success(age)
            
        } catch (e: Exception) {
            val errorMessage = errorHandler.handleDateCalculationError(e)
            DateCalculationResult.Error(errorMessage, FALLBACK_AGE)
        }
    }
    
    /**
     * Safely creates a birthday date for a specific year.
     * Handles leap year edge cases (Feb 29 on non-leap years).
     * 
     * @param birthDate The original birth date
     * @param targetYear The year to create the birthday for
     * @return The birthday date for the target year, or null if creation fails
     */
    private fun createBirthdayInYear(birthDate: LocalDate, targetYear: Int): LocalDate? {
        return try {
            birthDate.withYear(targetYear)
        } catch (e: DateTimeException) {
            // Handle leap year edge case (Feb 29 on non-leap year)
            if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29) {
                try {
                    LocalDate.of(targetYear, 2, 28)
                } catch (e2: DateTimeException) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    /**
     * Validates that dates are within reasonable ranges.
     * 
     * @param birthDate The birth date to validate
     * @param referenceDate The reference date (usually current date)
     * @return True if dates are valid, false otherwise
     */
    private fun isValidDateRange(birthDate: LocalDate, referenceDate: LocalDate): Boolean {
        val minDate = referenceDate.minusYears(MAX_YEARS_IN_PAST.toLong())
        val maxDate = referenceDate.plusYears(MAX_YEARS_IN_FUTURE.toLong())
        
        return birthDate.isAfter(minDate) && birthDate.isBefore(maxDate)
    }
    
    /**
     * Checks if a given year is a leap year.
     * 
     * @param year The year to check
     * @return True if the year is a leap year, false otherwise
     */
    fun isLeapYear(year: Int): Boolean {
        return try {
            LocalDate.of(year, 1, 1).isLeapYear
        } catch (e: DateTimeException) {
            false
        }
    }
    
    /**
     * Safely parses a date string with fallback behavior.
     * 
     * @param dateString The date string to parse
     * @param fallbackDate The fallback date if parsing fails
     * @return Parsed date or fallback date
     */
    fun safeParseDateString(
        dateString: String,
        fallbackDate: LocalDate = FALLBACK_DATE
    ): DateCalculationResult<LocalDate> {
        return try {
            val parsedDate = LocalDate.parse(dateString)
            DateCalculationResult.Success(parsedDate)
        } catch (e: Exception) {
            val errorMessage = errorHandler.handleDateCalculationError(e)
            DateCalculationResult.Error(errorMessage, fallbackDate)
        }
    }
}

/**
 * Result of a date calculation operation.
 */
sealed class DateCalculationResult<T> {
    data class Success<T>(val value: T) : DateCalculationResult<T>()
    data class Error<T>(val errorMessage: String, val fallbackValue: T) : DateCalculationResult<T>()
    
    /**
     * Returns the value if successful, or the fallback value if there was an error.
     */
    fun getValueOrFallback(): T = when (this) {
        is Success -> value
        is Error -> fallbackValue
    }
    
    /**
     * Returns true if the calculation was successful.
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Returns true if there was an error in the calculation.
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Gets the error message if there was an error.
     */
    fun getErrorMessageOrNull(): String? = when (this) {
        is Error -> errorMessage
        is Success -> null
    }
}