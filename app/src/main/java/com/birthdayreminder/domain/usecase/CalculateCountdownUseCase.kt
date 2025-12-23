package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.domain.util.SafeDateCalculator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for calculating countdown information for birthdays.
 * Handles leap years, edge cases, and provides accurate countdown calculations.
 */
@Singleton
class CalculateCountdownUseCase
    @Inject
    constructor(
        private val safeDateCalculator: SafeDateCalculator,
    ) {
        /**
         * Calculates countdown information for a single birthday.
         *
         * @param birthday The birthday to calculate countdown for
         * @param currentDate The current date (defaults to today, useful for testing)
         * @return BirthdayWithCountdown with calculated countdown information
         */
        fun calculateCountdown(
            birthday: Birthday,
            currentDate: LocalDate = LocalDate.now(),
        ): BirthdayWithCountdown {
            val nextOccurrenceResult = safeDateCalculator.calculateNextOccurrence(birthday.birthDate, currentDate)
            val nextOccurrence = nextOccurrenceResult.getValueOrFallback()

            val daysUntilNextResult = safeDateCalculator.calculateDaysUntilNext(birthday.birthDate, currentDate)
            val daysUntilNext = daysUntilNextResult.getValueOrFallback()

            val isToday = daysUntilNext == 0

            val ageResult = safeDateCalculator.calculateAge(birthday.birthDate, nextOccurrence)
            val age = ageResult.getValueOrFallback()

            return BirthdayWithCountdown(
                birthday = birthday,
                daysUntilNext = daysUntilNext,
                nextOccurrence = nextOccurrence,
                isToday = isToday,
                age = age,
            )
        }

        /**
         * Calculates countdown information for a list of birthdays.
         *
         * @param birthdays List of birthdays to calculate countdowns for
         * @param currentDate The current date (defaults to today, useful for testing)
         * @return List of BirthdayWithCountdown sorted by next occurrence
         */
        fun calculateCountdowns(
            birthdays: List<Birthday>,
            currentDate: LocalDate = LocalDate.now(),
        ): List<BirthdayWithCountdown> {
            return birthdays
                .map { calculateCountdown(it, currentDate) }
                .sortedWith(compareBy<BirthdayWithCountdown> { it.daysUntilNext }.thenBy { it.name })
        }

        /**
         * Checks if a given year is a leap year.
         *
         * @param year The year to check
         * @return True if the year is a leap year, false otherwise
         */
        fun isLeapYear(year: Int): Boolean {
            return safeDateCalculator.isLeapYear(year)
        }

        /**
         * Gets the number of days until a specific birthday from today.
         * Convenience method for quick countdown calculations.
         *
         * @param birthDate The birth date to calculate countdown for
         * @return Number of days until the next occurrence of this birthday
         */
        fun getDaysUntilBirthday(birthDate: LocalDate): Int {
            val result = safeDateCalculator.calculateDaysUntilNext(birthDate)
            return result.getValueOrFallback()
        }
    }
