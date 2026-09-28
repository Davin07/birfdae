package com.birthdayreminder.ui.navigation

/**
 * Navigation routes for the Birthday Reminder app
 */
object BirthdayNavigation {
    const val BIRTHDAY_LIST = "birthday_list"
    const val CALENDAR = "calendar"
    const val SEARCH = "search"
    const val ADD_EDIT_BIRTHDAY = "add_edit_birthday"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val BACKUP = "backup"
    const val PER_PERSON_REMINDERS = "per_person_reminders"

    /** One person's reminder settings. */
    const val PER_PERSON = "per_person/{personId}"

    /**
     * @param personId the person to open
     */
    fun perPerson(personId: Long): String = "per_person/$personId"

    // The shareable birthday card. This is the growth surface, so it gets its
    // own route rather than living inside the edit screen.
    const val CARD_WITH_ID = "birthday_card/{birthdayId}"

    /**
     * Builds the route to a person's shareable card.
     *
     * @param birthdayId the person whose card to show
     */
    fun createCardRoute(birthdayId: Long): String = "birthday_card/$birthdayId"

    // Route with arguments for editing birthdays
    const val ADD_EDIT_BIRTHDAY_WITH_ID = "add_edit_birthday/{birthdayId}"

    fun createAddEditBirthdayRoute(birthdayId: Long? = null): String {
        return if (birthdayId != null) {
            "add_edit_birthday/$birthdayId"
        } else {
            ADD_EDIT_BIRTHDAY
        }
    }
}
