package com.birthdayreminder.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.birthdayreminder.data.local.entity.Birthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the v3 to v4 migration preserves real data.
 *
 * This is the one change in the redesign that touches persisted user data, so
 * "the build passes" is not evidence — the assertion is that a birthday saved
 * before the upgrade is still there, with every field intact, afterwards.
 */
@RunWith(AndroidJUnit4::class)
class Migration3To4Test {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrates_3_to_4_preserves_existing_rows() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, notes, notificationsEnabled,
                    advanceNotificationDays, notificationHour, notificationMinute,
                    imageUri, relationship, isPinned, notificationOffsets, notificationTime, createdAt)
                VALUES (1, 'Amma', '1967-09-27', 'Loves jasmine', 1, 1, 9, 0,
                    NULL, 'Family', 1, '0,3', '09:00:00', '2024-01-15T10:30:00')
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        db.query("SELECT * FROM birthdays WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("Amma", c.getString(c.getColumnIndexOrThrow("name")))
            assertEquals("1967-09-27", c.getString(c.getColumnIndexOrThrow("birthDate")))
            assertEquals("Loves jasmine", c.getString(c.getColumnIndexOrThrow("notes")))
            assertEquals("Family", c.getString(c.getColumnIndexOrThrow("relationship")))
            assertEquals(1, c.getInt(c.getColumnIndexOrThrow("isPinned")))
            assertEquals("0,3", c.getString(c.getColumnIndexOrThrow("notificationOffsets")))
            assertEquals("2024-01-15T10:30:00", c.getString(c.getColumnIndexOrThrow("createdAt")))

            // The new column exists and defaults to "never skipped", so no
            // existing birthday is suddenly treated as declined.
            assertEquals(
                Birthday.NO_SKIPPED_YEAR,
                c.getInt(c.getColumnIndexOrThrow("skippedYear")),
            )
        }

        db.close()
    }

    @Test
    fun migrated_rows_are_readable_by_the_entity() {
        helper.createDatabase(TEST_DB + "2", 3).apply {
            execSQL(
                """
                INSERT INTO birthdays (id, name, birthDate, notificationsEnabled,
                    advanceNotificationDays, notificationOffsets, createdAt)
                VALUES (7, 'Karthik', '1991-10-17', 1, 0, '0', '2025-03-01T08:00:00')
                """.trimIndent(),
            )
            close()
        }

        val raw = helper.runMigrationsAndValidate(TEST_DB + "2", 4, true, AppDatabase.MIGRATION_3_4)

        // Open the migrated file with Room so the entity mapping is exercised,
        // not just the SQL. runMigrationsAndValidate returns a raw cursor db.
        val context =
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .targetContext
        val room =
            androidx.room.Room
                .databaseBuilder(context, AppDatabase::class.java, TEST_DB + "2")
                .addMigrations(*AppDatabase.ALL_MIGRATIONS)
                .allowMainThreadQueries()
                .build()
        val dao = room.birthdayDao()

        val loaded = kotlinx.coroutines.runBlocking { dao.getBirthdayById(7) }
        assertTrue("Row vanished during migration", loaded != null)
        requireNotNull(loaded)
        assertEquals("Karthik", loaded.name)
        assertEquals(Birthday.NO_SKIPPED_YEAR, loaded.skippedYear)

        room.close()
        raw.close()
    }

    private companion object {
        // MigrationTestHelper needs a real file-backed database.
        const val TEST_DB = "migration_3_4_test.db"
    }
}
