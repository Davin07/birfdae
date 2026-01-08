package com.birthdayreminder.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isMaterialYouEnabled: Flow<Boolean>
    suspend fun setMaterialYouEnabled(enabled: Boolean)
}
