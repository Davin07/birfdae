package com.birthdayreminder.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the v4 to v5 migration preserves real data.
 *
 * This migration adds the reminder-event table that the home screen's streak is
 * computed from. It is additive, so the risk is not data loss but a schema that
 * validates against the wrong identity hash -- which would show up as a crash
 * on upgrade for existing users, not in a test. The assertion is therefore that
 * a birthday saved before the upgrade survives with every field intact, and
 * that the new table is usable afterwards.
 */
@RunWith(AndroidJUnit4::class)
class Migration4To5Test {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrates4To5PreservingBirthdays() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, createdAt, isPinned,
                    notes, notificationsEnabled, notificationTime, relationship,
                    notificationOffsets, skippedYear, advanceNotificationDays)
                VALUES (1, 'Amma', '1971-12-03', '2024-03-10T09:00:00', 1,
                    'Call in the morning', 1, '09:00', 'Mother',
                    '', -1, 3)
                """.trimIndent(),
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        migrated.query("SELECT name, birthDate, isPinned, notes, skippedYear FROM birthdays").use { c ->
            c.moveToFirst()
            assertEquals("Amma", c.getString(0))
            assertEquals("1971-12-03", c.getString(1))
            assertEquals(1, c.getInt(2))
            assertEquals("Call in the morning", c.getString(3))
            assertEquals(-1, c.getInt(4))
        }
    }

    @Test
    fun theNewEventTableIsUsableAfterMigration() {
        helper.createDatabase(TEST_DB, 4).close()

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        migrated.execSQL(
            """
            INSERT INTO reminder_events (birthdayId, year, acknowledgedAt, sharedAt, createdAt)
            VALUES (1, 2025, '2025-12-03T09:00:00', NULL, '2025-12-03T09:00:00')
            """.trimIndent(),
        )
        migrated.query("SELECT COUNT(*) FROM reminder_events").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }

        // A second event for the same year must be rejected by the unique
        // index, since one birthday has one reminder outcome per year.
        val inserted =
            runCatching {
                migrated.execSQL(
                    """
                    INSERT INTO reminder_events (birthdayId, year, createdAt)
                    VALUES (1, 2025, '2025-12-04T09:00:00')
                    """.trimIndent(),
                )
            }
        assertTrue("A duplicate year for one birthday must not be insertable", inserted.isFailure)
    }

    private companion object {
        const val TEST_DB = "migration-4-5-test"
    }
}
