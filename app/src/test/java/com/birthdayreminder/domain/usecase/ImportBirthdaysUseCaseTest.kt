package com.birthdayreminder.domain.usecase

import android.net.Uri
import com.birthdayreminder.data.backup.BackupDataSource
import com.birthdayreminder.data.backup.BackupFileDto
import com.birthdayreminder.data.backup.BirthdayBackupDto
import com.birthdayreminder.data.backup.ConflictStrategy
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.testutil.TestUris
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ImportBirthdaysUseCaseTest {
    private lateinit var repository: FakeBirthdayRepository
    private lateinit var backupDataSource: BackupDataSource
    private lateinit var scheduleNotificationUseCase: ScheduleNotificationUseCase
    private lateinit var cancelNotificationUseCase: CancelNotificationUseCase
    private lateinit var useCase: ImportBirthdaysUseCase

    /**
     * Uri stand-in; android.net.Uri can neither be instantiated nor mocked in plain JVM tests.
     */
    private val testUri: Uri = TestUris.fakeUri()

    @Before
    fun setup(): Unit =
        runTest {
            repository = FakeBirthdayRepository()
            backupDataSource = mock()
            scheduleNotificationUseCase = mock()
            cancelNotificationUseCase = mock()
            useCase =
                ImportBirthdaysUseCase(
                    birthdayRepository = repository,
                    backupDataSource = backupDataSource,
                    scheduleNotificationUseCase = scheduleNotificationUseCase,
                    cancelNotificationUseCase = cancelNotificationUseCase,
                )
            whenever(scheduleNotificationUseCase.scheduleNotification(any()))
                .thenReturn(ScheduleNotificationResult.Success)
            whenever(cancelNotificationUseCase.cancelNotification(any<Long>()))
                .thenReturn(CancelNotificationResult.Success)
        }

    @Test
    fun `skip strategy inserts only birthdays that do not exist locally`() =
        runTest {
            seedLocal(Birthday(name = "Alice", birthDate = LocalDate.of(1990, 5, 15)))
            stubRead(backupFile(entry("Alice", "1990-05-15"), entry("Bob", "1985-01-02")))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.SKIP)

            assertTrue(result is ImportBirthdaysResult.Success)
            result as ImportBirthdaysResult.Success
            assertEquals(1, result.importedCount)
            assertEquals(1, result.skippedCount)
            assertEquals(listOf("Alice", "Bob"), repository.all().map { it.name })
            verify(scheduleNotificationUseCase).scheduleNotification(any())
        }

    @Test
    fun `overwrite strategy replaces existing record matched by natural key`() =
        runTest {
            seedLocal(
                Birthday(
                    name = "Alice",
                    birthDate = LocalDate.of(1990, 5, 15),
                    notes = "old note",
                ),
            )
            stubRead(backupFile(entry("Alice", "1990-05-15", notes = "new note")))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.OVERWRITE)

            assertTrue(result is ImportBirthdaysResult.Success)
            val stored = repository.all().single { it.name == "Alice" }
            assertEquals("new note", stored.notes)
            verify(cancelNotificationUseCase).cancelNotification(any<Long>())
        }

    @Test
    fun `merge strategy updates existing record keeping local identity`() =
        runTest {
            seedLocal(
                Birthday(
                    name = "Alice",
                    birthDate = LocalDate.of(1990, 5, 15),
                    notes = "old note",
                    createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(),
                ),
            )
            val localId = repository.all().single().id
            stubRead(backupFile(entry("Alice", "1990-05-15", notes = "updated note")))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.MERGE)

            assertTrue(result is ImportBirthdaysResult.Success)
            result as ImportBirthdaysResult.Success
            assertEquals(1, result.importedCount)
            assertEquals(0, result.skippedCount)
            val merged = repository.byId(localId)!!
            assertEquals("updated note", merged.notes)
            assertEquals(LocalDate.of(2024, 1, 1).atStartOfDay(), merged.createdAt)
        }

    @Test
    fun `unsupported version returns UnsupportedVersion and imports nothing`() =
        runTest {
            whenever(backupDataSource.readBackup(testUri))
                .thenReturn(Result.success(BackupFileDto(version = 99, exportDate = "x", birthdays = emptyList())))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.SKIP)

            assertTrue(result is ImportBirthdaysResult.UnsupportedVersion)
            assertEquals(99, (result as ImportBirthdaysResult.UnsupportedVersion).version)
            assertTrue(repository.all().isEmpty())
        }

    @Test
    fun `unreadable file returns StorageError`() =
        runTest {
            whenever(
                backupDataSource.readBackup(testUri),
            ).thenReturn(Result.failure(IllegalStateException("no stream")))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.SKIP)

            assertTrue(result is ImportBirthdaysResult.StorageError)
        }

    @Test
    fun `database failure returns DatabaseError`() =
        runTest {
            repository.failWrites = true
            stubRead(backupFile(entry("Bob", "1985-01-02")))

            val result = useCase.importBirthdays(testUri, ConflictStrategy.SKIP)

            assertTrue(result is ImportBirthdaysResult.DatabaseError)
            // All-or-nothing: nothing was committed.
            assertTrue(repository.all().isEmpty())
            verify(scheduleNotificationUseCase, never()).scheduleNotification(any())
        }

    private suspend fun stubRead(backupFile: BackupFileDto) {
        whenever(backupDataSource.readBackup(testUri)).thenReturn(Result.success(backupFile))
    }

    private suspend fun seedLocal(vararg birthdays: Birthday) {
        birthdays.forEach { repository.addBirthday(it) }
    }

    private fun backupFile(vararg entries: BirthdayBackupDto): BackupFileDto {
        return BackupFileDto(
            exportDate = "2026-01-01T00:00:00",
            birthdays = entries.toList(),
        )
    }

    private fun entry(
        name: String,
        birthDate: String,
        notes: String? = null,
    ): BirthdayBackupDto {
        return BirthdayBackupDto(name = name, birthDate = birthDate, notes = notes)
    }

    /**
     * In-memory BirthdayRepository fake; runInTransaction executes the block directly so conflict
     * logic can be exercised on the JVM without Room.
     */
    private class FakeBirthdayRepository : BirthdayRepository {
        private val store = linkedMapOf<Long, Birthday>()
        private var nextId = 1L
        var failWrites = false

        override fun getAllBirthdays(): Flow<List<Birthday>> = throw NotImplementedError()

        override suspend fun getAllBirthdaysSnapshot(): List<Birthday> = store.values.toList()

        override suspend fun getBirthdayById(id: Long): Birthday? = store[id]

        override suspend fun addBirthday(birthday: Birthday): Long {
            check(!failWrites) { "simulated write failure" }
            val id = nextId++
            store[id] = birthday.copy(id = id)
            return id
        }

        override suspend fun updateBirthday(birthday: Birthday) {
            check(!failWrites) { "simulated write failure" }
            store[birthday.id] = birthday
        }

        override suspend fun deleteBirthday(birthday: Birthday) {
            store.remove(birthday.id)
        }

        override suspend fun deleteBirthdayById(id: Long) {
            store.remove(id)
        }

        override suspend fun getBirthdayCount(): Int = store.size

        override fun searchBirthdaysByName(searchQuery: String): Flow<List<Birthday>> = throw NotImplementedError()

        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()

        override fun getBirthdaysForDate(monthDay: String): Flow<List<Birthday>> = throw NotImplementedError()

        override fun getBirthdaysForMonth(month: String): Flow<List<Birthday>> = throw NotImplementedError()

        override fun getBirthdaysWithNotificationsEnabled(): Flow<List<Birthday>> = throw NotImplementedError()

        fun all(): List<Birthday> = store.values.toList()

        fun byId(id: Long): Birthday? = store[id]
    }
}
