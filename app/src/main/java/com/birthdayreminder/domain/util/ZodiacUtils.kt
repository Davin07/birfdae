package com.birthdayreminder.domain.util

import java.time.Month

object ZodiacUtils {
    fun getZodiacSign(month: Month, day: Int): String {
        return when (month) {
            Month.JANUARY -> if (day < 20) "Capricorn" else "Aquarius"
            Month.FEBRUARY -> if (day < 19) "Aquarius" else "Pisces"
            Month.MARCH -> if (day < 21) "Pisces" else "Aries"
            Month.APRIL -> if (day < 20) "Aries" else "Taurus"
            Month.MAY -> if (day < 21) "Taurus" else "Gemini"
            Month.JUNE -> if (day < 21) "Gemini" else "Cancer"
            Month.JULY -> if (day < 23) "Cancer" else "Leo"
            Month.AUGUST -> if (day < 23) "Leo" else "Virgo"
            Month.SEPTEMBER -> if (day < 23) "Virgo" else "Libra"
            Month.OCTOBER -> if (day < 23) "Libra" else "Scorpio"
            Month.NOVEMBER -> if (day < 22) "Scorpio" else "Sagittarius"
            Month.DECEMBER -> if (day < 22) "Sagittarius" else "Capricorn"
        }
    }
}