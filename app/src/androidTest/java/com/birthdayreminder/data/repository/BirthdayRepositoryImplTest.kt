package com.birthdayreminder.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.database.AppDatabase
import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class BirthdayRepositoryImplTest {
    
    private lateinit var database: AppDatabase
    private lateinit var birthdayDao: BirthdayDao
    private lateinit var repository: BirthdayRepositoryImpl
    
    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        birthdayDao = database.birthdayDao()
        repository = BirthdayRepositoryImpl(birthdayDao)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun addBirthday_insertsAndReturnsId() = runTest {
        // Given
        val birthday = createTestBirthday("John Doe", LocalDate.of(1990, 5, 15))
        
        // When
        val birthdayId = repository.addBirthday(birthday)
        
        // Then
        assertTrue(birthdayId > 0)
        
        val savedBirthday = repository.getBirthdayById(birthdayId)
        assertNotNull(savedBirthday)
        assertEquals("John Doe", savedBirthday?.name)
        assertEquals(LocalDate.of(1990, 5, 15), savedBirthday?.birthDate)
    }
    
    @Test
    fun getAllBirthdays_returnsAllBirthdays() = runTest {
        // Given
        val birthday1 = createTestBirthday("Alice", LocalDate.of(1985, 3, 10))
        val birthday2 = createTestBirthday("Bob", LocalDate.of(1992, 8, 20))
        
        repository.addBirthday(birthday1)
        repository.addBirthday(birthday2)
        
        // When
        val birthdays = repository.getAllBirthdays().first()
        
        // Then
        assertEquals(2, birthdays.size)
        assertTrue(birthdays.any { it.name == "Alice" })
        assertTrue(birthdays.any { it.name == "Bob" })
    }
    
    @Test
    fun getBirthdayById_returnsCorrectBirthday() = runTest {
        // Given
        val birthday = createTestBirthday("Charlie", LocalDate.of(1988, 12, 25))
        val birthdayId = repository.addBirthday(birthday)
        
        // When
        val retrievedBirthday = repository.getBirthdayById(birthdayId)
        
        // Then
        assertNotNull(retrievedBirthday)
        assertEquals("Charlie", retrievedBirthday?.name)
        assertEquals(LocalDate.of(1988, 12, 25), retrievedBirthday?.birthDate)
    }
    
    @Test
    fun getBirthdayById_returnsNullForNonExistentId() = runTest {
        // When
        val retrievedBirthday = repository.getBirthdayById(999L)
        
        // Then
        assertNull(retrievedBirthday)
    }
    
    @Test
    fun updateBirthday_updatesExistingBirthday() = runTest {
        // Given
        val birthday = createTestBirthday("David", LocalDate.of(1995, 6, 30))
        val birthdayId = repository.addBirthday(birthday)
        
        val updatedBirthday = birthday.copy(
            id = birthdayId,
            name = "David Updated",
            notes = "Updated notes"
        )
        
        // When
        repository.updateBirthday(updatedBirthday)
        
        // Then
        val retrievedBirthday = repository.getBirthdayById(birthdayId)
        assertEquals("David Updated", retrievedBirthday?.name)
        assertEquals("Updated notes", retrievedBirthday?.notes)
        assertEquals(LocalDate.of(1995, 6, 30), retrievedBirthday?.birthDate)
    }
    
    @Test
    fun deleteBirthday_removesBirthdayFromDatabase() = runTest {
        // Given
        val birthday = createTestBirthday("Eve", LocalDate.of(1987, 9, 15))
        val birthdayId = repository.addBirthday(birthday)
        val savedBirthday = repository.getBirthdayById(birthdayId)!!
        
        // When
        repository.deleteBirthday(savedBirthday)
        
        // Then
        val retrievedBirthday = repository.getBirthdayById(birthdayId)
        assertNull(retrievedBirthday)
        
        val allBirthdays = repository.getAllBirthdays().first()
        assertFalse(allBirthdays.any { it.id == birthdayId })
    }
    
    @Test
    fun deleteBirthdayById_removesBirthdayFromDatabase() = runTest {
        // Given
        val birthday = createTestBirthday("Frank", LocalDate.of(1993, 11, 8))
        val birthdayId = repository.addBirthday(birthday)
        
        // When
        repository.deleteBirthdayById(birthdayId)
        
        // Then
        val retrievedBirthday = repository.getBirthdayById(birthdayId)
        assertNull(retrievedBirthday)
    }
    
    @Test
    fun getBirthdayCount_returnsCorrectCount() = runTest {
        // Given
        assertEquals(0, repository.getBirthdayCount())
        
        repository.addBirthday(createTestBirthday("Grace", LocalDate.of(1991, 4, 12)))
        repository.addBirthday(createTestBirthday("Henry", LocalDate.of(1989, 7, 22)))
        
        // When
        val count = repository.getBirthdayCount()
        
        // Then
        assertEquals(2, count)
    }
    
    @Test
    fun searchBirthdaysByName_returnsMatchingBirthdays() = runTest {
        // Given
        repository.addBirthday(createTestBirthday("Alice Johnson", LocalDate.of(1985, 3, 10)))
        repository.addBirthday(createTestBirthday("Bob Smith", LocalDate.of(1992, 8, 20)))
        repository.addBirthday(createTestBirthday("Alice Brown", LocalDate.of(1988, 12, 5)))
        
        // When
        val searchResults = repository.searchBirthdaysByName("Alice").first()
        
        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.name.contains("Alice") })
    }
    
    @Test
    fun getBirthdaysWithNotificationsEnabled_returnsOnlyEnabledBirthdays() = runTest {
        // Given
        repository.addBirthday(createTestBirthday("Enabled User", LocalDate.of(1990, 1, 1), notificationsEnabled = true))
        repository.addBirthday(createTestBirthday("Disabled User", LocalDate.of(1990, 1, 1), notificationsEnabled = false))
        
        // When
        val enabledBirthdays = repository.getBirthdaysWithNotificationsEnabled().first()
        
        // Then
        assertEquals(1, enabledBirthdays.size)
        assertEquals("Enabled User", enabledBirthdays[0].name)
        assertTrue(enabledBirthdays[0].notificationsEnabled)
    }
    
    @Test
    fun getBirthdaysForMonth_returnsCorrectBirthdays() = runTest {
        // Given
        repository.addBirthday(createTestBirthday("January Baby", LocalDate.of(1990, 1, 15)))
        repository.addBirthday(createTestBirthday("June Baby", LocalDate.of(1990, 6, 20)))
        repository.addBirthday(createTestBirthday("Another June Baby", LocalDate.of(1985, 6, 5)))
        
        // When
        val juneBirthdays = repository.getBirthdaysForMonth("06").first()
        
        // Then
        assertEquals(2, juneBirthdays.size)
        assertTrue(juneBirthdays.all { it.birthDate.monthValue == 6 })
    }
    
    @Test
    fun getBirthdaysForDate_returnsCorrectBirthdays() = runTest {
        // Given
        repository.addBirthday(createTestBirthday("Same Date 1", LocalDate.of(1990, 6, 15)))
        repository.addBirthday(createTestBirthday("Same Date 2", LocalDate.of(1985, 6, 15)))
        repository.addBirthday(createTestBirthday("Different Date", LocalDate.of(1990, 6, 20)))
        
        // When
        val sameDateBirthdays = repository.getBirthdaysForDate("06-15").first()
        
        // Then
        assertEquals(2, sameDateBirthdays.size)
        assertTrue(sameDateBirthdays.all { 
            it.birthDate.monthValue == 6 && it.birthDate.dayOfMonth == 15 
        })
    }
    
    @Test
    fun dataIntegrity_maintainsConsistencyAcrossOperations() = runTest {
        // Given - Add multiple birthdays
        val birthday1 = createTestBirthday("Test User 1", LocalDate.of(1990, 1, 1))
        val birthday2 = createTestBirthday("Test User 2", LocalDate.of(1991, 2, 2))
        val birthday3 = createTestBirthday("Test User 3", LocalDate.of(1992, 3, 3))
        
        val id1 = repository.addBirthday(birthday1)
        val id2 = repository.addBirthday(birthday2)
        val id3 = repository.addBirthday(birthday3)
        
        // When - Perform various operations
        assertEquals(3, repository.getBirthdayCount())
        
        // Update one birthday
        val updatedBirthday = birthday1.copy(id = id1, name = "Updated User 1")
        repository.updateBirthday(updatedBirthday)
        
        // Delete one birthday
        repository.deleteBirthdayById(id2)
        
        // Then - Verify final state
        assertEquals(2, repository.getBirthdayCount())
        
        val remainingBirthdays = repository.getAllBirthdays().first()
        assertEquals(2, remainingBirthdays.size)
        
        val updatedUser = repository.getBirthdayById(id1)
        assertEquals("Updated User 1", updatedUser?.name)
        
        val deletedUser = repository.getBirthdayById(id2)
        assertNull(deletedUser)
        
        val unchangedUser = repository.getBirthdayById(id3)
        assertEquals("Test User 3", unchangedUser?.name)
    }
    
    private fun createTestBirthday(
        name: String,
        birthDate: LocalDate,
        notes: String? = null,
        notificationsEnabled: Boolean = true,
        advanceNotificationDays: Int = 0
    ): Birthday {
        return Birthday(
            name = name,
            birthDate = birthDate,
            notes = notes,
            notificationsEnabled = notificationsEnabled,
            advanceNotificationDays = advanceNotificationDays,
            createdAt = LocalDateTime.now()
        )
    }
}