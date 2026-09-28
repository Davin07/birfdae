package com.birthdayreminder.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the v5 to v6 migration preserves real data.
 *
 * This migration adds the card's message tone. The column is nullable with no
 * default on purpose: an existing row must land on null, which the card reads
 * as "never chosen" and resolves to its default tone. Giving it a default of
 * 'WARM' would instead write a value the user never picked and make a later
 * change of default look like a data migration.
 *
 * As with every additive migration, the real risk is a schema that validates
 * against the wrong identity hash, which surfaces as a crash on upgrade for
 * existing users rather than as a failing assertion. So the assertions are that
 * a birthday saved before the upgrade survives with every field intact, and
 * that the new column is readable and writable afterwards.
 */
@RunWith(AndroidJUnit4::class)
class Migration5To6Test {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrates5To6PreservingBirthdays() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, createdAt, isPinned,
                    notes, notificationsEnabled, notificationTime, relationship,
                    notificationOffsets, skippedYear, advanceNotificationDays)
                VALUES (1, 'Amma', '1971-12-03', '2024-03-10T09:00:00', 1,
                    'Call in the morning', 1, '09:00', 'Parents',
                    '', -1, 3)
                """.trimIndent(),
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        migrated.query("SELECT name, birthDate, relationship, skippedYear, cardTone FROM birthdays").use { c ->
            c.moveToFirst()
            assertEquals("Amma", c.getString(0))
            assertEquals("1971-12-03", c.getString(1))
            assertEquals("Parents", c.getString(2))
            assertEquals(-1, c.getInt(3))
            // Null, not a default: the user never chose a tone for this person.
            assertNull(c.getString(4))
        }
    }

    @Test
    fun theNewColumnIsWritableAfterMigration() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, createdAt, isPinned,
                    notes, notificationsEnabled, notificationTime, relationship,
                    notificationOffsets, skippedYear, advanceNotificationDays)
                VALUES (1, 'Amma', '1971-12-03', '2024-03-10T09:00:00', 0,
                    NULL, 1, NULL, 'Parents', '', -1, 0)
                """.trimIndent(),
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        migrated.execSQL("UPDATE birthdays SET cardTone = 'FUNNY' WHERE id = 1")

        migrated.query("SELECT cardTone FROM birthdays WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("FUNNY", c.getString(0))
        }
    }

    @Test
    fun theReminderEventTableSurvivesTheUpgrade() {
        // The engagement history is the one thing the app cannot rebuild: it is
        // written by the user noticing things, not derived from their list. A
        // migration that dropped it would silently reset every streak.
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, createdAt, isPinned,
                    notes, notificationsEnabled, notificationTime, relationship,
                    notificationOffsets, skippedYear, advanceNotificationDays)
                VALUES (1, 'Amma', '1971-12-03', '2024-03-10T09:00:00', 0,
                    NULL, 1, NULL, 'Parents', '', -1, 0)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO reminder_events (birthdayId, year, acknowledgedAt, sharedAt, createdAt)
                VALUES (1, 2025, '2025-03-02T09:00:00', NULL, '2025-03-02T09:00:00')
                """.trimIndent(),
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        migrated.query("SELECT year, acknowledgedAt FROM reminder_events WHERE birthdayId = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(2025, c.getInt(0))
            assertEquals("2025-03-02T09:00:00", c.getString(1))
        }
    }

    private companion object {
        const val TEST_DB = "migration-5-6-test"
    }
}
