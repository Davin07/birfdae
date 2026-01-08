package com.birthdayreminder.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AgeUtilsTest {

    @Test
    fun calculateCurrentAge_returnsCorrectAge() {
        val birthDate = LocalDate.now().minusYears(30)
        assertEquals(30, AgeUtils.calculateCurrentAge(birthDate))
        
        val birthDateUpcoming = LocalDate.now().minusYears(30).plusDays(1) // Birthday tomorrow (born 30 years ago tomorrow)
        // Age is still 29
        assertEquals(29, AgeUtils.calculateCurrentAge(birthDateUpcoming))
    }
    
    @Test
    fun calculateUpcomingAge_returnsCorrectAge() {
        // Today is birthday (30 years ago)
        val birthDate = LocalDate.now().minusYears(30)
        // Should return 30 (Turns 30 today)
        assertEquals(30, AgeUtils.calculateUpcomingAge(birthDate))
        
        // Tomorrow is birthday (30 years ago + 1 day? No, born 1994-01-09. Today 2024-01-08)
        // birthDateTomorrow should be date AFTER today.
        val birthDateTomorrow = LocalDate.now().minusYears(30).plusDays(1)
        // Born 1994-01-09. Today 2024-01-08.
        // Current age 29. Turns 30 on 2024-01-09.
        assertEquals(30, AgeUtils.calculateUpcomingAge(birthDateTomorrow))
        
        // Yesterday was birthday
        val birthDateYesterday = LocalDate.now().minusYears(30).minusDays(1)
        // Born 1994-01-07. Today 2024-01-08.
        // Current age 30. Turns 31 on 2025-01-07.
        assertEquals(31, AgeUtils.calculateUpcomingAge(birthDateYesterday))
    }
}