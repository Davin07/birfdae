package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.ReminderEvent
import com.birthdayreminder.data.repository.ReminderEventRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The denominator of the streak is the part worth arguing about.
 *
 * "Remembered 11 of her last 11" only means something if the 11 is honest, so
 * the tracked years are the birthdays the app could actually have reminded the
 * user about: not one whose date had already passed when the person was saved,
 * and not a year that has not happened yet. Every year in between counts,
 * remembered or not -- which is what makes a missing row mean "not remembered"
 * rather than "not yet due".
 */
class GetReminderStreakUseCaseTest {
    private lateinit var repository: ReminderEventRepository
    private lateinit var useCase: GetReminderStreakUseCase

    /** Most birthdays here are in December, so a spring save still leaves that year countable. */
    private val decemberBirthday = LocalDate.of(1971, 12, 3)

    /** A March birthday, so a June save has already missed it for the year. */
    private val marchBirthday = LocalDate.of(1971, 3, 14)

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetReminderStreakUseCase(repository)
    }

    private fun event(
        year: Int,
        acknowledged: Boolean = true,
        shared: Boolean = false,
    ) = ReminderEvent(
        birthdayId = 1,
        year = year,
        acknowledgedAt = if (acknowledged) LocalDateTime.of(year, 12, 3, 9, 0) else null,
        sharedAt = if (shared) LocalDateTime.of(year, 12, 3, 9, 5) else null,
        createdAt = LocalDateTime.of(year, 12, 3, 9, 0),
    )

    private suspend fun given(vararg events: ReminderEvent) {
        whenever(repository.getEventsForBirthdaySnapshot(1)).thenReturn(events.toList())
    }

    private suspend fun streak(
        addedIn: String,
        currentYear: Int,
        birthDate: LocalDate = decemberBirthday,
    ) = useCase(
        birthdayId = 1,
        name = "Amma",
        birthDate = birthDate,
        createdAt = LocalDateTime.parse(addedIn),
        currentYear = currentYear,
    )

    @Test
    fun `the year someone was added counts when a birthday was still to come`() =
        runTest {
            given(event(2024))

            val result = streak("2024-03-10T09:00", 2024)

            assertEquals(
                "Added in March with a December birthday, the app could still remind in 2024",
                1,
                result.trackedYears,
            )
        }

    @Test
    fun `a birthday already gone when they were added does not count`() =
        runTest {
            // Added in June, birthday in March: the first chance was 2025.
            given(event(2024), event(2025))

            val result = streak("2024-06-10T09:00", 2025, birthDate = marchBirthday)

            assertEquals(1, result.trackedYears)
            assertEquals(2025, result.firstTrackedYear)
        }

    @Test
    fun `every year from being added on counts, remembered or not`() =
        runTest {
            // 2023 and 2026 remembered, 2024 and 2025 missed.
            given(event(2023), event(2026))

            val result = streak("2023-03-10T09:00", 2026)

            assertEquals(4, result.trackedYears)
            assertEquals(1, result.streakYears)
        }

    @Test
    fun `a year with no row breaks the streak`() =
        runTest {
            given(event(2023), event(2024), event(2026))

            val result = streak("2023-03-10T09:00", 2026)

            assertEquals(4, result.trackedYears)
            assertEquals(
                "The streak counts back from the current year and stops at the gap",
                1,
                result.streakYears,
            )
        }

    @Test
    fun `a share does not count as remembering`() =
        runTest {
            given(event(2023, acknowledged = false, shared = true))

            val result = streak("2023-03-10T09:00", 2025)

            assertEquals(
                "Sharing is not proof the reminder was seen, and must not feed the streak",
                0,
                result.streakYears,
            )
            assertEquals(1, result.sharedYears)
        }

    @Test
    fun `a short history is not reported as meaningful`() =
        runTest {
            given(event(2025))

            val result = streak("2025-03-10T09:00", 2025)

            assertEquals(1, result.trackedYears)
            assertFalse(
                "One tracked year cannot support a claim about consistency",
                result.isMeaningful,
            )
        }

    @Test
    fun `a long history is reported as meaningful`() =
        runTest {
            given(*(2020..2025).map { event(it) }.toTypedArray())

            val result = streak("2019-03-10T09:00", 2025)

            assertEquals(7, result.trackedYears)
            assertTrue(result.isMeaningful)
        }

    @Test
    fun `an unbroken run all the way back reads as the prototype promised`() =
        runTest {
            // Saved 2015, first acknowledged 2016, every year since.
            given(*(2016..2025).map { event(it) }.toTypedArray())

            val result = streak("2015-03-10T09:00", 2025)

            assertEquals(11, result.trackedYears)
            assertEquals(10, result.streakYears)
            assertTrue(result.isMeaningful)
        }

    @Test
    fun `a birthday not yet reached this year gives nothing to measure`() =
        runTest {
            given()

            val result = streak("2026-06-10T09:00", 2026, birthDate = marchBirthday)

            assertEquals(
                "The 2026 birthday is already past, so the first measurable year " +
                    "is 2027; a bare zero would be a sentence about nothing",
                0,
                result.trackedYears,
            )
            assertEquals(0, result.streakYears)
            assertFalse(result.isMeaningful)
            assertEquals(2027, result.firstTrackedYear)
        }
}
