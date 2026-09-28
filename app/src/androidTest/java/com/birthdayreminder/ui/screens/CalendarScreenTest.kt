package com.birthdayreminder.ui.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
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
 * Uses a Hilt-enabled host because [CalendarScreen] injects its own ViewModel;
 * `createComposeRule` hosts a bare ComponentActivity and cannot supply it.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun displaysNavigationButtons() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                CalendarScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("Previous month").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next month").assertIsDisplayed()
    }

    @Test
    fun displaysDaysOfWeek() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                CalendarScreen()
            }
        }

        // The Saffron calendar uses single-letter headers, not "Sun"/"Mon".
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { initial ->
            composeTestRule.onAllNodesWithText(initial).onFirst().assertIsDisplayed()
        }
    }

    @Test
    fun monthArrows_changeTheVisibleMonth() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                CalendarScreen()
            }
        }

        // Whatever month is shown first, moving forward must change the caption.
        val before = currentMonthCaption()
        composeTestRule.onNodeWithContentDescription("Next month").performClick()
        composeTestRule.waitForIdle()
        val after = currentMonthCaption()

        assert(before != after) { "Month caption did not change: $before" }
    }

    /** The caption is the only text on screen that carries a four-digit year. */
    private fun currentMonthCaption(): String =
        composeTestRule
            .onAllNodes(hasText("20", substring = true))
            .onFirst()
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString()
}
