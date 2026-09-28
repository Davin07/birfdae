package com.birthdayreminder.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The relationship vocabulary, and the one thing that most easily breaks:
 * a value the app can store but not find.
 */
class RelationshipTest {
    @Test
    fun `the six options match the approved concept, in order`() {
        assertEquals(
            listOf("Family", "Parents", "Friends", "Partner", "Colleagues", "Other"),
            Relationship.labels,
        )
    }

    @Test
    fun `stored and label agree for every option`() {
        Relationship.all.forEach { relationship ->
            assertEquals(relationship, Relationship.fromStored(relationship.stored))
        }
    }

    @Test
    fun `stored lookup ignores case and surrounding whitespace`() {
        assertEquals(Relationship.FAMILY, Relationship.fromStored("  family "))
        assertEquals(Relationship.COLLEAGUES, Relationship.fromStored("COLLEAGUES"))
    }

    @Test
    fun `a legacy wizard value still resolves`() {
        // The old wizard wrote singular relationship words, not these six chips.
        // A person saved under one of them must not disappear from a filter.
        assertEquals(Relationship.FRIENDS, Relationship.fromStored("Friend"))
        assertEquals(Relationship.COLLEAGUES, Relationship.fromStored("Work"))
        assertEquals(Relationship.OTHER, Relationship.fromStored("Acquaintance"))
        assertEquals(Relationship.FAMILY, Relationship.fromStored("Relative"))
    }

    @Test
    fun `a parent resolves to Parents, not to Family`() {
        // This is the mapping that matters most and is easiest to get wrong.
        // The seeded database, and every real save made before this change,
        // stores Mother/Father. Collapsing those into FAMILY would leave the
        // Parents chip matching nothing anyone ever actually saved.
        assertEquals(Relationship.PARENTS, Relationship.fromStored("Mother"))
        assertEquals(Relationship.PARENTS, Relationship.fromStored("Father"))
        assertEquals(Relationship.PARENTS, Relationship.fromStored("Parent"))
    }

    @Test
    fun `a sibling resolves to Family`() {
        assertEquals(Relationship.FAMILY, Relationship.fromStored("Sister"))
        assertEquals(Relationship.FAMILY, Relationship.fromStored("Brother"))
    }

    @Test
    fun `every relationship a real save has used resolves to a chip`() {
        // Taken from the values actually present in a seeded database, because
        // guessing the legacy vocabulary is how a filter ends up matching
        // nothing while appearing to work.
        listOf(
            "Mother", "Father", "Friend", "Sister", "Brother", "Spouse",
            "Partner", "Colleague", "Acquaintance", "Family", "Parents",
            "Friends", "Colleagues", "Other", "Son", "Daughter", "Cousin",
        ).forEach { stored ->
            assertTrue(
                "\"$stored\" resolves to no chip, so a person saved as $stored " +
                    "would be invisible to every filter except All",
                Relationship.fromStored(stored) != null,
            )
        }
    }

    @Test
    fun `every chip a filter can show has a value that reaches it`() {
        // The other direction: no chip should be reachable only by a value the
        // app never writes.
        Relationship.all.forEach { relationship ->
            assertTrue(
                "${relationship.label} matches no stored value",
                relationship.matches(relationship.stored),
            )
        }
    }

    @Test
    fun `a spouse is a partner`() {
        assertEquals(Relationship.PARTNER, Relationship.fromStored("Spouse"))
        assertEquals(Relationship.PARTNER, Relationship.fromStored("Wife"))
    }

    @Test
    fun `an absent relationship resolves to nothing rather than to Other`() {
        // Falling back to Other would put every untagged person under a filter
        // the user chose to mean something specific.
        assertNull(Relationship.fromStored(null))
        assertNull(Relationship.fromStored(""))
        assertNull(Relationship.fromStored("   "))
    }

    @Test
    fun `an unrecognised value resolves to nothing`() {
        // Not "neighbour" and "coworker": both are real words a person might
        // have saved, and both resolve to Other on purpose. This is a value
        // that means nothing, and it must not become Other, or the Other filter
        // would quietly become "everything unrecognised".
        assertNull(Relationship.fromStored("qwerty"))
        assertNull(Relationship.fromStored("42"))
    }

    @Test
    fun `matches is true only for its own relationship`() {
        assertTrue(Relationship.FAMILY.matches("Family"))
        assertTrue(Relationship.FAMILY.matches("Parents") == false)
        assertFalse(Relationship.FRIENDS.matches("Family"))
    }

    @Test
    fun `Other does not match an untagged person`() {
        // Other is a choice, not a synonym for "everything else".
        assertFalse(Relationship.OTHER.matches(null))
        assertFalse(Relationship.OTHER.matches(""))
        assertTrue(Relationship.OTHER.matches("Other"))
    }

    @Test
    fun `the Search filter labels match the concept`() {
        assertEquals(
            listOf("All", "Family", "Friends", "This month"),
            SearchFilter.entries.map { it.label },
        )
    }
}
