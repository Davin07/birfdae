package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.theme.BirthdayReminderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class BirthdayListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun birthdayListScreen_displaysEmptyState_whenNoBirthdays() {
        // Given
        val emptyBirthdays = emptyList<BirthdayWithCountdown>()
        val emptyTodaysBirthdays = emptyList<BirthdayWithCountdown>()

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = emptyBirthdays,
                    todaysBirthdays = emptyTodaysBirthdays,
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No birthdays yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add your first birthday to get started!").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_displaysBirthdays_whenBirthdaysExist() {
        // Given
        val birthdays =
            listOf(
                createMockBirthdayWithCountdown("Alice Johnson", 30),
                createMockBirthdayWithCountdown("Bob Smith", 45),
            )

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = birthdays,
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 days").assertIsDisplayed()
        composeTestRule.onNodeWithText("45 days").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_displaysTodaysBirthdays_whenTodaysBirthdaysExist() {
        // Given
        val todaysBirthdays =
            listOf(
                createMockBirthdayWithCountdown("Today Person", 0, isToday = true),
            )

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = emptyList(),
                    todaysBirthdays = todaysBirthdays,
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🎉 Today's Birthdays").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today Person").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today!").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_showsLoadingIndicator_whenLoading() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = emptyList(),
                    todaysBirthdays = emptyList(),
                    isLoading = true,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("loading_indicator")).assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_showsErrorMessage_whenErrorExists() {
        // Given
        val errorMessage = "Failed to load birthdays"

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = emptyList(),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = errorMessage,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_callsOnAddBirthdayClick_whenFabClicked() {
        // Given
        var addBirthdayClicked = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = emptyList(),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = { addBirthdayClicked = true },
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()

        // Then
        assert(addBirthdayClicked)
    }

    @Test
    fun birthdayListScreen_callsOnBirthdayClick_whenBirthdayCardClicked() {
        // Given
        val birthday = createMockBirthdayWithCountdown("Clickable Person", 15)
        var clickedBirthday: BirthdayWithCountdown? = null

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = listOf(birthday),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = { clickedBirthday = it },
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Clickable Person").performClick()

        // Then
        assert(clickedBirthday == birthday)
    }

    @Test
    fun birthdayListScreen_showsDeleteConfirmation_whenBirthdayToDeleteExists() {
        // Given
        val birthdayToDelete = createMockBirthdayWithCountdown("Delete Me", 20)

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = listOf(birthdayToDelete),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = birthdayToDelete,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete Delete Me's birthday?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_callsOnConfirmDelete_whenDeleteConfirmed() {
        // Given
        val birthdayToDelete = createMockBirthdayWithCountdown("Delete Me", 20)
        var deleteConfirmed = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = listOf(birthdayToDelete),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = birthdayToDelete,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = { deleteConfirmed = true },
                    onCancelDelete = {},
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Delete").performClick()

        // Then
        assert(deleteConfirmed)
    }

    @Test
    fun birthdayListScreen_callsOnCancelDelete_whenDeleteCancelled() {
        // Given
        val birthdayToDelete = createMockBirthdayWithCountdown("Delete Me", 20)
        var deleteCancelled = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = listOf(birthdayToDelete),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = birthdayToDelete,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = { deleteCancelled = true },
                    onRefresh = {},
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assert(deleteCancelled)
    }

    @Test
    fun birthdayListScreen_performsPullToRefresh_whenSwipedDown() {
        // Given
        var refreshCalled = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                BirthdayListScreen(
                    birthdays = listOf(createMockBirthdayWithCountdown("Test Person", 10)),
                    todaysBirthdays = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    birthdayToDelete = null,
                    onAddBirthdayClick = {},
                    onBirthdayClick = {},
                    onDeleteBirthdayClick = {},
                    onConfirmDelete = {},
                    onCancelDelete = {},
                    onRefresh = { refreshCalled = true },
                    onSearchQueryChange = {},
                    onClearSearch = {},
                    onClearError = {},
                )
            }
        }

        // When - Perform swipe down gesture
        composeTestRule.onRoot().performTouchInput {
            swipeDown()
        }

        // Then
        assert(refreshCalled)
    }

    private fun createMockBirthdayWithCountdown(
        name: String,
        daysUntilNext: Int,
        isToday: Boolean = false,
        id: Long = 1L,
    ): BirthdayWithCountdown {
        val birthday =
            Birthday(
                id = id,
                name = name,
                birthDate = LocalDate.of(1990, 6, 15),
                notes = null,
                notificationsEnabled = true,
                advanceNotificationDays = 0,
                createdAt = LocalDateTime.now(),
            )

        return BirthdayWithCountdown(
            birthday = birthday,
            daysUntilNext = daysUntilNext,
            nextOccurrence = LocalDate.now().plusDays(daysUntilNext.toLong()),
            isToday = isToday,
            age = 30,
        )
    }
}
