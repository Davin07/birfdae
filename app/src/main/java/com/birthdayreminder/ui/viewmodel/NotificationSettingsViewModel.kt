package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.notification.NotificationHelper
import com.birthdayreminder.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val areNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isMaterialYouEnabled: Boolean = false,
)

@HiltViewModel
class NotificationSettingsViewModel
    @Inject
    constructor(
        private val notificationHelper: NotificationHelper,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NotificationSettingsUiState())
        val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

        init {
            checkNotificationPermission()
            viewModelScope.launch {
                settingsRepository.isMaterialYouEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isMaterialYouEnabled = enabled)
                }
            }
        }

        private fun checkNotificationPermission() {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        areNotificationsEnabled = notificationHelper.areNotificationsEnabled(),
                    )
            }
        }

        fun refreshNotificationStatus() {
            checkNotificationPermission()
        }

        fun toggleMaterialYou(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.setMaterialYouEnabled(enabled)
            }
        }
    }
