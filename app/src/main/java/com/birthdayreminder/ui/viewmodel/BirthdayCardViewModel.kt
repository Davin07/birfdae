package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.data.repository.ReminderEventRepository
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Backs the shareable birthday card.
 *
 * Everything here is derived from columns that actually exist. There is no
 * reminder-history table, so this cannot and does not compute a streak - the
 * card shows the age turning and the year the person was added instead.
 */
@HiltViewModel
class BirthdayCardViewModel
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val calculateCountdownUseCase: CalculateCountdownUseCase,
        private val reminderEventRepository: ReminderEventRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BirthdayCardUiState())
        val uiState: StateFlow<BirthdayCardUiState> = _uiState.asStateFlow()

        private val birthdayId: Long? = savedStateHandle.get<String>("birthdayId")?.toLongOrNull()

        /**
         * Records that a card for this birthday was shared.
         *
         * Kept off the acknowledgement streak on purpose. Sharing is the growth
         * signal -- it tells us whose cards actually get sent -- but it is not
         * proof the message arrived, so it must never be counted as proof the
         * user remembered.
         *
         * @param year the year the birthday falls in, so an early share is not
         *   credited to the wrong year
         */
        fun recordShare(year: Int) {
            val id = birthdayId ?: return
            viewModelScope.launch {
                reminderEventRepository.recordShared(id, year)
            }
        }

        private companion object {
            /**
             * The schema has no user profile, so there is no name to attribute
             * the card to. Attributing to the app itself is honest; inventing
             * a "you" would not be.
             */
            const val DEFAULT_SENDER = "Birf Dae"
        }

        init {
            load()
        }

        private fun load() {
            viewModelScope.launch {
                val id = birthdayId
                if (id == null) {
                    _uiState.value = BirthdayCardUiState()
                    return@launch
                }

                val birthday = birthdayRepository.getBirthdayById(id)
                if (birthday == null) {
                    _uiState.value = BirthdayCardUiState()
                    return@launch
                }

                val countdown = calculateCountdownUseCase.calculateCountdown(birthday)

                _uiState.value =
                    BirthdayCardUiState(
                        birthday = birthday,
                        nextOccurrence = countdown.nextOccurrence,
                        ageTurning = countdown.age,
                        senderName = DEFAULT_SENDER,
                        isLoading = false,
                    )
            }
        }
    }

/**
 * Immutable snapshot for the card screen.
 *
 * @property birthday the person, or null while loading or after deletion
 * @property nextOccurrence the upcoming date the card celebrates
 * @property ageTurning the age reached on that date
 * @property senderName shown in the attribution line
 * @property isLoading true until the first load resolves
 */
data class BirthdayCardUiState(
    val birthday: Birthday? = null,
    val nextOccurrence: LocalDate? = null,
    val ageTurning: Int = 0,
    val senderName: String = "Someone",
    val isLoading: Boolean = true,
)
