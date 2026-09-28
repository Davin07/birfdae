package com.birthdayreminder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.birthdayreminder.data.local.converter.DateConverters
import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.dao.ReminderEventDao
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.local.entity.ReminderEvent

/**
 * Room database class for the Birthday Reminder app.
 * Manages the local SQLite database with birthday data.
 */
@Database(
    entities = [Birthday::class, ReminderEvent::class],
    version = 6,
    exportSchema = true,
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides access to the Birthday DAO for database operations.
     */
    abstract fun birthdayDao(): BirthdayDao

    /**
     * Provides access to the reminder history DAO.
     *
     * Separate from [birthdayDao] because it records a different kind of fact:
     * what the user did about a birthday, rather than what the birthday is.
     */
    abstract fun reminderEventDao(): ReminderEventDao

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
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Add notificationHour column (nullable integer)
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN notificationHour INTEGER")

                    // Add notificationMinute column (nullable integer)
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN notificationMinute INTEGER")
                }
            }

        /**
         * Migration from version 2 to version 3.
         * Adds imageUri, relationship, isPinned, notificationOffsets, notificationTime.
         */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN imageUri TEXT")
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN relationship TEXT")
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN notificationOffsets TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN notificationTime TEXT")
                }
            }

        /**
         * Migration from version 3 to version 4.
         * Adds skippedYear, which records that the user chose not to celebrate a
         * given birthday this year. -1 means "never skipped", matching
         * Birthday.NO_SKIPPED_YEAR.
         */
        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE birthdays ADD COLUMN skippedYear INTEGER NOT NULL DEFAULT -1")
                }
            }

        /**
         * Migration from version 4 to version 5.
         * Creates reminder_events, which records that a birthday notification
         * was opened and, separately, that a card was shared. This is what the
         * home screen's streak is computed from, and it is empty for every
         * existing user: the app had no history table, so a streak can only
         * ever count from the day this migration lands.
         */
        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `reminder_events` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `birthdayId` INTEGER NOT NULL,
                            `year` INTEGER NOT NULL,
                            `acknowledgedAt` TEXT,
                            `sharedAt` TEXT,
                            `createdAt` TEXT NOT NULL DEFAULT '',
                            FOREIGN KEY(`birthdayId`) REFERENCES `birthdays`(`id`)
                                ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )
                    // One row per person per year. This is what makes a
                    // double-tap count once instead of twice.
                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX IF NOT EXISTS
                            `index_reminder_events_birthdayId_year`
                            ON `reminder_events` (`birthdayId`, `year`)
                        """.trimIndent(),
                    )
                }
            }

        /**
         * Adds the card's message tone.
         *
         * Nullable with no default, so every existing row lands on null and
         * reads as "never chosen", which the card treats as its default tone
         * rather than as a value the user picked.
         */
        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `birthdays` ADD COLUMN `cardTone` TEXT")
                }
            }

        /**
         * Every migration, in order.
         *
         * A test that opens a migrated file with [androidx.room.Room] directly
         * has to register these itself, or Room refuses to open a database
         * whose file is older than the current schema. Keeping the list here
         * stops that being a per-test chore that breaks silently the next time
         * the schema is bumped.
         */
        val ALL_MIGRATIONS: Array<Migration> =
            arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)

        /**
         * Creates and configures the Room database instance.
         * This method should be called from the Hilt module.
         */
        fun create(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
            return builder
                .addMigrations(*ALL_MIGRATIONS)
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
    }
}
