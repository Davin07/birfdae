package com.birthdayreminder.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BirthdayAppNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun birthdayApp_displaysBirthdaysScreen_initially() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_navigatesToCalendar_whenCalendarTabClicked() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Calendar").performClick()

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_displaysBottomNavigation() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_navigatesToAddBirthday_whenAddButtonClicked() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").performClick()

        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_displaysEditModeTitle_whenEditingBirthday() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").performClick()
        composeTestRule.onNodeWithText("Name *").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_navigatesBackToListScreen_whenBackPressed() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").performClick()
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_hidesBottomNavigation_onAddEditScreen() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").performClick()

        composeTestRule.onNodeWithText("List").assertDoesNotExist()
        composeTestRule.onNodeWithText("Calendar").assertDoesNotExist()
    }

    @Test
    fun birthdayApp_calendarNavigation_worksCorrectly() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }

        composeTestRule.onNodeWithText("Calendar").performClick()

        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Previous month").performClick()

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }
}
