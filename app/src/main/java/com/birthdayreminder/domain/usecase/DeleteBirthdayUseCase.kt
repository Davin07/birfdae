package com.birthdayreminder.domain.usecase

import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for deleting birthday entries.
 * Handles birthday deletion with proper validation and error handling.
 */
@Singleton
class DeleteBirthdayUseCase
    @Inject
    constructor(
        private val birthdayRepository: BirthdayRepository,
        private val cancelNotificationUseCase: CancelNotificationUseCase,
    ) {
        /**
         * Deletes a birthday by its ID.
         *
         * @param birthdayId The ID of the birthday to delete
         * @return Result indicating success or failure with details
         */
        suspend fun deleteBirthdayById(birthdayId: Long): DeleteBirthdayResult {
            return try {
                // Check if birthday exists
                val existingBirthday =
                    birthdayRepository.getBirthdayById(birthdayId)
                        ?: return DeleteBirthdayResult.NotFound("Birthday with ID $birthdayId not found")

                birthdayRepository.deleteBirthdayById(birthdayId)

                // Cancel any scheduled notifications
                cancelNotificationUseCase.cancelNotification(existingBirthday)

                DeleteBirthdayResult.Success(existingBirthday)
            } catch (e: Exception) {
                DeleteBirthdayResult.DatabaseError(e.message ?: "Failed to delete birthday")
            }
        }

        /**
         * Deletes a birthday using the birthday entity.
         *
         * @param birthday The birthday entity to delete
         * @return Result indicating success or failure with details
         */
        suspend fun deleteBirthday(birthday: Birthday): DeleteBirthdayResult {
            return try {
                birthdayRepository.deleteBirthday(birthday)

                // Cancel any scheduled notifications
                cancelNotificationUseCase.cancelNotification(birthday)

                DeleteBirthdayResult.Success(birthday)
            } catch (e: Exception) {
                DeleteBirthdayResult.DatabaseError(e.message ?: "Failed to delete birthday")
            }
        }

        /**
         * Deletes multiple birthdays by their IDs.
         * Useful for bulk deletion operations.
         *
         * @param birthdayIds List of birthday IDs to delete
         * @return Result containing successful deletions and any failures
         */
        suspend fun deleteBirthdays(birthdayIds: List<Long>): BulkDeleteBirthdayResult {
            val successfulDeletions = mutableListOf<Birthday>()
            val failures = mutableListOf<DeleteFailure>()

            for (birthdayId in birthdayIds) {
                when (val result = deleteBirthdayById(birthdayId)) {
                    is DeleteBirthdayResult.Success -> {
                        successfulDeletions.add(result.deletedBirthday)
                    }
                    is DeleteBirthdayResult.NotFound -> {
                        failures.add(DeleteFailure(birthdayId, result.message))
                    }
                    is DeleteBirthdayResult.DatabaseError -> {
                        failures.add(DeleteFailure(birthdayId, result.message))
                    }
                }
            }

            return BulkDeleteBirthdayResult(
                successfulDeletions = successfulDeletions,
                failures = failures,
            )
        }

        /**
         * Checks if a birthday can be safely deleted.
         * Currently always returns true, but can be extended for business rules.
         *
         * @param birthdayId The ID of the birthday to check
         * @return True if the birthday can be deleted, false otherwise
         */
        suspend fun canDeleteBirthday(birthdayId: Long): Boolean {
            return try {
                birthdayRepository.getBirthdayById(birthdayId) != null
            } catch (e: Exception) {
                false
            }
        }
    }

/**
 * Result of deleting a single birthday operation.
 */
sealed class DeleteBirthdayResult {
    data class Success(val deletedBirthday: Birthday) : DeleteBirthdayResult()

    data class NotFound(val message: String) : DeleteBirthdayResult()

    data class DatabaseError(val message: String) : DeleteBirthdayResult()
}

/**
 * Result of bulk birthday deletion operation.
 */
data class BulkDeleteBirthdayResult(
    val successfulDeletions: List<Birthday>,
    val failures: List<DeleteFailure>,
) {
    val hasFailures: Boolean get() = failures.isNotEmpty()
    val allSuccessful: Boolean get() = failures.isEmpty()
    val totalProcessed: Int get() = successfulDeletions.size + failures.size
}

/**
 * Represents a failure during bulk deletion.
 */
data class DeleteFailure(
    val birthdayId: Long,
    val errorMessage: String,
)
