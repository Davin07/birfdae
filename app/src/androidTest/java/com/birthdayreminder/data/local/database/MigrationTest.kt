package com.birthdayreminder.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db =
            helper.createDatabase(TEST_DB, 2).apply {
                // Insert data using version 2 schema
                execSQL(
                    "INSERT INTO birthdays (name, birthDate, notes, notificationsEnabled, advanceNotificationDays, notificationHour, notificationMinute, createdAt) VALUES ('Test User', '1990-01-01', 'Notes', 1, 0, 9, 0, '2024-01-01T10:00:00')",
                )
                close()
            }

        // Migrate to version 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)

        // Validate that the new columns exist and have default values
        val cursor = db.query("SELECT * FROM birthdays WHERE name = 'Test User'")
        if (cursor.moveToFirst()) {
            // Check imageUri
            val imageUriIndex = cursor.getColumnIndex("imageUri")
            assert(imageUriIndex != -1)
            assert(cursor.isNull(imageUriIndex))

            // Check relationship
            val relationshipIndex = cursor.getColumnIndex("relationship")
            assert(relationshipIndex != -1)
            assert(cursor.isNull(relationshipIndex))

            // Check isPinned
            val isPinnedIndex = cursor.getColumnIndex("isPinned")
            assert(isPinnedIndex != -1)
            assert(cursor.getInt(isPinnedIndex) == 0) // Default false

            // Check notificationOffsets
            val notificationOffsetsIndex = cursor.getColumnIndex("notificationOffsets")
            assert(notificationOffsetsIndex != -1)
            val offsets = cursor.getString(notificationOffsetsIndex)
            assert(offsets == "")

            // Check notificationTime
            val notificationTimeIndex = cursor.getColumnIndex("notificationTime")
            assert(notificationTimeIndex != -1)
            assert(cursor.isNull(notificationTimeIndex))
        } else {
            throw AssertionError("Row not found after migration")
        }
        cursor.close()
    }
}
