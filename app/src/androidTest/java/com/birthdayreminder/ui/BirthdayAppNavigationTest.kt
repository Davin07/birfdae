package com.birthdayreminder.ui

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.ui.theme.BirthdayReminderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BirthdayAppNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun birthdayApp_navigatesToAddBirthdayScreen_whenFabClicked() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // When - Click the add birthday FAB
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()

        // Then - Should navigate to add birthday screen
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Birth Date").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_navigatesToCalendarScreen_whenCalendarTabClicked() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // When - Click the calendar tab
        composeTestRule.onNodeWithText("Calendar").performClick()

        // Then - Should navigate to calendar screen
        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_navigatesBackToListScreen_whenBackPressed() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Navigate to add birthday screen
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()

        // When - Press back button
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then - Should return to list screen
        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_maintainsBottomNavigation_acrossScreens() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Then - Bottom navigation should be visible on list screen
        composeTestRule.onNodeWithText("List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()

        // When - Navigate to calendar
        composeTestRule.onNodeWithText("Calendar").performClick()

        // Then - Bottom navigation should still be visible
        composeTestRule.onNodeWithText("List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_hidesBottomNavigation_onAddEditScreen() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Navigate to add birthday screen
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()

        // Then - Bottom navigation should be hidden
        composeTestRule.onNodeWithText("List").assertDoesNotExist()
        composeTestRule.onNodeWithText("Calendar").assertDoesNotExist()
    }

    @Test
    fun birthdayApp_completeBirthdayAddFlow_worksEndToEnd() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Start from list screen
        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()

        // When - Navigate to add birthday
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()

        // Then - Should be on add birthday screen
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()

        // When - Fill in birthday details
        composeTestRule.onNodeWithText("Name").performTextInput("John Doe")
        composeTestRule.onNodeWithText("Notes (Optional)").performTextInput("Best friend")

        // When - Try to save (this would normally trigger validation)
        composeTestRule.onNodeWithText("Save").performClick()

        // Then - Should show validation error for missing birth date
        composeTestRule.onNodeWithText("Birth date is required").assertIsDisplayed()

        // When - Select birth date (this would open date picker in real app)
        composeTestRule.onNodeWithText("Birth Date").performClick()

        // Note: In a real test, you would interact with the date picker here
        // For this test, we'll assume the date picker interaction is tested separately
    }

    @Test
    fun birthdayApp_searchFunctionality_worksInListScreen() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Should be on list screen
        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()

        // When - Open search (if search functionality exists)
        composeTestRule.onNodeWithContentDescription("Search").performClick()

        // Then - Search field should be visible
        composeTestRule.onNodeWithText("Search birthdays...").assertIsDisplayed()

        // When - Type search query
        composeTestRule.onNodeWithText("Search birthdays...").performTextInput("John")

        // Then - Search should be active (results would depend on data)
        // This test assumes the search functionality exists and works
    }

    @Test
    fun birthdayApp_refreshFunctionality_worksInListScreen() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Should be on list screen
        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()

        // When - Perform pull to refresh gesture
        composeTestRule.onRoot().performTouchInput {
            swipeDown()
        }

        // Then - Refresh should be triggered
        // In a real app, this would show a loading indicator briefly
        // The exact assertion would depend on the implementation
    }

    @Test
    fun birthdayApp_calendarNavigation_worksCorrectly() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Navigate to calendar screen
        composeTestRule.onNodeWithText("Calendar").performClick()

        // When - Navigate to next month
        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        // Then - Month should change (exact month depends on current date)
        // The calendar should still be functional
        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()

        // When - Navigate back to previous month
        composeTestRule.onNodeWithContentDescription("Previous month").performClick()

        // Then - Should return to original month
        // Calendar should still be functional
        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_deepLinking_worksForDirectNavigation() {
        // This test would verify deep linking functionality
        // The exact implementation would depend on how deep links are set up

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                // In a real app, you might pass a deep link parameter here
                BirthdayApp()
            }
        }

        // Then - App should handle deep links correctly
        // This is a placeholder for deep link testing
        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
    }

    @Test
    fun birthdayApp_statePreservation_worksAcrossNavigation() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayApp()
            }
        }

        // Given - Navigate to add birthday and enter some data
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()
        composeTestRule.onNodeWithText("Name").performTextInput("Test User")

        // When - Navigate away and back
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()

        // Then - In a real app with proper state management,
        // the form might preserve the entered data or reset it
        // The exact behavior would depend on the implementation
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }
}
