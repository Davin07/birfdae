package com.birthdayreminder.domain.model

/**
 * Which people the home screen may list below its cards.
 *
 * The overdue card and the hero are both announcements of specific people, and
 * each says something about them. A list that then repeats those people
 * contradicts the card directly: the overdue card reads "5 DAYS AGO, Karthik"
 * and the list shows Karthik 250 days out under "Later this year". Both
 * computations are individually right, and together they read as a bug.
 *
 * Excluding only the people a card names is also what stops the exclusion from
 * going too far. The hero used to hold one person back to fill a hero card,
 * which meant the person the screen was about could be the one missing from
 * the list.
 *
 * @param birthdays everyone, with countdowns, as the database returns them
 * @param heroIds the people the hero has just announced
 * @param overdueIds the people the overdue card and its collapsed remainder
 *   stand for, including the one shown in full
 * @return the people the list may show, in the order given
 */
fun listAfterAnnouncements(
    birthdays: List<BirthdayWithCountdown>,
    heroIds: Set<Long>,
    overdueIds: Set<Long>,
): List<BirthdayWithCountdown> = birthdays.filterNot { it.birthday.id in heroIds || it.birthday.id in overdueIds }
