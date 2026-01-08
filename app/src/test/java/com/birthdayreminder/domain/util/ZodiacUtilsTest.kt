package com.birthdayreminder.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month

class ZodiacUtilsTest {

    @Test
    fun getZodiacSign_returnsCorrectSign() {
        // Aries: Mar 21 - Apr 19
        assertEquals("Aries", ZodiacUtils.getZodiacSign(Month.MARCH, 21))
        assertEquals("Aries", ZodiacUtils.getZodiacSign(Month.APRIL, 19))
        
        // Taurus: Apr 20 - May 20
        assertEquals("Taurus", ZodiacUtils.getZodiacSign(Month.APRIL, 20))
        assertEquals("Taurus", ZodiacUtils.getZodiacSign(Month.MAY, 20))
        
        // Gemini: May 21 - Jun 20
        assertEquals("Gemini", ZodiacUtils.getZodiacSign(Month.MAY, 21))
        
        // Capricorn: Dec 22 - Jan 19
        assertEquals("Capricorn", ZodiacUtils.getZodiacSign(Month.DECEMBER, 22))
        assertEquals("Capricorn", ZodiacUtils.getZodiacSign(Month.JANUARY, 19))
    }
}
