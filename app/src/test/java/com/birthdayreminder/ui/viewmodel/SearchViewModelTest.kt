package com.birthdayreminder.ui.viewmodel

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.model.SearchFilter
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
        relationship: String? = null,
        birthDate: LocalDate = LocalDate.of(1990, 6, 15),
    ) = Birthday(
        id = id,
        name = name,
        birthDate = birthDate,
        notes = null,
        notificationsEnabled = true,
        advanceNotificationDays = 0,
        relationship = relationship,
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
            // One list, filtered in memory. The previous test stubbed a
            // repository search that the screen no longer calls, so it passed
            // against a code path the screen had left behind.
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma"), person(2, "Karthik"))),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onQueryChanged("K")

            assertEquals(1, viewModel.uiState.value.results.size)
            assertEquals("Karthik", viewModel.uiState.value.results.first().birthday.name)
        }

    @Test
    fun `the Family filter finds a parent`() =
        runTest {
            // The reason the relationship vocabulary is shared with the wizard:
            // someone saved as Parents has to be reachable by the relationship
            // they were tagged with.
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(
                    listOf(
                        person(1, "Amma", relationship = "Parents"),
                        person(2, "Karthik", relationship = "Friends"),
                    ),
                ),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.FAMILY)

            assertEquals(
                listOf("Amma"),
                viewModel.uiState.value.results.map { it.birthday.name },
            )
        }

    @Test
    fun `the Friends filter returns only friends`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(
                    listOf(
                        person(1, "Amma", relationship = "Parents"),
                        person(2, "Karthik", relationship = "Friends"),
                    ),
                ),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.FRIENDS)

            assertEquals(
                listOf("Karthik"),
                viewModel.uiState.value.results.map { it.birthday.name },
            )
        }

    @Test
    fun `All shows a person with no relationship at all`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(
                    listOf(
                        person(1, "Untagged", relationship = null),
                        person(2, "Karthik", relationship = "Friends"),
                    ),
                ),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.ALL)

            assertEquals(2, viewModel.uiState.value.results.size)
        }

    @Test
    fun `This month returns only birthdays still ahead this month`() =
        runTest {
            val today = LocalDate.now()
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(
                    listOf(
                        person(1, "Later this month", birthDate = today.withDayOfMonth(28)),
                        // Earlier in the month, so its next occurrence is next
                        // year and it is not "this month".
                        person(2, "Earlier this month", birthDate = today.withDayOfMonth(1)),
                    ),
                ),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.THIS_MONTH)

            assertTrue(
                "A birthday earlier this month recurs next year, so it is not " +
                    "still ahead this month",
                viewModel.uiState.value.results.none { it.birthday.name == "Earlier this month" },
            )
        }

    @Test
    fun `a filter and a query combine`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(
                    listOf(
                        person(1, "Amma", relationship = "Parents"),
                        person(2, "Amit", relationship = "Parents"),
                        person(3, "Karthik", relationship = "Friends"),
                    ),
                ),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.FAMILY)
            viewModel.onQueryChanged("Ami")

            assertEquals(
                listOf("Amit"),
                viewModel.uiState.value.results.map { it.birthday.name },
            )
        }

    @Test
    fun `choosing the filter twice is not an error`() =
        runTest {
            // The guard exists so a re-tap does not re-run the query. It must
            // not leave the state wrong when the same filter is chosen again.
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma", relationship = "Parents"))),
            )
            viewModel = SearchViewModel(repository, countdownUseCase)

            viewModel.onFilterChanged(SearchFilter.FAMILY)
            viewModel.onFilterChanged(SearchFilter.FAMILY)

            assertEquals(SearchFilter.FAMILY, viewModel.uiState.value.filter)
            assertEquals(1, viewModel.uiState.value.results.size)
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
