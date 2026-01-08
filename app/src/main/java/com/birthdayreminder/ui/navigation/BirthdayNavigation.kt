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
