package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.HiltTestActivity
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [BirthdayListScreen] injects its own ViewModel, so this needs the Hilt host.
 *
 * The previous version used `createComposeRule` (no Hilt component) and
 * asserted pre-redesign copy -- "Birthdays", "Add Birthday", "No birthdays yet"
 * and "Add your first birthday to get started!" none of which the Saffron
 * screen renders any more. Assertions below match the current copy.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BirthdayListScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun displaysUpcomingHeading() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState = BirthdayListUiState(birthdays = emptyList(), isLoading = false),
                )
            }
        }

        composeTestRule.onNodeWithText("Upcoming").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyState_whenNoBirthdays() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState = BirthdayListUiState(birthdays = emptyList(), isLoading = false),
                )
            }
        }

        composeTestRule.onNodeWithText("No birthdays yet").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Add the people you'd be sad to forget. About fifteen seconds each.")
            .assertIsDisplayed()
    }

    @Test
    fun displaysBirthday_whenBirthdaysExist() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState =
                        BirthdayListUiState(
                            birthdays = listOf(mockBirthday("Test Person", 10)),
                            isLoading = false,
                        ),
                )
            }
        }

        composeTestRule.onNodeWithText("Test Person").assertIsDisplayed()
    }

    @Test
    fun displaysTheOverdueSection_whenABirthdayHasPassed() {
        val late =
            com.birthdayreminder.domain.model.OverdueBirthday(
                birthday = mockBirthday("Late Person", 0).birthday,
                occurredOn = java.time.LocalDate.now().minusDays(3),
                daysOverdue = 3,
            )

        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState =
                        BirthdayListUiState(
                            birthdays = listOf(mockBirthday("Test Person", 10, id = 2L)),
                            overdue = listOf(late),
                            isLoading = false,
                        ),
                )
            }
        }

        // No "Needs a moment" heading: the card's own eyebrow already says how
        // long ago the birthday was, and the heading stated it twice in weaker
        // words. The heading that does follow the card is the one that names
        // what comes next.
        composeTestRule.onNodeWithText("Late Person").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coming up").assertIsDisplayed()
    }

    @Test
    fun doesNotRepeatAnOverduePersonInTheList() {
        // The card says the date has passed; the list must not then show the
        // same person as still upcoming. Both were individually correct and
        // together read as a bug.
        val overduePerson = mockBirthday("Late Person", 250)
        val late =
            com.birthdayreminder.domain.model.OverdueBirthday(
                birthday = overduePerson.birthday,
                occurredOn = java.time.LocalDate.now().minusDays(3),
                daysOverdue = 3,
            )

        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState =
                        BirthdayListUiState(
                            // Distinct ids: mockBirthday defaults both to
                            // 1L, which would make the exclusion remove the
                            // wrong person and pass for the wrong reason.
                            birthdays =
                                listOf(
                                    overduePerson,
                                    mockBirthday("Test Person", 10, id = 2L),
                                ),
                            overdue = listOf(late),
                            isLoading = false,
                        ),
                )
            }
        }

        composeTestRule.onNodeWithText("Late Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Test Person").assertCountEquals(1)
    }

    @Test
    fun callsOnAddBirthday_whenTheEmptyStateIsTapped() {
        var addTapped = false

        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState = BirthdayListUiState(birthdays = emptyList(), isLoading = false),
                    onAddBirthday = { addTapped = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Add your first birthday").performClick()

        assertTrue(addTapped)
    }

    private fun mockBirthday(
        name: String,
        daysUntilNext: Int,
        id: Long = 1L,
    ) = com.birthdayreminder.domain.model.BirthdayWithCountdown(
        birthday =
            com.birthdayreminder.data.local.entity.Birthday(
                id = id,
                name = name,
                birthDate = java.time.LocalDate.of(1990, 6, 15),
                notes = null,
                notificationsEnabled = true,
                advanceNotificationDays = 0,
                createdAt = java.time.LocalDateTime.now(),
            ),
        daysUntilNext = daysUntilNext,
        nextOccurrence = java.time.LocalDate.now().plusDays(daysUntilNext.toLong()),
        isToday = daysUntilNext == 0,
        age = 30,
    )
}
