package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.repository.ReminderEventRepository
import com.birthdayreminder.domain.util.BirthdayYear
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * How well the user has kept up with one person's birthdays.
 *
 * The prototype's hero promised "You've remembered 11 of her last 11", which
 * is a claim about behaviour, not about data. This type is where that claim is
 * earned or refused: every field is countable from stored events, and the
 * "last N" is derived from when the person was actually added rather than
 * asserted.
 *
 * @property birthdayId the person this describes
 * @property name their name, for the copy
 * @property streakYears the most recent consecutive years, counting back from
 *   the current one, in which a reminder was acknowledged
 * @property trackedYears the years that count toward [streakYears]. This is
 *   the denominator, and it is deliberately *not* the number of event rows:
 *   years with no row are exactly the years being counted against.
 * @property sharedYears years in which a card was shared
 * @property firstTrackedYear the earliest year that counts, so the copy can say
 *   "since 2016" rather than implying an unbounded history
 */
data class ReminderStreak(
    val birthdayId: Long,
    val name: String,
    val streakYears: Int,
    val trackedYears: Int,
    val sharedYears: Int,
    val firstTrackedYear: Int,
) {
    /**
     * Whether there is enough history to say anything about consistency.
     *
     * A user who installed the app this month has one tracked year at most.
     * Telling them they have "remembered 1 of her last 1" is technically true
     * and emotionally empty -- worse, it dresses a brand-new install up as a
     * track record. Callers show the "on your list since" variant instead.
     */
    val isMeaningful: Boolean get() = trackedYears >= MIN_TRACKED_YEARS

    companion object {
        /** Below this many tracked years, a streak says nothing about a habit. */
        const val MIN_TRACKED_YEARS = 2
    }
}

/**
 * Computes a person's acknowledgement streak.
 *
 * The denominator is the part worth arguing about, so it is stated here rather
 * than left implicit: a year counts if the app could have reminded the user
 * about that birthday -- not before they saved the person, and not for a
 * birthday that had already passed when they did. Everything from then on
 * counts, whether or not a row exists for it, which is what makes a missing
 * row mean "not remembered" rather than "not yet due".
 */
class GetReminderStreakUseCase
    @Inject
    constructor(
        private val reminderEventRepository: ReminderEventRepository,
    ) {
        /**
         * Computes the streak for one person.
         *
         * @param birthdayId the person
         * @param name their name, echoed into the result for the copy
         * @param birthDate their date of birth, which decides whether the year
         *   they were added was already gone or still had a birthday in it
         * @param createdAt when they were saved
         * @param currentYear the year to measure up to, injectable for tests
         */
        suspend operator fun invoke(
            birthdayId: Long,
            name: String,
            birthDate: LocalDate,
            createdAt: LocalDateTime,
            currentYear: Int = LocalDate.now().year,
        ): ReminderStreak {
            val firstEligible =
                BirthdayYear.firstEligibleYear(
                    birthDate = birthDate,
                    addedOn = createdAt.toLocalDate(),
                )

            // Someone added after the last birthday of this year has nothing to
            // be measured against yet, and saying "0 of 0" would be a sentence
            // about nothing.
            if (firstEligible > currentYear) {
                return ReminderStreak(
                    birthdayId = birthdayId,
                    name = name,
                    streakYears = 0,
                    trackedYears = 0,
                    sharedYears = 0,
                    firstTrackedYear = firstEligible,
                )
            }

            val events = reminderEventRepository.getEventsForBirthdaySnapshot(birthdayId)
            val countableYears = (firstEligible..currentYear).toList()
            val acknowledged = events.filter { it.acknowledgedAt != null }.map { it.year }.toSet()

            val streak =
                countableYears
                    .asReversed()
                    .takeWhile { it in acknowledged }
                    .size

            return ReminderStreak(
                birthdayId = birthdayId,
                name = name,
                streakYears = streak,
                trackedYears = countableYears.size,
                sharedYears =
                    countableYears.count { year ->
                        events.any { it.year == year && it.sharedAt != null }
                    },
                firstTrackedYear = firstEligible,
            )
        }
    }
