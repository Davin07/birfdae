package com.birthdayreminder.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.data.local.database.AppDatabase
import com.birthdayreminder.data.local.entity.Birthday
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class BirthdayDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var birthdayDao: BirthdayDao

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java,
            ).allowMainThreadQueries().build()

        birthdayDao = database.birthdayDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertBirthday_insertsAndReturnsId() =
        runTest {
            // Given
            val birthday = createTestBirthday("John Doe", LocalDate.of(1990, 5, 15))

            // When
            val birthdayId = birthdayDao.insertBirthday(birthday)

            // Then
            assertTrue(birthdayId > 0)

            val allBirthdays = birthdayDao.getAllBirthdays().first()
            assertEquals(1, allBirthdays.size)
            assertEquals("John Doe", allBirthdays[0].name)
        }

    @Test
    fun getAllBirthdays_returnsAllInsertedBirthdays() =
        runTest {
            // Given
            val birthday1 = createTestBirthday("Alice", LocalDate.of(1985, 3, 10))
            val birthday2 = createTestBirthday("Bob", LocalDate.of(1992, 8, 20))
            val birthday3 = createTestBirthday("Charlie", LocalDate.of(1988, 12, 25))

            birthdayDao.insertBirthday(birthday1)
            birthdayDao.insertBirthday(birthday2)
            birthdayDao.insertBirthday(birthday3)

            // When
            val allBirthdays = birthdayDao.getAllBirthdays().first()

            // Then
            assertEquals(3, allBirthdays.size)

            val names = allBirthdays.map { it.name }
            assertTrue(names.contains("Alice"))
            assertTrue(names.contains("Bob"))
            assertTrue(names.contains("Charlie"))
        }

    @Test
    fun getBirthdayById_returnsCorrectBirthday() =
        runTest {
            // Given
            val birthday = createTestBirthday("David", LocalDate.of(1995, 6, 30))
            val birthdayId = birthdayDao.insertBirthday(birthday)

            // When
            val retrievedBirthday = birthdayDao.getBirthdayById(birthdayId)

            // Then
            assertNotNull(retrievedBirthday)
            assertEquals("David", retrievedBirthday?.name)
            assertEquals(LocalDate.of(1995, 6, 30), retrievedBirthday?.birthDate)
            assertEquals(birthdayId, retrievedBirthday?.id)
        }

    @Test
    fun getBirthdayById_returnsNullForNonExistentId() =
        runTest {
            // When
            val retrievedBirthday = birthdayDao.getBirthdayById(999L)

            // Then
            assertNull(retrievedBirthday)
        }

    @Test
    fun updateBirthday_updatesExistingBirthday() =
        runTest {
            // Given
            val birthday = createTestBirthday("Eve", LocalDate.of(1987, 9, 15))
            val birthdayId = birthdayDao.insertBirthday(birthday)

            val updatedBirthday =
                birthday.copy(
                    id = birthdayId,
                    name = "Eve Updated",
                    notes = "Updated notes",
                    notificationsEnabled = false,
                    advanceNotificationDays = 7,
                )

            // When
            birthdayDao.updateBirthday(updatedBirthday)

            // Then
            val retrievedBirthday = birthdayDao.getBirthdayById(birthdayId)
            assertEquals("Eve Updated", retrievedBirthday?.name)
            assertEquals("Updated notes", retrievedBirthday?.notes)
            assertFalse(retrievedBirthday?.notificationsEnabled ?: true)
            assertEquals(7, retrievedBirthday?.advanceNotificationDays)
            assertEquals(LocalDate.of(1987, 9, 15), retrievedBirthday?.birthDate)
        }

    @Test
    fun deleteBirthday_removesBirthdayFromDatabase() =
        runTest {
            // Given
            val birthday = createTestBirthday("Frank", LocalDate.of(1993, 11, 8))
            val birthdayId = birthdayDao.insertBirthday(birthday)
            val savedBirthday = birthdayDao.getBirthdayById(birthdayId)!!

            // When
            birthdayDao.deleteBirthday(savedBirthday)

            // Then
            val retrievedBirthday = birthdayDao.getBirthdayById(birthdayId)
            assertNull(retrievedBirthday)

            val allBirthdays = birthdayDao.getAllBirthdays().first()
            assertTrue(allBirthdays.isEmpty())
        }

    @Test
    fun deleteBirthdayById_removesBirthdayFromDatabase() =
        runTest {
            // Given
            val birthday = createTestBirthday("Grace", LocalDate.of(1991, 4, 12))
            val birthdayId = birthdayDao.insertBirthday(birthday)

            // When
            birthdayDao.deleteBirthdayById(birthdayId)

            // Then
            val retrievedBirthday = birthdayDao.getBirthdayById(birthdayId)
            assertNull(retrievedBirthday)

            val allBirthdays = birthdayDao.getAllBirthdays().first()
            assertTrue(allBirthdays.isEmpty())
        }

    @Test
    fun getBirthdayCount_returnsCorrectCount() =
        runTest {
            // Given - Initially empty
            assertEquals(0, birthdayDao.getBirthdayCount())

            // When - Add birthdays
            birthdayDao.insertBirthday(createTestBirthday("Henry", LocalDate.of(1989, 7, 22)))
            assertEquals(1, birthdayDao.getBirthdayCount())

            birthdayDao.insertBirthday(createTestBirthday("Ivy", LocalDate.of(1994, 10, 3)))
            assertEquals(2, birthdayDao.getBirthdayCount())

            // When - Delete one
            val allBirthdays = birthdayDao.getAllBirthdays().first()
            birthdayDao.deleteBirthday(allBirthdays[0])

            // Then
            assertEquals(1, birthdayDao.getBirthdayCount())
        }

    @Test
    fun searchBirthdaysByName_returnsMatchingBirthdays() =
        runTest {
            // Given
            birthdayDao.insertBirthday(createTestBirthday("Alice Johnson", LocalDate.of(1985, 3, 10)))
            birthdayDao.insertBirthday(createTestBirthday("Bob Smith", LocalDate.of(1992, 8, 20)))
            birthdayDao.insertBirthday(createTestBirthday("Alice Brown", LocalDate.of(1988, 12, 5)))
            birthdayDao.insertBirthday(createTestBirthday("Charlie Alice", LocalDate.of(1990, 6, 15)))

            // When
            val searchResults = birthdayDao.searchBirthdaysByName("Alice").first()

            // Then
            assertEquals(3, searchResults.size)
            assertTrue(searchResults.all { it.name.contains("Alice", ignoreCase = true) })

            val names = searchResults.map { it.name }
            assertTrue(names.contains("Alice Johnson"))
            assertTrue(names.contains("Alice Brown"))
            assertTrue(names.contains("Charlie Alice"))
        }

    @Test
    fun searchBirthdaysByName_isCaseInsensitive() =
        runTest {
            // Given
            birthdayDao.insertBirthday(createTestBirthday("alice johnson", LocalDate.of(1985, 3, 10)))
            birthdayDao.insertBirthday(createTestBirthday("ALICE BROWN", LocalDate.of(1988, 12, 5)))
            birthdayDao.insertBirthday(createTestBirthday("Bob Smith", LocalDate.of(1992, 8, 20)))

            // When
            val searchResults = birthdayDao.searchBirthdaysByName("ALICE").first()

            // Then
            assertEquals(2, searchResults.size)
            assertTrue(searchResults.any { it.name == "alice johnson" })
            assertTrue(searchResults.any { it.name == "ALICE BROWN" })
        }

    @Test
    fun getBirthdaysWithNotificationsEnabled_returnsOnlyEnabledBirthdays() =
        runTest {
            // Given
            birthdayDao.insertBirthday(
                createTestBirthday(
                    name = "Enabled 1",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notificationsEnabled = true,
                ),
            )
            birthdayDao.insertBirthday(
                createTestBirthday(
                    name = "Disabled 1",
                    birthDate = LocalDate.of(1990, 2, 2),
                    notificationsEnabled = false,
                ),
            )
            birthdayDao.insertBirthday(
                createTestBirthday(
                    name = "Enabled 2",
                    birthDate = LocalDate.of(1990, 3, 3),
                    notificationsEnabled = true,
                ),
            )
            birthdayDao.insertBirthday(
                createTestBirthday(
                    name = "Disabled 2",
                    birthDate = LocalDate.of(1990, 4, 4),
                    notificationsEnabled = false,
                ),
            )

            // When
            val enabledBirthdays = birthdayDao.getBirthdaysWithNotificationsEnabled().first()

            // Then
            assertEquals(2, enabledBirthdays.size)
            assertTrue(enabledBirthdays.all { it.notificationsEnabled })

            val names = enabledBirthdays.map { it.name }
            assertTrue(names.contains("Enabled 1"))
            assertTrue(names.contains("Enabled 2"))
        }

    @Test
    fun getBirthdaysForMonth_returnsCorrectBirthdays() =
        runTest {
            // Given
            birthdayDao.insertBirthday(createTestBirthday("January Baby 1", LocalDate.of(1990, 1, 5)))
            birthdayDao.insertBirthday(createTestBirthday("January Baby 2", LocalDate.of(1985, 1, 25)))
            birthdayDao.insertBirthday(createTestBirthday("June Baby", LocalDate.of(1990, 6, 20)))
            birthdayDao.insertBirthday(createTestBirthday("December Baby", LocalDate.of(1992, 12, 31)))

            // When
            val januaryBirthdays = birthdayDao.getBirthdaysForMonth("01").first()
            val juneBirthdays = birthdayDao.getBirthdaysForMonth("06").first()

            // Then
            assertEquals(2, januaryBirthdays.size)
            assertTrue(januaryBirthdays.all { it.birthDate.monthValue == 1 })

            assertEquals(1, juneBirthdays.size)
            assertEquals("June Baby", juneBirthdays[0].name)
        }

    @Test
    fun getBirthdaysForDate_returnsCorrectBirthdays() =
        runTest {
            // Given
            birthdayDao.insertBirthday(createTestBirthday("Same Date 1", LocalDate.of(1990, 6, 15)))
            birthdayDao.insertBirthday(createTestBirthday("Same Date 2", LocalDate.of(1985, 6, 15)))
            birthdayDao.insertBirthday(createTestBirthday("Different Month", LocalDate.of(1990, 7, 15)))
            birthdayDao.insertBirthday(createTestBirthday("Different Day", LocalDate.of(1990, 6, 20)))

            // When
            val sameDateBirthdays = birthdayDao.getBirthdaysForDate("06-15").first()

            // Then
            assertEquals(2, sameDateBirthdays.size)
            assertTrue(
                sameDateBirthdays.all {
                    it.birthDate.monthValue == 6 && it.birthDate.dayOfMonth == 15
                },
            )

            val names = sameDateBirthdays.map { it.name }
            assertTrue(names.contains("Same Date 1"))
            assertTrue(names.contains("Same Date 2"))
        }

    @Test
    fun flowUpdates_emitNewValuesOnDataChange() =
        runTest {
            // Given
            val birthday = createTestBirthday("Flow Test", LocalDate.of(1990, 1, 1))

            // When - Insert birthday
            val birthdayId = birthdayDao.insertBirthday(birthday)

            // Then - Flow should emit updated list
            val allBirthdays = birthdayDao.getAllBirthdays().first()
            assertEquals(1, allBirthdays.size)
            assertEquals("Flow Test", allBirthdays[0].name)

            // When - Update birthday
            val updatedBirthday = allBirthdays[0].copy(name = "Flow Test Updated")
            birthdayDao.updateBirthday(updatedBirthday)

            // Then - Flow should emit updated data
            val updatedBirthdays = birthdayDao.getAllBirthdays().first()
            assertEquals(1, updatedBirthdays.size)
            assertEquals("Flow Test Updated", updatedBirthdays[0].name)

            // When - Delete birthday
            birthdayDao.deleteBirthdayById(birthdayId)

            // Then - Flow should emit empty list
            val finalBirthdays = birthdayDao.getAllBirthdays().first()
            assertTrue(finalBirthdays.isEmpty())
        }

    @Test
    fun concurrentOperations_maintainDataIntegrity() =
        runTest {
            // Given - Multiple birthdays
            val birthdays =
                (1..10).map { i ->
                    createTestBirthday("User $i", LocalDate.of(1990, i % 12 + 1, i % 28 + 1))
                }

            // When - Insert all birthdays
            val insertedIds = birthdays.map { birthdayDao.insertBirthday(it) }

            // Then - All should be inserted
            assertEquals(10, birthdayDao.getBirthdayCount())

            // When - Update some birthdays
            val allBirthdays = birthdayDao.getAllBirthdays().first()
            val toUpdate = allBirthdays.take(3).map { it.copy(notes = "Updated") }
            toUpdate.forEach { birthdayDao.updateBirthday(it) }

            // When - Delete some birthdays
            val toDelete = allBirthdays.drop(7)
            toDelete.forEach { birthdayDao.deleteBirthday(it) }

            // Then - Verify final state
            val finalCount = birthdayDao.getBirthdayCount()
            assertEquals(7, finalCount) // 10 - 3 deleted

            val finalBirthdays = birthdayDao.getAllBirthdays().first()
            val updatedBirthdays = finalBirthdays.filter { it.notes == "Updated" }
            assertEquals(3, updatedBirthdays.size)
        }

    private fun createTestBirthday(
        name: String,
        birthDate: LocalDate,
        notes: String? = null,
        notificationsEnabled: Boolean = true,
        advanceNotificationDays: Int = 0,
    ): Birthday {
        return Birthday(
            name = name,
            birthDate = birthDate,
            notes = notes,
            notificationsEnabled = notificationsEnabled,
            advanceNotificationDays = advanceNotificationDays,
            createdAt = LocalDateTime.now(),
        )
    }
}
