package com.birthdayreminder.ui.viewmodel

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.usecase.UpdateBirthdayResult
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class PerPersonReminderViewModelTest {
    private lateinit var repository: BirthdayRepository
    private lateinit var updateBirthdayUseCase: UpdateBirthdayUseCase
    private lateinit var viewModel: PerPersonReminderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mock()
        updateBirthdayUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun person(
        id: Long,
        name: String,
        lead: Int = 0,
        enabled: Boolean = true,
    ) = Birthday(
        id = id,
        name = name,
        birthDate = LocalDate.of(1990, 6, 15),
        notes = null,
        notificationsEnabled = enabled,
        advanceNotificationDays = lead,
        createdAt = LocalDateTime.now(),
    )

    /**
     * Reads the people flow the way the screen does.
     *
     * `stateIn(WhileSubscribed)` only runs while something is collecting, so a
     * test that reads `.value` directly sees the initial empty list rather than
     * the database. That is correct behaviour, not a bug to work around in the
     * ViewModel, so the test collects like the screen.
     */
    private suspend fun firstPeople(): List<PerPersonReminderRow> = viewModel.people.first { it.isNotEmpty() }

    /**
     * Stubs the partial-update use case to always return [result].
     *
     * `whenever` builds a stub by invoking the call, which is impossible for a
     * suspend function, so Mockito-Kotlin's `wheneverBlocking` is the variant
     * that suspends while doing it. Matchers are written out fully qualified
     * with an explicit type per field: a bare anyOrNull() inside a suspend
     * lambda does not constrain its own type parameter here, and the
     * fourteen-argument call fails to resolve without it.
     */
    private fun stubUpdate(result: UpdateBirthdayResult) {
        wheneverBlocking {
            updateBirthdayUseCase.updateBirthdayPartial(
                birthdayId = org.mockito.ArgumentMatchers.anyLong(),
                name = org.mockito.ArgumentMatchers.any<String>(),
                birthDate = org.mockito.ArgumentMatchers.any<java.time.LocalDate>(),
                notes = org.mockito.ArgumentMatchers.any<String>(),
                notificationsEnabled = org.mockito.ArgumentMatchers.any<Boolean>(),
                advanceNotificationDays = org.mockito.ArgumentMatchers.any<Int>(),
                notificationHour = org.mockito.ArgumentMatchers.any<Int>(),
                notificationMinute = org.mockito.ArgumentMatchers.any<Int>(),
                imageUri = org.mockito.ArgumentMatchers.any<String>(),
                relationship = org.mockito.ArgumentMatchers.any<String>(),
                isPinned = org.mockito.ArgumentMatchers.any<Boolean>(),
                notificationOffsets = org.mockito.ArgumentMatchers.any<List<Int>>(),
                notificationTime = org.mockito.ArgumentMatchers.any<java.time.LocalTime>(),
                cardTone = org.mockito.ArgumentMatchers.any<String>(),
            )
        }.thenReturn(result)
    }

    @Test
    fun `it lists people in name order, not insertion order`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "zoe"), person(2, "Amma"), person(3, "bob"))),
            )

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)

            assertEquals(
                listOf("Amma", "bob", "zoe"),
                firstPeople().map { it.name },
            )
        }

    @Test
    fun `it exposes the stored lead time and switch per person`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(
                flowOf(listOf(person(1, "Amma", lead = 7, enabled = false))),
            )

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)

            val row = firstPeople().single()
            assertEquals(7, row.leadTimeDays)
            assertEquals(false, row.notificationsEnabled)
        }

    @Test
    fun `setting a lead time writes only that field`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(flowOf(listOf(person(1, "Amma"))))
            stubUpdate(UpdateBirthdayResult.Success)

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)
            viewModel.setLeadTime(1, 3)

            verifyBlocking(updateBirthdayUseCase) {
                updateBirthdayUseCase.updateBirthdayPartial(
                    birthdayId = eq(1),
                    name = org.mockito.ArgumentMatchers.isNull(),
                    birthDate = org.mockito.ArgumentMatchers.isNull(),
                    notes = org.mockito.ArgumentMatchers.isNull(),
                    notificationsEnabled = org.mockito.ArgumentMatchers.isNull(),
                    advanceNotificationDays = eq(3),
                    notificationHour = org.mockito.ArgumentMatchers.isNull(),
                    notificationMinute = org.mockito.ArgumentMatchers.isNull(),
                    imageUri = org.mockito.ArgumentMatchers.isNull(),
                    relationship = org.mockito.ArgumentMatchers.isNull(),
                    isPinned = org.mockito.ArgumentMatchers.isNull(),
                    notificationOffsets = org.mockito.ArgumentMatchers.isNull(),
                    notificationTime = org.mockito.ArgumentMatchers.isNull(),
                    cardTone = org.mockito.ArgumentMatchers.isNull(),
                )
            }
        }

    @Test
    fun `toggling reminders writes only that field`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(flowOf(listOf(person(1, "Amma"))))
            stubUpdate(UpdateBirthdayResult.Success)

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)
            viewModel.setNotificationsEnabled(1, false)

            verifyBlocking(updateBirthdayUseCase) {
                updateBirthdayUseCase.updateBirthdayPartial(
                    birthdayId = eq(1),
                    name = org.mockito.ArgumentMatchers.isNull(),
                    birthDate = org.mockito.ArgumentMatchers.isNull(),
                    notes = org.mockito.ArgumentMatchers.isNull(),
                    notificationsEnabled = eq(false),
                    advanceNotificationDays = org.mockito.ArgumentMatchers.isNull(),
                    notificationHour = org.mockito.ArgumentMatchers.isNull(),
                    notificationMinute = org.mockito.ArgumentMatchers.isNull(),
                    imageUri = org.mockito.ArgumentMatchers.isNull(),
                    relationship = org.mockito.ArgumentMatchers.isNull(),
                    isPinned = org.mockito.ArgumentMatchers.isNull(),
                    notificationOffsets = org.mockito.ArgumentMatchers.isNull(),
                    notificationTime = org.mockito.ArgumentMatchers.isNull(),
                    cardTone = org.mockito.ArgumentMatchers.isNull(),
                )
            }
        }

    @Test
    fun `a failed write is reported against the row that failed`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(flowOf(listOf(person(1, "Amma"))))
            stubUpdate(UpdateBirthdayResult.NotFound("No such person"))

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)
            viewModel.setLeadTime(1, 7)

            assertTrue(
                "A silently failed write leaves the chip showing a value the database does not have",
                viewModel.errors.value.containsKey(1L),
            )
        }

    @Test
    fun `clearing an error removes only that row`() =
        runTest {
            whenever(repository.getAllBirthdays()).thenReturn(flowOf(listOf(person(1, "Amma"))))
            stubUpdate(UpdateBirthdayResult.NotFound("No such person"))

            viewModel = PerPersonReminderViewModel(repository, updateBirthdayUseCase)
            viewModel.setLeadTime(1, 7)
            viewModel.clearError(1)

            assertTrue(viewModel.errors.value.isEmpty())
            verify(repository, never()).getAllBirthdaysSnapshot()
        }
}
