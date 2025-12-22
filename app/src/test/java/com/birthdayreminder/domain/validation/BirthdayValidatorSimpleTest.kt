package com.birthdayreminder.domain.validation

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import java.time.LocalDate

class BirthdayValidatorSimpleTest {

    private val validator = BirthdayValidator()

    @Test
    fun `validateNotificationHour returns null for valid hours`() {
        // Test valid hours: 0-23
        for (hour in 0..23) {
            assertNull(validator.validateNotificationHour(hour))
        }
    }

    @Test
    fun `validateNotificationHour returns error for invalid hours`() {
        // Test hours below minimum
        assertEquals(
            BirthdayValidator.ERROR_INVALID_NOTIFICATION_HOUR,
            validator.validateNotificationHour(-1)
        )

        // Test hours above maximum
        assertEquals(
            BirthdayValidator.ERROR_INVALID_NOTIFICATION_HOUR,
            validator.validateNotificationHour(24)
        )
    }

    @Test
    fun `validateNotificationHour returns null for null value`() {
        assertNull(validator.validateNotificationHour(null))
    }

    @Test
    fun `validateNotificationMinute returns null for valid minutes`() {
        // Test valid minutes: 0-59
        for (minute in 0..59) {
            assertNull(validator.validateNotificationMinute(minute))
        }
    }

    @Test
    fun `validateNotificationMinute returns error for invalid minutes`() {
        // Test minutes below minimum
        assertEquals(
            BirthdayValidator.ERROR_INVALID_NOTIFICATION_MINUTE,
            validator.validateNotificationMinute(-1)
        )

        // Test minutes above maximum
        assertEquals(
            BirthdayValidator.ERROR_INVALID_NOTIFICATION_MINUTE,
            validator.validateNotificationMinute(60)
        )
    }

    @Test
    fun `validateNotificationMinute returns null for null value`() {
        assertNull(validator.validateNotificationMinute(null))
    }
}