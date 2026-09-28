package com.birthdayreminder.domain.model

import com.birthdayreminder.data.local.entity.Birthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Which people a list may show, given what the cards above it already said.
 *
 * The overdue card and the hero are both announcements. Repeating one of their
 * people further down the list makes the app contradict itself: the overdue
 * card says "5 DAYS AGO, Karthik" and the list then shows Karthik 250 days out
 * under "Later this year", which reads as a bug even though both are computed
 * correctly in isolation.
 */
class ListExclusionTest {
    private fun person(
        id: Long,
        name: String,
    ) = Birthday(
        id = id,
        name = name,
        birthDate = LocalDate.of(1993, 6, 12),
        createdAt = LocalDateTime.now(),
    )

    @Test
    fun `a person shown in the overdue card is not also shown in the list`() {
        val overdue = OverdueBirthday(person(1, "Karthik"), LocalDate.now().minusDays(5), 5)

        val listed =
            listOf(person(1, "Karthik"), person(2, "Amma")).filterNot {
                it.id in setOf(overdue.id)
            }

        assertEquals(
            listOf("Amma"),
            listed.map { it.name },
        )
    }

    @Test
    fun `a person shown in the hero is not also shown in the list`() {
        val heroIds = setOf(2L)

        val listed = listOf(person(1, "Karthik"), person(2, "Amma")).filterNot { it.id in heroIds }

        assertEquals(listOf("Karthik"), listed.map { it.name })
    }

    @Test
    fun `a collapsed remainder is still excluded from the list`() {
        // The "N more" row stands in for the other overdue people, so everyone
        // it represents is already accounted for above.
        val all = listOf(person(1, "Karthik"), person(2, "Priya"), person(3, "Amma"))
        val overdueIds = setOf(1L, 2L)

        assertEquals(
            listOf("Amma"),
            all.filterNot { it.id in overdueIds }.map { it.name },
        )
    }

    @Test
    fun `someone who is neither overdue nor the hero is still listed`() {
        val heroIds = setOf(1L)
        val overdueIds = setOf(2L)

        val listed =
            listOf(person(1, "Karthik"), person(2, "Priya"), person(3, "Amma")).filterNot {
                it.id in heroIds || it.id in overdueIds
            }

        assertTrue("Excluding the announced people must not empty the list", listed.isNotEmpty())
        assertEquals(listOf("Amma"), listed.map { it.name })
    }
}
