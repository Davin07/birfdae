package com.birthdayreminder.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdayreminder.data.local.dao.BirthdayDao
import com.birthdayreminder.data.local.database.AppDatabase
import com.birthdayreminder.data.local.entity.Birthday
import com.birthdayreminder.data.repository.BirthdayRepository
import com.birthdayreminder.data.repository.BirthdayRepositoryImpl
import com.birthdayreminder.domain.error.ErrorHandler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Proves "Not this year" actually persists.
 *
 * A device run showed the overdue card disappearing from the UI while
 * `skippedYear` stayed at -1 in the database. The optimistic state update hid
 * the symptom, so the only way to catch a write that never lands is to assert
 * against the database afterwards.
 */
@RunWith(AndroidJUnit4::class)
class SkipBirthdayForYearUseCaseTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: BirthdayDao
    private lateinit var repository: BirthdayRepository
    private lateinit var useCase: SkipBirthdayForYearUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.birthdayDao()
        repository =
            BirthdayRepositoryImpl(
                birthdayDao = dao,
                database = db,
                errorHandler = ErrorHandler(),
            )
        useCase = SkipBirthdayForYearUseCase(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun skip_persists_the_year_to_the_database() =
        runBlocking {
            val id = dao.insertBirthday(makeBirthday(name = "Amma"))

            val updated = useCase(id, 2026)

            assertTrue("use case reported failure", updated)
            assertEquals(2026, dao.getBirthdayById(id)?.skippedYear)
        }

    @Test
    fun skipping_a_missing_row_is_a_no_op() =
        runBlocking {
            val updated = useCase(9999L, 2026)

            assertFalse("A missing row must not throw or report success", updated)
        }

    @Test
    fun skipping_preserves_every_other_field() =
        runBlocking {
            val original =
                makeBirthday(name = "Appa").copy(
                    notes = "Likes filter coffee",
                    isPinned = true,
                    relationship = "Family",
                )
            val id = dao.insertBirthday(original)

            useCase(id, 2026)

            val loaded = requireNotNull(dao.getBirthdayById(id))
            assertEquals("Appa", loaded.name)
            assertEquals("Likes filter coffee", loaded.notes)
            assertTrue(loaded.isPinned)
            assertEquals("Family", loaded.relationship)
            assertEquals(original.birthDate, loaded.birthDate)
            assertEquals(2026, loaded.skippedYear)
        }

    @Test
    fun skipping_twice_in_a_row_is_idempotent() =
        runBlocking {
            val id = dao.insertBirthday(makeBirthday())

            useCase(id, 2026)
            useCase(id, 2026)

            assertEquals(2026, dao.getBirthdayById(id)?.skippedYear)
        }

    @Test
    fun a_new_year_replaces_the_old_skip() =
        runBlocking {
            val id = dao.insertBirthday(makeBirthday())

            useCase(id, 2026)
            useCase(id, 2027)

            assertEquals(2027, dao.getBirthdayById(id)?.skippedYear)
        }

    private fun makeBirthday(name: String = "Test") =
        Birthday(
            name = name,
            birthDate = LocalDate.of(1974, 11, 14),
        )
}
