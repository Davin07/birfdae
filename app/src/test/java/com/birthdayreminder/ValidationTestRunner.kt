package com.birthdayreminder

import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.util.SafeDateCalculator
import com.birthdayreminder.domain.validation.BirthdayValidator
import org.junit.Test
import java.time.LocalDate

/**
 * Simple test runner to verify our validation and error handling implementation.
 * This can be run to quickly check if the basic functionality works.
 */
class ValidationTestRunner {
    
    @Test
    fun `test basic validation functionality`() {
        val validator = BirthdayValidator()
        
        // Test valid input
        val validResult = validator.validateBirthday(
            name = "John Doe",
            birthDate = LocalDate.now().minusYears(25),
            notes = "Birthday notes",
            advanceNotificationDays = 1
        )
        assert(validResult.isValid) { "Valid input should pass validation" }
        
        // Test invalid input
        val invalidResult = validator.validateBirthday(
            name = "",
            birthDate = LocalDate.now().plusDays(1),
            notes = "a".repeat(501),
            advanceNotificationDays = 5
        )
        assert(invalidResult.isInvalid) { "Invalid input should fail validation" }
        assert(invalidResult.errorMessages.size == 4) { "Should have 4 validation errors" }
        
        println("✓ Validation tests passed")
    }
    
    @Test
    fun `test error handler functionality`() {
        val errorHandler = ErrorHandler()
        
        // Test database error handling
        val dbError = RuntimeException("Database connection failed")
        val userFriendlyMessage = errorHandler.handleDatabaseError(dbError)
        assert(userFriendlyMessage.isNotEmpty()) { "Should return user-friendly message" }
        assert(!userFriendlyMessage.contains("RuntimeException")) { "Should not contain technical details" }
        
        // Test recoverable error detection
        val recoverableError = java.io.IOException("Temporary file error")
        assert(errorHandler.isRecoverableError(recoverableError)) { "IOException should be recoverable" }
        
        val nonRecoverableError = OutOfMemoryError("Out of memory")
        assert(!errorHandler.isRecoverableError(nonRecoverableError)) { "OutOfMemoryError should not be recoverable" }
        
        println("✓ Error handler tests passed")
    }
    
    @Test
    fun `test safe date calculator functionality`() {
        val errorHandler = ErrorHandler()
        val calculator = SafeDateCalculator(errorHandler)
        
        // Test normal date calculation
        val birthDate = LocalDate.of(1990, 6, 15)
        val currentDate = LocalDate.of(2024, 3, 10)
        
        val nextOccurrenceResult = calculator.calculateNextOccurrence(birthDate, currentDate)
        assert(nextOccurrenceResult.isSuccess) { "Normal date calculation should succeed" }
        assert(nextOccurrenceResult.getValueOrFallback() == LocalDate.of(2024, 6, 15)) { "Should calculate correct next occurrence" }
        
        // Test leap year handling
        val leapYearBirthday = LocalDate.of(2000, 2, 29)
        val nonLeapYearDate = LocalDate.of(2023, 1, 15)
        
        val leapYearResult = calculator.calculateNextOccurrence(leapYearBirthday, nonLeapYearDate)
        assert(leapYearResult.isSuccess) { "Leap year calculation should succeed" }
        assert(leapYearResult.getValueOrFallback() == LocalDate.of(2023, 2, 28)) { "Should use Feb 28 for leap day in non-leap year" }
        
        // Test invalid date range
        val tooOldDate = LocalDate.of(1800, 6, 15)
        val invalidResult = calculator.calculateNextOccurrence(tooOldDate, currentDate)
        assert(invalidResult.isError) { "Invalid date range should return error" }
        
        println("✓ Safe date calculator tests passed")
    }
    
    @Test
    fun `test data sanitization`() {
        val validator = BirthdayValidator()
        
        // Test name sanitization
        val messyName = "  John   Doe  "
        val sanitizedName = validator.sanitizeName(messyName)
        assert(sanitizedName == "John Doe") { "Should normalize spaces in name" }
        
        // Test notes sanitization
        val messyNotes = "  Some notes  "
        val sanitizedNotes = validator.sanitizeNotes(messyNotes)
        assert(sanitizedNotes == "Some notes") { "Should trim notes" }
        
        val emptyNotes = "   "
        val sanitizedEmptyNotes = validator.sanitizeNotes(emptyNotes)
        assert(sanitizedEmptyNotes == null) { "Should return null for empty notes" }
        
        println("✓ Data sanitization tests passed")
    }
    
    @Test
    fun `test comprehensive validation scenarios`() {
        val validator = BirthdayValidator()
        
        // Test edge cases
        val scenarios = listOf(
            // Valid scenarios
            Triple("John Doe", LocalDate.now().minusYears(25), true),
            Triple("Mary-Jane O'Connor", LocalDate.now().minusYears(30), true),
            Triple("Dr. Smith", LocalDate.of(2000, 2, 29), true), // Leap year
            Triple("a".repeat(100), LocalDate.now().minusYears(1), true), // Max length name
            
            // Invalid scenarios
            Triple("", LocalDate.now().minusYears(25), false), // Empty name
            Triple("John Doe", LocalDate.now().plusDays(1), false), // Future date
            Triple("a".repeat(101), LocalDate.now().minusYears(25), false), // Name too long
            Triple("John@Doe", LocalDate.now().minusYears(25), false) // Invalid characters
        )
        
        scenarios.forEachIndexed { index, (name, birthDate, shouldBeValid) ->
            val result = validator.validateBirthday(name, birthDate)
            if (shouldBeValid) {
                assert(result.isValid) { "Scenario $index should be valid: $name, $birthDate" }
            } else {
                assert(result.isInvalid) { "Scenario $index should be invalid: $name, $birthDate" }
            }
        }
        
        println("✓ Comprehensive validation tests passed")
    }
    
    @Test
    fun `run all validation tests`() {
        println("Running validation and error handling tests...")
        
        `test basic validation functionality`()
        `test error handler functionality`()
        `test safe date calculator functionality`()
        `test data sanitization`()
        `test comprehensive validation scenarios`()
        
        println("✅ All validation and error handling tests passed!")
        println("Task 10: Add data validation and error handling - COMPLETED")
    }
}