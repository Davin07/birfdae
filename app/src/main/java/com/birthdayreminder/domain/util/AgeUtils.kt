package com.birthdayreminder.domain.util

import java.time.LocalDate
import java.time.Period

object AgeUtils {
    fun calculateCurrentAge(birthDate: LocalDate): Int {
        return Period.between(birthDate, LocalDate.now()).years
    }

    fun calculateUpcomingAge(birthDate: LocalDate): Int {
        val today = LocalDate.now()
        val currentYear = today.year
        
        // Calculate birthday in current year
        // plusYears handles leap years automatically (e.g. Feb 29 -> Feb 28 in non-leap years)
        var nextBirthday = birthDate.plusYears((currentYear - birthDate.year).toLong())
        
        if (nextBirthday.isBefore(today)) {
            nextBirthday = birthDate.plusYears((currentYear - birthDate.year + 1).toLong())
        }
        
        return Period.between(birthDate, nextBirthday).years
    }
}