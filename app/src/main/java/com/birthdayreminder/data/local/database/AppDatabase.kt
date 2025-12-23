package com.birthdayreminder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.birthdayreminder.data.local.converter.DateConverters
import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.entity.Birthday

/**
 * Room database class for the Birthday Reminder app.
 * Manages the local SQLite database with birthday data.
 */
@Database(
    entities = [Birthday::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides access to the Birthday DAO for database operations.
     */
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        /**
         * Database name for the local SQLite database.
         */
        const val DATABASE_NAME = "birthday_reminder_database"

        /**
         * Migration from version 1 to version 2.
         * Adds notificationHour and notificationMinute columns to the birthdays table.
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Add notificationHour column (nullable integer)
                    database.execSQL("ALTER TABLE birthdays ADD COLUMN notificationHour INTEGER")

                    // Add notificationMinute column (nullable integer)
                    database.execSQL("ALTER TABLE birthdays ADD COLUMN notificationMinute INTEGER")
                }
            }

        /**
         * Creates and configures the Room database instance.
         * This method should be called from the Hilt module.
         */
        fun create(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
            return builder
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
    }
}
