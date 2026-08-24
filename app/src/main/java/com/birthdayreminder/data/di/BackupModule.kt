package com.birthdayreminder.data.di

import com.birthdayreminder.data.backup.BackupDataSource
import com.birthdayreminder.data.backup.ContentResolverBackupDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing backup-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    /**
     * Binds the ContentResolver-backed implementation to the BackupDataSource interface.
     */
    @Binds
    @Singleton
    abstract fun bindBackupDataSource(impl: ContentResolverBackupDataSource): BackupDataSource
}
