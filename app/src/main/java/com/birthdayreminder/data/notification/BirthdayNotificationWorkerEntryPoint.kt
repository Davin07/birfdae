package com.birthdayreminder.data.notification

import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BirthdayNotificationWorkerEntryPoint {
    fun birthdayRepository(): BirthdayRepository
    fun calculateCountdownUseCase(): CalculateCountdownUseCase
    fun notificationHelper(): NotificationHelper
}