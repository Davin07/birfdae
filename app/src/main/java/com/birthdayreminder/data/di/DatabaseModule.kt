package com.birthdayreminder.data.di

import android.content.Context
import androidx.room.Room
import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.dao.ReminderEventDao
import com.birthdayreminder.data.local.database.AppDatabase
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.data.repository.BirthdayRepositoryImpl
import com.birthdayreminder.data.repository.ReminderEventRepository
import com.birthdayreminder.data.repository.ReminderEventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 * Configures Room database, DAOs, and repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    /**
     * Binds the BirthdayRepositoryImpl to the BirthdayRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindBirthdayRepository(birthdayRepositoryImpl: BirthdayRepositoryImpl): BirthdayRepository

    /**
     * Binds the ReminderEventRepositoryImpl to the ReminderEventRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindReminderEventRepository(
        reminderEventRepositoryImpl: ReminderEventRepositoryImpl,
    ): ReminderEventRepository

    companion object {
        /**
         * Provides the Room database instance.
         * Configures the database with proper settings and migrations.
         */
        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context,
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME,
            ).let { builder ->
                AppDatabase.create(builder)
            }
        }

        /**
         * Provides the BirthdayDao from the database.
         */
        @Provides
        fun provideBirthdayDao(database: AppDatabase): BirthdayDao {
            return database.birthdayDao()
        }

        /**
         * Provides the ReminderEventDao from the database.
         */
        @Provides
        fun provideReminderEventDao(database: AppDatabase): ReminderEventDao {
            return database.reminderEventDao()
        }
    }
}
