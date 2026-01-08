package com.birthdayreminder.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class DateConvertersTest {
    private val converter = DateConverters()

    @Test
    fun localTimeConversion() {
        val time = LocalTime.of(14, 30, 0)
        val string = converter.fromLocalTime(time)
        assertEquals("14:30:00", string)
        assertEquals(time, converter.toLocalTime(string))
    }

    @Test
    fun intListConversion() {
        val list = listOf(1, 2, 3)
        val string = converter.fromIntList(list)
        assertEquals("1,2,3", string)
        assertEquals(list, converter.toIntList(string))
    }
    
    @Test
    fun emptyIntListConversion() {
        val list = emptyList<Int>()
        val string = converter.fromIntList(list)
        assertEquals("", string)
        assertEquals(list, converter.toIntList(string))
    }
    
    @Test
    fun nullIntListConversion() {
        val string = converter.fromIntList(null)
        assertEquals(null, string)
        assertEquals(emptyList<Int>(), converter.toIntList(null))
    }
}
