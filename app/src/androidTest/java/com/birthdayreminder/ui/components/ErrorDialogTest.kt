package com.birthdayreminder.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.domain.error.ErrorResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorDialog_displaysErrorTitle_andMessage() {
        val errorResult = ErrorResult("Test error message", isRecoverable = true)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = {},
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test error message").assertIsDisplayed()
    }

    @Test
    fun errorDialog_displaysRetryButton_whenErrorIsRecoverable() {
        val errorResult = ErrorResult("Test error", isRecoverable = true)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = {},
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun errorDialog_displaysOkButton_whenErrorIsNotRecoverable() {
        val errorResult = ErrorResult("Test error", isRecoverable = false)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = null,
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun errorDialog_callsOnRetry_whenRetryButtonClicked() {
        var retryClicked = false
        val errorResult = ErrorResult("Test error", isRecoverable = true)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = { retryClicked = true },
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()
        assert(retryClicked)
    }

    @Test
    fun errorDialog_callsOnDismiss_whenCancelButtonClicked() {
        var dismissClicked = false
        val errorResult = ErrorResult("Test error", isRecoverable = true)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = {},
                        onDismiss = { dismissClicked = true },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissClicked)
    }

    @Test
    fun errorDialog_callsOnDismiss_whenOkButtonClicked() {
        var dismissClicked = false
        val errorResult = ErrorResult("Test error", isRecoverable = false)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ErrorDialog(
                        error = errorResult,
                        onRetry = null,
                        onDismiss = { dismissClicked = true },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("OK").performClick()
        assert(dismissClicked)
    }

    @Test
    fun simpleErrorDialog_displaysTitle_andMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SimpleErrorDialog(
                        message = "Simple error message",
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Simple error message").assertIsDisplayed()
    }

    @Test
    fun simpleErrorDialog_displaysOkButton() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SimpleErrorDialog(
                        message = "Simple error message",
                        onDismiss = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun simpleErrorDialog_callsOnDismiss_whenOkButtonClicked() {
        var dismissClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SimpleErrorDialog(
                        message = "Simple error message",
                        onDismiss = { dismissClicked = true },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("OK").performClick()
        assert(dismissClicked)
    }
}
