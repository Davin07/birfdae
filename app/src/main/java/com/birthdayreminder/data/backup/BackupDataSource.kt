package com.birthdayreminder.data.backup

import android.net.Uri

/**
 * Abstraction over backup file I/O.
 *
 * Isolates platform storage access (ContentResolver) from business logic so that import/export
 * orchestration can be unit tested without an Android device.
 */
interface BackupDataSource {
    /**
     * Reads and parses a backup file.
     *
     * @param uri URI of the backup file to read
     * @return Result containing the parsed backup file, or a failure if reading or parsing failed
     */
    suspend fun readBackup(uri: Uri): Result<BackupFileDto>

    /**
     * Writes a backup file atomically. The payload is fully serialized and parse-verified before
     * any bytes are written to the destination, so a failure never leaves a truncated file behind.
     *
     * @param uri URI where the backup file should be written
     * @param backupFile Backup contents to write
     * @return Result indicating success or failure
     */
    suspend fun writeBackup(
        uri: Uri,
        backupFile: BackupFileDto,
    ): Result<Unit>
}
