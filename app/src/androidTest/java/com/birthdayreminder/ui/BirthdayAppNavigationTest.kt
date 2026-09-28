package com.birthdayreminder.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.HiltTestActivity
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real navigation graph.
 *
 * Needs Hilt, because every screen resolves its own @HiltViewModel. The
 * previous version used `createComposeRule`, which hosts a bare
 * ComponentActivity with no Hilt component, so it could not have passed once
 * the screens took injected ViewModels.
 *
 * The assertions are also updated to the Saffron copy: the old ones looked for
 * "Birthdays", "List" and "Add Birthday", none of which exist any more.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BirthdayAppNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun launch() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayApp()
            }
        }
    }

    @Test
    fun displaysUpcomingScreen_initially() {
        launch()

        // "Upcoming" is both the screen heading and the nav label, so assert
        // on the count rather than resolving a single node.
        composeTestRule.onAllNodesWithText("Upcoming").assertCountEquals(2)
    }

    @Test
    fun displaysBottomNavigation() {
        launch()

        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun navigatesToCalendar_whenCalendarTabClicked() {
        launch()

        composeTestRule.onNodeWithText("Calendar").performClick()

        composeTestRule
            .onNodeWithContentDescription("Previous month")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Next month")
            .assertIsDisplayed()
    }

    @Test
    fun calendarMonthArrows_work() {
        launch()

        composeTestRule.onNodeWithText("Calendar").performClick()
        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        composeTestRule
            .onNodeWithContentDescription("Previous month")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Next month")
            .assertIsDisplayed()
    }

    @Test
    fun opensTheWizard_whenTheAddFabIsClicked() {
        launch()

        // The FAB is icon-only, so it is matched by content description.
        composeTestRule.onNodeWithContentDescription("Add").performClick()

        // The redesigned wizard is titled "Add a birthday" and opens on step 1.
        composeTestRule.onNodeWithText("Add a birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Step 1 of 3").assertIsDisplayed()
    }

    @Test
    fun hidesBottomNavigation_onTheWizard() {
        launch()

        composeTestRule.onNodeWithContentDescription("Add").performClick()

        composeTestRule.onNodeWithText("Settings").assertDoesNotExist()
    }
}
