package com.birthdayreminder.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Room type converters for handling LocalDate, LocalDateTime, LocalTime objects and Lists.
 * Converts between Java 8 time objects/Lists and String representations for database storage.
 */
class DateConverters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    /**
     * Converts LocalDate to String for database storage.
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    /**
     * Converts String from database to LocalDate.
     */
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }

    /**
     * Converts LocalDateTime to String for database storage.
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    /**
     * Converts String from database to LocalDateTime.
     */
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    /**
     * Converts LocalTime to String for database storage.
     */
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }

    /**
     * Converts String from database to LocalTime.
     */
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it, timeFormatter) }
    }

    /**
     * Converts List<Int> to String for database storage.
     */
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    /**
     * Converts String from database to List<Int>.
     */
    @TypeConverter
    fun toIntList(data: String?): List<Int>? {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.toIntOrNull() }
    }
}
