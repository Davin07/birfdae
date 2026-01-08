package com.birthdayreminder.data.settings

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val _isMaterialYouEnabled = MutableStateFlow(prefs.getBoolean(KEY_MATERIAL_YOU, false))

    override val isMaterialYouEnabled: Flow<Boolean> = _isMaterialYouEnabled.asStateFlow()

    override suspend fun setMaterialYouEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_MATERIAL_YOU, enabled) }
        _isMaterialYouEnabled.value = enabled
    }

    companion object {
        private const val KEY_MATERIAL_YOU = "material_you_enabled"
    }
}
