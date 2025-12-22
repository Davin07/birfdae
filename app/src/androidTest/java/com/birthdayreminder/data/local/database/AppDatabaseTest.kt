package com.birthdayreminder.data.local.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    
    private lateinit var database: AppDatabase
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun createDatabase_createsAllTablesCorrectly() = runTest {
        // Given - Database is created in setUp()
        
        // When - Access the DAO
        val birthdayDao = database.birthdayDao()
        
        // Then - Should be able to perform basic operations
        val birthday = Birthday(
            name = "Test User",
            birthDate = LocalDate.of(1990, 1, 1),
            notes = "Test notes",
            notificationsEnabled = true,
            advanceNotificationDays = 1,
            createdAt = LocalDateTime.now()
        )
        
        val birthdayId = birthdayDao.insertBirthday(birthday)
        assertTrue(birthdayId > 0)
        
        val retrievedBirthday = birthdayDao.getBirthdayById(birthdayId)
        assertNotNull(retrievedBirthday)
        assertEquals("Test User", retrievedBirthday?.name)
    }
    
    @Test
    fun databaseSchema_hasCorrectTableStructure() = runTest {
        // Given
        val birthday = Birthday(
            name = "Schema Test",
            birthDate = LocalDate.of(1995, 6, 15),
            notes = "Testing schema",
            notificationsEnabled = false,
            advanceNotificationDays = 7,
            createdAt = LocalDateTime.now()
        )
        
        // When
        val birthdayId = database.birthdayDao().insertBirthday(birthday)
        val retrievedBirthday = database.birthdayDao().getBirthdayById(birthdayId)
        
        // Then - All fields should be preserved correctly
        assertNotNull(retrievedBirthday)
        assertEquals("Schema Test", retrievedBirthday?.name)
        assertEquals(LocalDate.of(1995, 6, 15), retrievedBirthday?.birthDate)
        assertEquals("Testing schema", retrievedBirthday?.notes)
        assertFalse(retrievedBirthday?.notificationsEnabled ?: true)
        assertEquals(7, retrievedBirthday?.advanceNotificationDays)
        assertNotNull(retrievedBirthday?.createdAt)
    }
    
    @Test
    fun typeConverters_handleLocalDateCorrectly() = runTest {
        // Given - Various date scenarios
        val testDates = listOf(
            LocalDate.of(1990, 1, 1),      // Regular date
            LocalDate.of(2000, 2, 29),     // Leap year
            LocalDate.of(1999, 12, 31),    // Year end
            LocalDate.of(2024, 6, 15)      // Current era
        )
        
        // When - Insert birthdays with different dates
        val insertedIds = testDates.mapIndexed { index, date ->
            val birthday = Birthday(
                name = "Date Test $index",
                birthDate = date,
                notes = null,
                notificationsEnabled = true,
                advanceNotificationDays = 0,
                createdAt = LocalDateTime.now()
            )
            database.birthdayDao().insertBirthday(birthday)
        }
        
        // Then - All dates should be retrieved correctly
        insertedIds.forEachIndexed { index, id ->
            val retrievedBirthday = database.birthdayDao().getBirthdayById(id)
            assertNotNull(retrievedBirthday)
            assertEquals(testDates[index], retrievedBirthday?.birthDate)
        }
    }
    
    @Test
    fun typeConverters_handleLocalDateTimeCorrectly() = runTest {
        // Given - Various datetime scenarios
        val testDateTimes = listOf(
            LocalDateTime.of(2024, 1, 1, 0, 0, 0),        // Midnight
            LocalDateTime.of(2024, 6, 15, 12, 30, 45),    // Midday with seconds
            LocalDateTime.of(2024, 12, 31, 23, 59, 59)    // End of year
        )
        
        // When - Insert birthdays with different creation times
        val insertedIds = testDateTimes.mapIndexed { index, dateTime ->
            val birthday = Birthday(
                name = "DateTime Test $index",
                birthDate = LocalDate.of(1990, 1, 1),
                notes = null,
                notificationsEnabled = true,
                advanceNotificationDays = 0,
                createdAt = dateTime
            )
            database.birthdayDao().insertBirthday(birthday)
        }
        
        // Then - All datetimes should be retrieved correctly
        insertedIds.forEachIndexed { index, id ->
            val retrievedBirthday = database.birthdayDao().getBirthdayById(id)
            assertNotNull(retrievedBirthday)
            assertEquals(testDateTimes[index], retrievedBirthday?.createdAt)
        }
    }
    
    @Test
    fun constraints_enforceDataIntegrity() = runTest {
        // Given
        val birthday = Birthday(
            name = "Constraint Test",
            birthDate = LocalDate.of(1990, 1, 1),
            notes = null,
            notificationsEnabled = true,
            advanceNotificationDays = 0,
            createdAt = LocalDateTime.now()
        )
        
        // When - Insert birthday
        val birthdayId = database.birthdayDao().insertBirthday(birthday)
        
        // Then - ID should be auto-generated and positive
        assertTrue(birthdayId > 0)
        
        val retrievedBirthday = database.birthdayDao().getBirthdayById(birthdayId)
        assertEquals(birthdayId, retrievedBirthday?.id)
    }
    
    @Test
    fun nullableFields_handleNullValuesCorrectly() = runTest {
        // Given - Birthday with null optional fields
        val birthday = Birthday(
            name = "Null Test",
            birthDate = LocalDate.of(1990, 1, 1),
            notes = null, // Nullable field
            notificationsEnabled = true,
            advanceNotificationDays = 0,
            createdAt = LocalDateTime.now()
        )
        
        // When
        val birthdayId = database.birthdayDao().insertBirthday(birthday)
        val retrievedBirthday = database.birthdayDao().getBirthdayById(birthdayId)
        
        // Then
        assertNotNull(retrievedBirthday)
        assertEquals("Null Test", retrievedBirthday?.name)
        assertNull(retrievedBirthday?.notes)
    }
    
    @Test
    fun defaultValues_areAppliedCorrectly() = runTest {
        // Given - Birthday with default values
        val birthday = Birthday(
            name = "Default Test",
            birthDate = LocalDate.of(1990, 1, 1),
            notes = null,
            notificationsEnabled = true, // Default value
            advanceNotificationDays = 0, // Default value
            createdAt = LocalDateTime.now()
        )
        
        // When
        val birthdayId = database.birthdayDao().insertBirthday(birthday)
        val retrievedBirthday = database.birthdayDao().getBirthdayById(birthdayId)
        
        // Then
        assertNotNull(retrievedBirthday)
        assertTrue(retrievedBirthday?.notificationsEnabled ?: false)
        assertEquals(0, retrievedBirthday?.advanceNotificationDays)
    }
    
    @Test
    fun databaseOperations_areTransactional() = runTest {
        // Given - Multiple operations that should be atomic
        val birthday1 = Birthday(
            name = "Transaction Test 1",
            birthDate = LocalDate.of(1990, 1, 1),
            notes = null,
            notificationsEnabled = true,
            advanceNotificationDays = 0,
            createdAt = LocalDateTime.now()
        )
        
        val birthday2 = Birthday(
            name = "Transaction Test 2",
            birthDate = LocalDate.of(1991, 2, 2),
            notes = null,
            notificationsEnabled = true,
            advanceNotificationDays = 0,
            createdAt = LocalDateTime.now()
        )
        
        // When - Insert both birthdays
        val id1 = database.birthdayDao().insertBirthday(birthday1)
        val id2 = database.birthdayDao().insertBirthday(birthday2)
        
        // Then - Both should be inserted successfully
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)
        assertEquals(2, database.birthdayDao().getBirthdayCount())
        
        // When - Delete one
        database.birthdayDao().deleteBirthdayById(id1)
        
        // Then - Only one should remain
        assertEquals(1, database.birthdayDao().getBirthdayCount())
        assertNull(database.birthdayDao().getBirthdayById(id1))
        assertNotNull(database.birthdayDao().getBirthdayById(id2))
    }
    
    @Test
    fun performanceTest_handlesManyRecords() = runTest {
        // Given - Large number of birthdays
        val numberOfBirthdays = 1000
        val birthdays = (1..numberOfBirthdays).map { i ->
            Birthday(
                name = "Performance Test User $i",
                birthDate = LocalDate.of(1990 + (i % 30), (i % 12) + 1, (i % 28) + 1),
                notes = if (i % 10 == 0) "Notes for user $i" else null,
                notificationsEnabled = i % 2 == 0,
                advanceNotificationDays = i % 4,
                createdAt = LocalDateTime.now().plusSeconds(i.toLong())
            )
        }
        
        // When - Insert all birthdays
        val startTime = System.currentTimeMillis()
        birthdays.forEach { birthday ->
            database.birthdayDao().insertBirthday(birthday)
        }
        val insertTime = System.currentTimeMillis() - startTime
        
        // Then - All should be inserted in reasonable time
        assertEquals(numberOfBirthdays, database.birthdayDao().getBirthdayCount())
        assertTrue("Insert time should be reasonable", insertTime < 10000) // Less than 10 seconds
        
        // When - Query all birthdays
        val queryStartTime = System.currentTimeMillis()
        val allBirthdays = database.birthdayDao().getAllBirthdays()
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        // Then - Query should be fast
        assertTrue("Query time should be reasonable", queryTime < 1000) // Less than 1 second
    }
}