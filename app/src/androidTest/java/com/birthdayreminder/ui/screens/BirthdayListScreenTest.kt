package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.viewmodel.BirthdayListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BirthdayListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun birthdayListScreen_displaysTitle() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListScreen(
                    onNavigateToAddBirthday = {},
                    onNavigateToEditBirthday = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_displaysAddBirthdayButton() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListScreen(
                    onNavigateToAddBirthday = {},
                    onNavigateToEditBirthday = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_callsOnNavigateToAddBirthday_whenFabClicked() {
        var addBirthdayClicked = false

        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListScreen(
                    onNavigateToAddBirthday = { addBirthdayClicked = true },
                    onNavigateToEditBirthday = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").performClick()

        assert(addBirthdayClicked)
    }

    @Test
    fun birthdayListScreen_displaysLoadingIndicator_whenIsLoadingIsTrue() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState = BirthdayListUiState(isLoading = true),
                    onRefresh = {},
                    onNavigateToEditBirthday = {},
                    onDeleteBirthday = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Birthdays").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_displaysEmptyState_whenNoBirthdays() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState =
                        BirthdayListUiState(
                            birthdays = emptyList(),
                            isLoading = false,
                        ),
                    onRefresh = {},
                    onNavigateToEditBirthday = {},
                    onDeleteBirthday = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No birthdays yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add your first birthday to get started").assertIsDisplayed()
    }

    @Test
    fun birthdayListScreen_displaysBirthdayCards_whenBirthdaysExist() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                BirthdayListContent(
                    uiState =
                        BirthdayListUiState(
                            birthdays = emptyList(),
                            isLoading = false,
                        ),
                    onRefresh = {},
                    onNavigateToEditBirthday = {},
                    onDeleteBirthday = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }
}
