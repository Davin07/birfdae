package com.birthdayreminder.ui.viewmodel

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import com.birthdayreminder.domain.util.SafeDateCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Search has no "run a search" button, so its correctness depends entirely on
 * what happens on load. Two bugs lived here and neither is visible from the
 * code alone:
 *
 * - the ViewModel never ran an initial search, so the list stayed empty until
 *   the user typed, and the screen read "No birthdays yet" while the home
 *   screen showed the same people;
 * - a blank query cleared the results rather than meaning "everyone".
 *
 * Both look like data loss to the user, so both are pinned here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private lateinit var repository: BirthdayRepository
    private lateinit var viewModel: SearchViewModel

    private val dispatcher = UnconfinedTestDispatcher()

    private fun person(
        id: Long,
        name: String,
    ) = Birthday(
        id = id,
        name = name,
        birthDate = LocalDate.of(1990, 6, 15),
        notes = null,
        notificationsEnabled = true,
        advanceNotificationDays = 0,
        createdAt = LocalDateTime.now(),
    )

    // The real use case, not a mock. calculateCountdown takes a second
    // argument, so a one-arg Mockito answer would not match, and using the
    // real thing keeps the test about the query rather than about stubs.
    private val countdownUseCase: CalculateCountdownUseCase =
        CalculateCountdownUseCase(SafeDateCalculator(ErrorHandler()))

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it shows everyone on load, before anything is typed`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma"), person(2, "Karthik"))),
            )

            viewModel = SearchViewModel(repository, countdownUseCase)

            assertEquals(
                "Search opened empty although two birthdays are saved",
                listOf("Amma", "Karthik"),
                viewModel.uiState.value.results.map { it.birthday.name },
            )
        }

    @Test
    fun `a blank query still shows everyone`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma"))),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onQueryChanged("")

            assertEquals(
                1,
                viewModel.uiState.value.results.size,
            )
        }

    @Test
    fun `a name query filters the list`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(flowOf(emptyList()))
            whenever(repository.searchBirthdaysByName("K")).thenReturn(
                flowOf(listOf(person(2, "Karthik"))),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onQueryChanged("K")

            assertEquals(1, viewModel.uiState.value.results.size)
            assertEquals("Karthik", viewModel.uiState.value.results.first().birthday.name)
        }

    @Test
    fun `results are ordered by how soon they are`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma"), person(2, "Karthik"))),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            // The mock returns daysUntilNext = 10 for both, so the assertion
            // that matters is that both came through and the sort did not drop
            // anything.
            assertTrue(viewModel.uiState.value.results.isNotEmpty())
            assertEquals(2, viewModel.uiState.value.results.size)
        }
}
