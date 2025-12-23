package com.birthdayreminder.domain.validation

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class BirthdayValidatorTest {
    private val validator = BirthdayValidator()

    @Test
    fun `validateNotificationHour returns null for valid hours`() {
        // Valid hours: 0-23
        for (hour in 0..23) {
            val result = validator.validateNotificationHour(hour)
            assertEquals(null, result)
        }
    }

    @Test
    fun `validateNotificationHour returns error for invalid hours`() {
        // Hours below minimum
        val result1 = validator.validateNotificationHour(-1)
        assertEquals(BirthdayValidator.ERROR_INVALID_NOTIFICATION_HOUR, result1)

        // Hours above maximum
        val result2 = validator.validateNotificationHour(24)
        assertEquals(BirthdayValidator.ERROR_INVALID_NOTIFICATION_HOUR, result2)
    }

    @Test
    fun `validateNotificationHour returns null for null value`() {
        val result = validator.validateNotificationHour(null)
        assertEquals(null, result)
    }

    @Test
    fun `validateNotificationMinute returns null for valid minutes`() {
        // Valid minutes: 0-59
        for (minute in 0..59) {
            val result = validator.validateNotificationMinute(minute)
            assertEquals(null, result)
        }
    }

    @Test
    fun `validateNotificationMinute returns error for invalid minutes`() {
        // Minutes below minimum
        val result1 = validator.validateNotificationMinute(-1)
        assertEquals(BirthdayValidator.ERROR_INVALID_NOTIFICATION_MINUTE, result1)

        // Minutes above maximum
        val result2 = validator.validateNotificationMinute(60)
        assertEquals(BirthdayValidator.ERROR_INVALID_NOTIFICATION_MINUTE, result2)
    }

    @Test
    fun `validateNotificationMinute returns null for null value`() {
        val result = validator.validateNotificationMinute(null)
        assertEquals(null, result)
    }

    @Test
    fun `validateBirthday includes notification time validation`() {
        // Valid case
        val validResult =
            validator.validateBirthday(
                name = "John Doe",
                birthDate = LocalDate.now().minusYears(25),
                notificationHour = 9,
                notificationMinute = 30,
            )
        assertTrue(validResult.isValid)

        // Invalid hour
        val invalidHourResult =
            validator.validateBirthday(
                name = "John Doe",
                birthDate = LocalDate.now().minusYears(25),
                notificationHour = 25,
            )
        assertTrue(invalidHourResult.isInvalid)
        assertTrue(
            (invalidHourResult as ValidationResult.Invalid).errors.contains(
                BirthdayValidator.ERROR_INVALID_NOTIFICATION_HOUR,
            ),
        )

        // Invalid minute
        val invalidMinuteResult =
            validator.validateBirthday(
                name = "John Doe",
                birthDate = LocalDate.now().minusYears(25),
                notificationMinute = 65,
            )
        assertTrue(invalidMinuteResult.isInvalid)
        assertTrue(
            (invalidMinuteResult as ValidationResult.Invalid).errors.contains(
                BirthdayValidator.ERROR_INVALID_NOTIFICATION_MINUTE,
            ),
        )
    }
}
