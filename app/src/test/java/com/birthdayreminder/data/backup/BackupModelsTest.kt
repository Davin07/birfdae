package com.birthdayreminder.data.backup

import com.birthdayreminder.data.local.entity.Birthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BackupModelsTest {
    @Test
    fun `backup dto round trip preserves all fields`() {
        val original =
            BackupFileDto(
                version = BACKUP_FORMAT_VERSION,
                exportDate = "2026-01-01T12:00:00",
                birthdays =
                    listOf(
                        BirthdayBackupDto(
                            name = "Alice",
                            birthDate = "1990-05-15",
                            notes = "Friend",
                            notificationsEnabled = true,
                            advanceNotificationDays = 3,
                            notificationHour = 9,
                            notificationMinute = 30,
                            imageUri = null,
                            relationship = "Family",
                            isPinned = true,
                            notificationOffsets = listOf(0, 7),
                            notificationTime = "08:45:00",
                            createdAt = "2025-06-01T10:00:00",
                        ),
                    ),
            )

        val parsed = parseBackupFile(original.toJsonString())

        assertEquals(original, parsed)
    }

    @Test
    fun `legacy version 1 entity-shaped payload can still be parsed`() {
        val legacyJson =
            """
            {
              "version": 1,
              "exportDate": "2025-01-01T10:00:00",
              "birthdays": [
                {
                  "id": 42,
                  "name": "Alice",
                  "birthDate": "1990-05-15",
                  "notes": null,
                  "notificationsEnabled": true,
                  "advanceNotificationDays": 2,
                  "notificationHour": null,
                  "notificationMinute": null,
                  "imageUri": null,
                  "relationship": null,
                  "isPinned": false,
                  "notificationOffsets": [ ],
                  "createdAt": "2024-01-01T09:00:00"
                }
              ]
            }
            """.trimIndent()

        val parsed = parseBackupFile(legacyJson)

        assertEquals(1, parsed.version)
        assertEquals("2025-01-01T10:00:00", parsed.exportDate)
        assertEquals(1, parsed.birthdays.size)
        val entry = parsed.birthdays.first()
        assertEquals("Alice", entry.name)
        assertEquals("1990-05-15", entry.birthDate)
        assertNull(entry.notificationTime)
    }

    @Test
    fun `invalid json fails parsing`() {
        assertThrows(Exception::class.java) {
            parseBackupFile("this is not json")
        }
    }

    @Test
    fun `toEntity maps backup dto to fresh entity`() {
        val dto =
            BirthdayBackupDto(
                name = "Alice",
                birthDate = "1990-05-15",
                notes = "note",
                notificationsEnabled = false,
                advanceNotificationDays = 1,
                notificationHour = 8,
                notificationMinute = 15,
                relationship = "Work",
                isPinned = true,
                notificationOffsets = listOf(3),
                notificationTime = "07:30:00",
                createdAt = "2025-02-02T08:00:00",
            )

        val entity = dto.toEntity()

        assertEquals(0L, entity.id)
        assertEquals("Alice", entity.name)
        assertEquals(LocalDate.of(1990, 5, 15), entity.birthDate)
        assertEquals(LocalTime.of(7, 30), entity.notificationTime)
        assertEquals(LocalDateTime.of(2025, 2, 2, 8, 0, 0), entity.createdAt)
        assertEquals(listOf(3), entity.notificationOffsets)
    }

    @Test
    fun `natural key normalizes name and uses birth date`() {
        val a =
            Birthday(
                name = "  Alice Smith ",
                birthDate = LocalDate.of(1990, 5, 15),
            )
        val b =
            Birthday(
                name = "alice smith",
                birthDate = LocalDate.of(1990, 5, 15),
            )
        val c =
            Birthday(
                name = "alice smith",
                birthDate = LocalDate.of(1991, 5, 15),
            )

        assertEquals(a.naturalKey(), b.naturalKey())
        assertEquals(a.naturalKey(), "alice smith|19900515")
        assertEquals(c.naturalKey(), "alice smith|19910515")
    }
}
