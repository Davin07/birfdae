package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.repository.BirthdayRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Records that the user does not want to celebrate a birthday this year.
 *
 * Offered by the overdue state alongside "Send a belated wish". Without
 * somewhere to store the answer, the same person would be asked again on every
 * launch for the whole window, so the decision is persisted against the year.
 */
class SkipBirthdayForYearUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
    ) {
        /**
         * Marks a birthday as skipped for [year].
         *
         * A no-op when the row has gone, so a tap racing a delete does not
         * throw.
         *
         * @param birthdayId the person to skip
         * @param year the year being declined
         * @return true when the record was updated
         */
        suspend operator fun invoke(
            birthdayId: Long,
            year: Int = LocalDate.now().year,
        ): Boolean {
            val birthday = birthdayRepository.getBirthdayById(birthdayId) ?: return false
            birthdayRepository.updateBirthday(birthday.copy(skippedYear = year))
            return true
        }
    }
