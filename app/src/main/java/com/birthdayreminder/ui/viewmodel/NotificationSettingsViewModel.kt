package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.notification.NotificationHelper
import com.birthdayreminder.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val areNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isMaterialYouEnabled: Boolean = false,
    val defaultHour: Int = 9,
    val defaultMinute: Int = 0,
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
                    _uiState.update { it.copy(isMaterialYouEnabled = enabled) }
                }
            }
            viewModelScope.launch {
                settingsRepository.defaultNotificationTime.collect { (h, m) ->
                    _uiState.update { it.copy(defaultHour = h, defaultMinute = m) }
                }
            }
        }

        private fun checkNotificationPermission() {
            viewModelScope.launch {
                val enabled = notificationHelper.areNotificationsEnabled()
                _uiState.update { it.copy(areNotificationsEnabled = enabled) }
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

        fun updateDefaultTime(
            hour: Int,
            minute: Int,
        ) {
            viewModelScope.launch {
                settingsRepository.setDefaultNotificationTime(hour, minute)
            }
        }
    }
