package com.birthdayreminder.domain.usecase

import android.net.Uri
import com.birthdayreminder.data.backup.BackupDataSource
import com.birthdayreminder.data.backup.BackupFileDto
import com.birthdayreminder.data.backup.BirthdayBackupDto
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.testutil.TestUris
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ExportValidateUseCasesTest {
    private lateinit var repository: BirthdayRepository
    private lateinit var backupDataSource: BackupDataSource

    /**
     * Uri stand-in; android.net.Uri can neither be instantiated nor mocked in plain JVM tests.
     */
    private val testUri: Uri = TestUris.fakeUri()

    @Before
    fun setup() {
        repository = mock()
        backupDataSource = mock()
    }

    @Test
    fun `export writes snapshot and returns count on success`() =
        runTest {
            val birthdays =
                listOf(
                    Birthday(name = "Alice", birthDate = LocalDate.of(1990, 5, 15)),
                    Birthday(name = "Bob", birthDate = LocalDate.of(1985, 1, 2)),
                )
            whenever(repository.getAllBirthdaysSnapshot()).thenReturn(birthdays)
            whenever(backupDataSource.writeBackup(any(), any())).thenReturn(Result.success(Unit))

            val useCase = ExportBirthdaysUseCase(repository, backupDataSource)
            val result = useCase.exportBirthdays(testUri)

            assertTrue(result is ExportBirthdaysResult.Success)
            assertEquals(2, (result as ExportBirthdaysResult.Success).count)
        }

    @Test
    fun `export maps storage failure to StorageError`() =
        runTest {
            whenever(repository.getAllBirthdaysSnapshot()).thenReturn(emptyList())
            whenever(backupDataSource.writeBackup(any(), any()))
                .thenReturn(Result.failure(IllegalStateException("cannot write")))

            val useCase = ExportBirthdaysUseCase(repository, backupDataSource)
            val result = useCase.exportBirthdays(testUri)

            assertTrue(result is ExportBirthdaysResult.StorageError)
        }

    @Test
    fun `validate returns metadata for valid backup`() =
        runTest {
            whenever(backupDataSource.readBackup(testUri))
                .thenReturn(
                    Result.success(
                        BackupFileDto(
                            version = 2,
                            exportDate = "2026-01-01T00:00:00",
                            birthdays = listOf(BirthdayBackupDto(name = "Alice", birthDate = "1990-05-15")),
                        ),
                    ),
                )

            val useCase = ValidateBackupFileUseCase(backupDataSource)
            val result = useCase.validateBackupFile(testUri)

            assertTrue(result is ValidateBackupFileResult.Valid)
            result as ValidateBackupFileResult.Valid
            assertEquals(2, result.version)
            assertEquals(1, result.birthdayCount)
            assertEquals("2026-01-01T00:00:00", result.exportDate)
        }

    @Test
    fun `validate distinguishes invalid content from storage errors`() =
        runTest {
            // Serialization failure -> Invalid
            whenever(backupDataSource.readBackup(testUri))
                .thenReturn(Result.failure(kotlinx.serialization.SerializationException("bad json")))

            val useCase = ValidateBackupFileUseCase(backupDataSource)
            val invalidResult = useCase.validateBackupFile(testUri)

            assertTrue(invalidResult is ValidateBackupFileResult.Invalid)

            // Stream failure -> StorageError
            whenever(backupDataSource.readBackup(testUri))
                .thenReturn(Result.failure(java.io.IOException("stream unavailable")))

            val storageResult = useCase.validateBackupFile(testUri)

            assertTrue(storageResult is ValidateBackupFileResult.StorageError)
        }
}
