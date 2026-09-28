package com.birthdayreminder.ui.screens

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.domain.validation.BirthdayValidator
import com.birthdayreminder.ui.viewmodel.AddEditBirthdayUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Covers the wizard's step behaviour and the validation copy that goes with it.
 *
 * The previous version of this file tested a single-screen form that the
 * three-step wizard replaced, so its assertions no longer described anything
 * the app does. These check what exists now.
 */
@RunWith(AndroidJUnit4::class)
class AddEditBirthdayWizardTest {
    @Test
    fun a_blank_name_blocks_continuing_and_says_why() {
        val state = AddEditBirthdayUiState(name = "", step = 1)

        assertEquals(BirthdayValidator.ERROR_NAME_REQUIRED, state.currentStepError(BirthdayValidator()))
    }

    @Test
    fun a_valid_name_reports_no_error() {
        val state = AddEditBirthdayUiState(name = "Amma", step = 1)

        assertNull(state.currentStepError(BirthdayValidator()))
    }

    @Test
    fun step_two_requires_a_birth_date() {
        val state =
            AddEditBirthdayUiState(
                name = "Amma",
                birthDate = null,
                step = 2,
            )

        assertEquals(BirthdayValidator.ERROR_BIRTH_DATE_REQUIRED, state.currentStepError(BirthdayValidator()))
    }

    @Test
    fun a_future_birth_date_is_rejected_on_step_two() {
        val state =
            AddEditBirthdayUiState(
                name = "Amma",
                birthDate = LocalDate.now().plusDays(1),
                step = 2,
            )

        assertEquals(BirthdayValidator.ERROR_BIRTH_DATE_FUTURE, state.currentStepError(BirthdayValidator()))
    }

    @Test
    fun step_three_rejects_notes_past_the_limit() {
        val state =
            AddEditBirthdayUiState(
                name = "Amma",
                birthDate = LocalDate.of(1974, 11, 14),
                notes = "x".repeat(BirthdayValidator.MAX_NOTES_LENGTH + 1),
                step = 3,
            )

        assertEquals(BirthdayValidator.ERROR_NOTES_TOO_LONG, state.currentStepError(BirthdayValidator()))
    }

    @Test
    fun a_step_error_routes_to_the_field_that_caused_it() {
        val state = AddEditBirthdayUiState()

        val nameState = state.withFieldError(1, BirthdayValidator.ERROR_NAME_REQUIRED)
        assertEquals(BirthdayValidator.ERROR_NAME_REQUIRED, nameState.nameError)
        assertNull(nameState.birthDateError)
        assertNull(nameState.notesError)

        val dateState = state.withFieldError(2, BirthdayValidator.ERROR_BIRTH_DATE_REQUIRED)
        assertEquals(BirthdayValidator.ERROR_BIRTH_DATE_REQUIRED, dateState.birthDateError)
        assertNull(dateState.nameError)

        val notesState = state.withFieldError(3, BirthdayValidator.ERROR_NOTES_TOO_LONG)
        assertEquals(BirthdayValidator.ERROR_NOTES_TOO_LONG, notesState.notesError)
    }

    @Test
    fun form_data_survives_a_step_error() {
        val filled =
            AddEditBirthdayUiState(
                name = "Amma",
                birthDate = LocalDate.of(1974, 11, 14),
                notes = "Likes jasmine",
                relationship = "Family",
            )

        val afterError = filled.withFieldError(1, "Some error")

        assertEquals("Amma", afterError.name)
        assertEquals(LocalDate.of(1974, 11, 14), afterError.birthDate)
        assertEquals("Likes jasmine", afterError.notes)
        assertEquals("Family", afterError.relationship)
    }
}
