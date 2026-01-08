package com.birthdayreminder.domain.di

import com.birthdayreminder.data.notification.AlarmScheduler
import com.birthdayreminder.data.notification.NotificationHelper
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.domain.error.ErrorHandler
import com.birthdayreminder.domain.usecase.AddBirthdayUseCase
import com.birthdayreminder.domain.usecase.CalculateCountdownUseCase
import com.birthdayreminder.domain.usecase.CancelNotificationUseCase
import com.birthdayreminder.domain.usecase.DeleteBirthdayUseCase
import com.birthdayreminder.domain.usecase.GetAllBirthdaysUseCase
import com.birthdayreminder.domain.usecase.ScheduleNotificationUseCase
import com.birthdayreminder.domain.usecase.UpdateBirthdayUseCase
import com.birthdayreminder.domain.util.SafeDateCalculator
import com.birthdayreminder.domain.validation.BirthdayValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing domain layer dependencies.
 * Configures use cases and business logic components.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    /**
     * Provides the GetAllBirthdaysUseCase.
     */
    @Provides
    @Singleton
    fun provideGetAllBirthdaysUseCase(
        repository: BirthdayRepository,
        calculateCountdownUseCase: CalculateCountdownUseCase,
    ): GetAllBirthdaysUseCase {
        return GetAllBirthdaysUseCase(repository, calculateCountdownUseCase)
    }

    /**
     * Provides the AddBirthdayUseCase.
     */
    @Provides
    @Singleton
    fun provideAddBirthdayUseCase(
        repository: BirthdayRepository,
        scheduleNotificationUseCase: ScheduleNotificationUseCase,
        birthdayValidator: BirthdayValidator,
    ): AddBirthdayUseCase {
        return AddBirthdayUseCase(
            repository,
            scheduleNotificationUseCase,
            birthdayValidator,
        )
    }

    /**
     * Provides the UpdateBirthdayUseCase.
     */
    @Provides
    @Singleton
    fun provideUpdateBirthdayUseCase(
        repository: BirthdayRepository,
        scheduleNotificationUseCase: ScheduleNotificationUseCase,
        cancelNotificationUseCase: CancelNotificationUseCase,
        birthdayValidator: BirthdayValidator,
    ): UpdateBirthdayUseCase {
        return UpdateBirthdayUseCase(
            repository,
            scheduleNotificationUseCase,
            cancelNotificationUseCase,
            birthdayValidator,
        )
    }

    /**
     * Provides the DeleteBirthdayUseCase.
     */
    @Provides
    @Singleton
    fun provideDeleteBirthdayUseCase(
        repository: BirthdayRepository,
        cancelNotificationUseCase: CancelNotificationUseCase,
    ): DeleteBirthdayUseCase {
        return DeleteBirthdayUseCase(repository, cancelNotificationUseCase)
    }

    /**
     * Provides the CalculateCountdownUseCase.
     */
    @Provides
    @Singleton
    fun provideCalculateCountdownUseCase(safeDateCalculator: SafeDateCalculator): CalculateCountdownUseCase {
        return CalculateCountdownUseCase(safeDateCalculator)
    }

    /**
     * Provides the ScheduleNotificationUseCase.
     */
    @Provides
    @Singleton
    fun provideScheduleNotificationUseCase(alarmScheduler: AlarmScheduler): ScheduleNotificationUseCase {
        return ScheduleNotificationUseCase(alarmScheduler)
    }

    /**
     * Provides the CancelNotificationUseCase.
     */
    @Provides
    @Singleton
    fun provideCancelNotificationUseCase(
        alarmScheduler: AlarmScheduler,
        notificationHelper: NotificationHelper,
    ): CancelNotificationUseCase {
        return CancelNotificationUseCase(alarmScheduler, notificationHelper)
    }

    /**
     * Provides the BirthdayValidator.
     */
    @Provides
    @Singleton
    fun provideBirthdayValidator(): BirthdayValidator {
        return BirthdayValidator()
    }

    /**
     * Provides the ErrorHandler.
     */
    @Provides
    @Singleton
    fun provideErrorHandler(): ErrorHandler {
        return ErrorHandler()
    }

    /**
     * Provides the SafeDateCalculator.
     */
    @Provides
    @Singleton
    fun provideSafeDateCalculator(errorHandler: ErrorHandler): SafeDateCalculator {
        return SafeDateCalculator(errorHandler)
    }
}
