package com.birthdayreminder.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val defaultNotificationTime: Flow<Pair<Int, Int>>

    suspend fun setDefaultNotificationTime(
        hour: Int,
        minute: Int,
    )
}
