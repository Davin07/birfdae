package com.birthdayreminder.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [BackupDataSource] implementation backed by the Android Storage Access Framework.
 * All I/O is dispatched off the main thread and uses explicit UTF-8 encoding.
 */
@Singleton
class ContentResolverBackupDataSource
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BackupDataSource {
        override suspend fun readBackup(uri: Uri): Result<BackupFileDto> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val jsonString =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                                reader.readText()
                            }
                        } ?: throw IllegalStateException("Failed to open input stream for $uri")

                    parseBackupFile(jsonString)
                }.onFailure { Timber.e(it, "Error reading backup file from $uri") }
            }

        override suspend fun writeBackup(
            uri: Uri,
            backupFile: BackupFileDto,
        ): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    // Serialize and verify before touching the destination so a serialization
                    // failure cannot leave a truncated or corrupt backup file behind.
                    val jsonString = backupFile.toJsonString()
                    parseBackupFile(jsonString)

                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.writer(Charsets.UTF_8).use { writer ->
                            writer.write(jsonString)
                            writer.flush()
                        }
                    } ?: throw IllegalStateException("Failed to open output stream for $uri")
                }.onFailure { Timber.e(it, "Error writing backup file to $uri") }
            }

        companion object {
            /**
             * Generates a default backup file name with a timestamp.
             *
             * @return Default backup file name, e.g. birthday_reminder_backup_20260101_120000.json
             */
            fun generateDefaultBackupFileName(): String {
                val timestamp =
                    java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                return "birthday_reminder_backup_$timestamp.json"
            }
        }
    }
