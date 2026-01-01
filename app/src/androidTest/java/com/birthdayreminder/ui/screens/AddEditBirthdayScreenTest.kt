package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AddEditBirthdayScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addEditBirthdayScreen_displaysAddModeTitle() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysNameField() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Name *").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysBirthDateField() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Birth Date *").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysNotificationSettings() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Notification Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enable Notifications").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_callsOnNavigateBack_whenBackButtonClicked() {
        var navigateBackCalled = false

        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = { navigateBackCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(navigateBackCalled)
    }

    @Test
    fun addEditBirthdayScreen_displaysNameError_whenNameIsBlank() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayForm(
                    uiState =
                        AddEditBirthdayUiState(
                            name = "",
                            nameError = "Name is required",
                            birthDate = LocalDate.now().minusYears(1),
                        ),
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsToggle = {},
                    onAdvanceNotificationDaysChange = {},
                    onNotificationHourChange = {},
                    onNotificationMinuteChange = {},
                    onErrorDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysBirthDateError_whenDateIsMissing() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayForm(
                    uiState =
                        AddEditBirthdayUiState(
                            name = "John Doe",
                            birthDateError = "Birth date is required",
                        ),
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsToggle = {},
                    onAdvanceNotificationDaysChange = {},
                    onNotificationHourChange = {},
                    onNotificationMinuteChange = {},
                    onErrorDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Birth date is required").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysLoadingIndicator_whenIsLoadingIsTrue() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayForm(
                    uiState =
                        AddEditBirthdayUiState(
                            isLoading = true,
                        ),
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsToggle = {},
                    onAdvanceNotificationDaysChange = {},
                    onNotificationHourChange = {},
                    onNotificationMinuteChange = {},
                    onErrorDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Name *").assertIsNotDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysErrorMessage_whenErrorMessageIsSet() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayForm(
                    uiState =
                        AddEditBirthdayUiState(
                            name = "John Doe",
                            birthDate = LocalDate.now().minusYears(1),
                            errorMessage = "Failed to save birthday",
                        ),
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsToggle = {},
                    onAdvanceNotificationDaysChange = {},
                    onNotificationHourChange = {},
                    onNotificationMinuteChange = {},
                    onErrorDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to save birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_hidesNotificationTimePicker_whenNotificationsDisabled() {
        composeTestRule.setContent {
            BirthdayReminderAppTheme {
                AddEditBirthdayForm(
                    uiState =
                        AddEditBirthdayUiState(
                            name = "John Doe",
                            birthDate = LocalDate.now().minusYears(1),
                            notificationsEnabled = false,
                        ),
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsToggle = {},
                    onAdvanceNotificationDaysChange = {},
                    onNotificationHourChange = {},
                    onNotificationMinuteChange = {},
                    onErrorDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Advance Notification").assertIsNotDisplayed()
    }
}
