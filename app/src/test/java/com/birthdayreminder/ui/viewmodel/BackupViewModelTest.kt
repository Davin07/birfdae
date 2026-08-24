package com.birthdayreminder.ui.viewmodel

import android.net.Uri
import com.birthdayreminder.data.backup.ConflictStrategy
import com.birthdayreminder.domain.usecase.ExportBirthdaysResult
import com.birthdayreminder.domain.usecase.ExportBirthdaysUseCase
import com.birthdayreminder.domain.usecase.ImportBirthdaysResult
import com.birthdayreminder.domain.usecase.ImportBirthdaysUseCase
import com.birthdayreminder.domain.usecase.ValidateBackupFileResult
import com.birthdayreminder.domain.usecase.ValidateBackupFileUseCase
import com.birthdayreminder.testutil.TestUris
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    private lateinit var exportUseCase: ExportBirthdaysUseCase
    private lateinit var importUseCase: ImportBirthdaysUseCase
    private lateinit var validateUseCase: ValidateBackupFileUseCase
    private lateinit var viewModel: BackupViewModel

    // Unconfined so viewModelScope launches execute eagerly and state updates are visible
    // immediately after the triggering call.
    private val dispatcher = UnconfinedTestDispatcher()

    /**
     * Uri stand-in; android.net.Uri can neither be instantiated nor mocked in plain JVM tests.
     */
    private val testUri: Uri = TestUris.fakeUri()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        exportUseCase = mock()
        importUseCase = mock()
        validateUseCase = mock()
        viewModel =
            BackupViewModel(
                exportBirthdaysUseCase = exportUseCase,
                importBirthdaysUseCase = importUseCase,
                validateBackupFileUseCase = validateUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful export updates exported count`() =
        runTest(dispatcher) {
            whenever(exportUseCase.exportBirthdays(any())).thenReturn(ExportBirthdaysResult.Success(5))

            viewModel.exportBirthdays(testUri)

            val state = viewModel.uiState.value
            assertEquals(5, state.export.exportedCount)
            assertFalse(state.isBusy)
            assertNull(state.export.error)
        }

    @Test
    fun `failed export surfaces storage error`() =
        runTest(dispatcher) {
            whenever(exportUseCase.exportBirthdays(any()))
                .thenReturn(ExportBirthdaysResult.StorageError(IllegalStateException("no space")))

            viewModel.exportBirthdays(testUri)

            val state = viewModel.uiState.value
            assertTrue(state.export.error is BackupError.Storage)
            assertNull(state.export.exportedCount)
        }

    @Test
    fun `unsupported backup version maps to typed error`() =
        runTest(dispatcher) {
            whenever(importUseCase.importBirthdays(any(), eq(ConflictStrategy.SKIP)))
                .thenReturn(ImportBirthdaysResult.UnsupportedVersion(99))

            viewModel.importBirthdays(testUri, ConflictStrategy.SKIP)

            val state = viewModel.uiState.value
            assertEquals(99, (state.import.error as BackupError.UnsupportedVersion).version)
            assertNull(state.import.summary)
        }

    @Test
    fun `invalid file validation records reason`() =
        runTest(dispatcher) {
            whenever(validateUseCase.validateBackupFile(any()))
                .thenReturn(ValidateBackupFileResult.Invalid("bad json"))

            viewModel.validateBackupFile(testUri)

            val state = viewModel.uiState.value
            assertEquals("bad json", state.validation.invalidReason)
            assertNull(state.validation.fileInfo)
        }
}
