package com.birthdayreminder.domain.model

import com.birthdayreminder.domain.usecase.ReminderStreak
import java.time.LocalDate

/**
 * What the home screen's hero says, and who it is about.
 *
 * The prototype's hero was built around one sentence -- "You've remembered 11
 * of her last 11" -- which is a claim about the user's behaviour. Everything
 * here exists to make that sentence either true or absent. There is no branch
 * that produces a flattering line the data does not support, because the whole
 * point of a streak is that someone else can rely on it.
 *
 * @property people the birthdays the hero is about, never empty
 * @property eyebrow the small label above the headline
 * @property headline the large line, naming the people
 * @property supportingLine the honest detail: ages, and a streak only when
 *   there is enough history to mean something
 * @property primaryBirthdayId who the hero's action is for, which is the
 *   first of [people] and is also who tapping the row edits
 * @property isToday whether these birthdays are actually today, which decides
 *   between the "Today" and "Next up" framing
 */
data class HomeHero(
    val people: List<HeroPerson>,
    val eyebrow: String,
    val headline: String,
    val supportingLine: String,
    val primaryBirthdayId: Long,
    val isToday: Boolean,
) {
    companion object {
        const val MAX_PEOPLE = 2
    }
}

/**
 * One birthday in the hero.
 *
 * @property birthdayId the person
 * @property name their name
 * @property ageTurning the age they turn on this occurrence
 */
data class HeroPerson(
    val birthdayId: Long,
    val name: String,
    val ageTurning: Int,
)

/**
 * Turns birthdays and a streak into the words the hero shows.
 *
 * Separate from the composable on purpose: every rule about what may be claimed
 * is a rule about copy, and copy is the thing most likely to drift into a lie
 * when it is written inline in a layout.
 */
object HomeHeroCopy {
    /**
     * Builds the hero for the birthdays that matter most.
     *
     * @param dueOn birthdays falling on [on], already sorted
     * @param nextUp the soonest upcoming birthday when nothing is due today
     * @param streak the streak of the person the hero is about, or null when
     *   there is no history to describe
     * @param on the day being described
     */
    fun build(
        dueOn: List<HeroPerson>,
        nextUp: HeroPerson?,
        streak: ReminderStreak?,
        on: LocalDate = LocalDate.now(),
    ): HomeHero? {
        val people =
            when {
                dueOn.isNotEmpty() -> dueOn.take(HomeHero.MAX_PEOPLE)
                nextUp != null -> listOf(nextUp)
                else -> return null
            }
        val isToday = dueOn.isNotEmpty()

        return HomeHero(
            people = people,
            eyebrow = if (isToday) "Today" else "Coming up",
            headline = headlineFor(people),
            supportingLine = supportingLineFor(people, streak, isToday),
            primaryBirthdayId = people.first().birthdayId,
            isToday = isToday,
        )
    }

    /**
     * The large line, combining names when two birthdays land together.
     *
     * "Amma" alone when one person; "Amma & Appa" for two. The joining is the
     * point -- the prototype's example was a shared date, and a hero that said
     * only one name would hide the reason the date mattered.
     */
    private fun headlineFor(people: List<HeroPerson>): String =
        when (people.size) {
            1 -> people[0].name
            2 -> "${people[0].name} & ${people[1].name}"
            else -> people.take(HomeHero.MAX_PEOPLE).joinToString(" & ")
        }

    /**
     * The honest detail line.
     *
     * The streak appears only when [ReminderStreak.isMeaningful]. With one
     * tracked year the sentence "you've remembered 1 of her last 1" is true and
     * worthless, and it would dress up a fresh install as a track record -- so
     * a short history falls back to when the person was added, which is a real
     * fact about the relationship rather than a flattering one.
     */
    private fun supportingLineFor(
        people: List<HeroPerson>,
        streak: ReminderStreak?,
        isToday: Boolean,
    ): String {
        val ages =
            when (people.size) {
                1 -> "turns ${people[0].ageTurning}"
                else -> "turn ${people.map { it.ageTurning }.joinToString(" and ")}"
            }
        // No "Today turns 31" / "Coming up turns 66" lead here: the hero's
        // eyebrow is the label for that, and the line repeated it verbatim
        // directly underneath.
        val detail =
            when {
                streak != null && streak.isMeaningful ->
                    "You've remembered ${streak.streakYears} of ${streak.name}'s " +
                        "last ${streak.trackedYears}."

                streak != null -> "On your list since ${streak.firstTrackedYear}."

                else -> ""
            }

        return if (detail.isEmpty()) "$ages." else "$ages. $detail"
    }
}
