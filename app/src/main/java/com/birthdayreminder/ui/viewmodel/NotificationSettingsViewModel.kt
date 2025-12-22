package com.birthdayreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdayreminder.data.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val areNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkNotificationPermission()
    }
    
    private fun checkNotificationPermission() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                areNotificationsEnabled = notificationHelper.areNotificationsEnabled()
            )
        }
    }
    
    fun refreshNotificationStatus() {
        checkNotificationPermission()
    }
}