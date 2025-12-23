package com.birthdayreminder.ui.screens

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.ui.theme.BirthdayReminderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AddEditBirthdayScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addEditBirthdayScreen_displaysAddModeCorrectly() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "",
                    birthDate = null,
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Birth Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes (Optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_displaysEditModeCorrectly() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "John Doe",
                    birthDate = LocalDate.of(1990, 5, 15),
                    notes = "Best friend",
                    notificationsEnabled = true,
                    advanceNotificationDays = 3,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = true,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Edit Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Best friend").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_callsOnNameChange_whenNameFieldChanged() {
        // Given
        var changedName = ""

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "",
                    birthDate = null,
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = { changedName = it },
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Name").performTextInput("Alice Johnson")

        // Then
        assert(changedName == "Alice Johnson")
    }

    @Test
    fun addEditBirthdayScreen_callsOnNotesChange_whenNotesFieldChanged() {
        // Given
        var changedNotes = ""

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = { changedNotes = it },
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Notes (Optional)").performTextInput("Test notes")

        // Then
        assert(changedNotes == "Test notes")
    }

    @Test
    fun addEditBirthdayScreen_showsValidationErrors_whenErrorsExist() {
        // Given
        val nameErrors = listOf("Name is required")
        val birthDateErrors = listOf("Birth date cannot be in the future")
        val notesErrors = listOf("Notes are too long")

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "",
                    birthDate = null,
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = nameErrors,
                    birthDateErrors = birthDateErrors,
                    notesErrors = notesErrors,
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Birth date cannot be in the future").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes are too long").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_showsLoadingIndicator_whenLoading() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = true,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("loading_indicator")).assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_callsOnSave_whenSaveButtonClicked() {
        // Given
        var saveClicked = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = { saveClicked = true },
                    onNavigateBack = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        assert(saveClicked)
    }

    @Test
    fun addEditBirthdayScreen_callsOnNavigateBack_whenBackButtonClicked() {
        // Given
        var backClicked = false

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "",
                    birthDate = null,
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = { backClicked = true },
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then
        assert(backClicked)
    }

    @Test
    fun addEditBirthdayScreen_togglesNotifications_whenNotificationSwitchClicked() {
        // Given
        var notificationsEnabled = true

        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = notificationsEnabled,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = { notificationsEnabled = it },
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Enable Notifications").performClick()

        // Then
        assert(!notificationsEnabled)
    }

    @Test
    fun addEditBirthdayScreen_showsAdvanceNotificationOptions_whenNotificationsEnabled() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 3,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Advance Notification").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 days before").assertIsDisplayed()
    }

    @Test
    fun addEditBirthdayScreen_hidesAdvanceNotificationOptions_whenNotificationsDisabled() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = LocalDate.of(1990, 1, 1),
                    notes = "",
                    notificationsEnabled = false,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Advance Notification").assertDoesNotExist()
    }

    @Test
    fun addEditBirthdayScreen_opensDatePicker_whenBirthDateFieldClicked() {
        // When
        composeTestRule.setContent {
            BirthdayReminderTheme {
                AddEditBirthdayScreen(
                    name = "Test User",
                    birthDate = null,
                    notes = "",
                    notificationsEnabled = true,
                    advanceNotificationDays = 0,
                    nameErrors = emptyList(),
                    birthDateErrors = emptyList(),
                    notesErrors = emptyList(),
                    isLoading = false,
                    isEditMode = false,
                    onNameChange = {},
                    onBirthDateChange = {},
                    onNotesChange = {},
                    onNotificationsEnabledChange = {},
                    onAdvanceNotificationDaysChange = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Birth Date").performClick()

        // Then - Date picker should be displayed (this would depend on the actual implementation)
        // The exact assertion would depend on how the date picker is implemented
        composeTestRule.onNodeWithText("Select date").assertIsDisplayed()
    }
}
