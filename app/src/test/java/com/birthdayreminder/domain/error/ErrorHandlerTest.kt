package com.birthdayreminder.domain.error

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.DateTimeException

class ErrorHandlerTest {
    private val errorHandler = ErrorHandler()

    @Test
    fun `handleDatabaseError returns specific message for locked database`() =
        runTest {
            val exception = Exception("database is locked")
            val message = errorHandler.handleDatabaseError(exception)

            assertEquals("Database is busy. Please try again in a moment.", message)
        }

    @Test
    fun `handleDatabaseError returns specific message for no such table`() =
        runTest {
            val exception = Exception("no such table birthdays")
            val message = errorHandler.handleDatabaseError(exception)

            assertEquals("Database structure error. Please restart the app.", message)
        }

    @Test
    fun `handleDatabaseError returns specific message for disk I O error`() =
        runTest {
            val exception = Exception("disk I/O error")
            val message = errorHandler.handleDatabaseError(exception)

            assertEquals("Storage error. Please check available space and try again.", message)
        }

    @Test
    fun `handleDatabaseError returns correct message for IOException`() =
        runTest {
            val exception = IOException("disk full")
            val message = errorHandler.handleDatabaseError(exception)

            assertEquals("Storage access error. Please check permissions and try again.", message)
        }

    @Test
    fun `handleDateCalculationError returns correct message for DateTimeException`() =
        runTest {
            val exception = DateTimeException("invalid date")
            val message = errorHandler.handleDateCalculationError(exception)

            assertEquals(ErrorHandler.ERROR_DATE_INVALID, message)
        }

    @Test
    fun `handleDateCalculationError returns fallback for generic Exception`() =
        runTest {
            val exception = ArithmeticException("overflow")
            val message = errorHandler.handleDateCalculationError(exception)

            assertEquals("Date calculation overflow. Please check the date range.", message)
        }

    @Test
    fun `isRecoverableError returns true for locked database`() =
        runTest {
            val exception = Exception("database is locked")

            assertTrue(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `isRecoverableError returns true for disk I O error`() =
        runTest {
            val exception = Exception("disk I/O error")

            assertTrue(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `isRecoverableError returns false for OutOfMemoryError`() =
        runTest {
            val exception = OutOfMemoryError()

            assertFalse(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `isRecoverableError returns false for SecurityException`() =
        runTest {
            val exception = SecurityException("no permission")

            assertFalse(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `isRecoverableError returns false for IllegalArgumentException`() =
        runTest {
            val exception = IllegalArgumentException("invalid input")

            assertFalse(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `isRecoverableError returns true for generic Exception`() =
        runTest {
            val exception = Exception("unknown error")

            assertTrue(errorHandler.isRecoverableError(exception))
        }

    @Test
    fun `createErrorResult creates correct ErrorResult for database error`() =
        runTest {
            val exception = Exception("database is locked")

            val errorResult = errorHandler.createErrorResult(exception, "test operation")

            assertTrue(errorResult.isRecoverable)
            assertTrue(errorResult.message.contains("busy"))
            assertEquals(exception, errorResult.originalException)
        }

    @Test
    fun `createErrorResult creates correct ErrorResult for non-recoverable error`() =
        runTest {
            val exception = OutOfMemoryError()

            val errorResult = errorHandler.createErrorResult(exception, "test operation")

            assertFalse(errorResult.isRecoverable)
            assertTrue(errorResult.message.contains("memory"))
            assertEquals(exception, errorResult.originalException)
        }

    @Test
    fun `createErrorResult includes operation name in message`() =
        runTest {
            val exception = IllegalArgumentException("test")

            val errorResult = errorHandler.createErrorResult(exception, "load birthdays")

            assertTrue(errorResult.message.contains("load birthdays"))
        }

    @Test
    fun `createErrorResult handles date calculation error type`() =
        runTest {
            val exception = DateTimeException("invalid date")

            val errorResult = errorHandler.createErrorResult(exception, "calculate age")

            assertFalse(errorResult.isRecoverable)
            assertEquals(ErrorHandler.ERROR_DATE_INVALID, errorResult.message)
        }

    @Test
    fun `ErrorResult canRetry returns recoverable value`() =
        runTest {
            val exception = Exception("database is locked")

            val errorResult = errorHandler.createErrorResult(exception, "test")

            assertTrue(errorResult.canRetry)
        }

    @Test
    fun `ErrorResult canRetry returns false for non-recoverable`() =
        runTest {
            val exception = OutOfMemoryError()

            val errorResult = errorHandler.createErrorResult(exception, "test")

            assertFalse(errorResult.canRetry)
        }

    @Test
    fun `ErrorResult technicalMessage returns original exception message`() =
        runTest {
            val exception = Exception("technical error message")
            val errorResult = errorHandler.createErrorResult(exception, "test")

            assertEquals("technical error message", errorResult.technicalMessage)
        }

    @Test
    fun `ErrorResult technicalMessage returns user message when original is null`() =
        runTest {
            val errorResult = ErrorResult("user message", true, null)

            assertEquals("user message", errorResult.technicalMessage)
        }
}
