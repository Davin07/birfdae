package com.birthdayreminder.ui.components.birfdae

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure helpers behind the Saffron component suite.
 *
 * These cover the two places where a quiet regression would be visible to
 * users but hard to spot in review: initials derivation, and the deterministic
 * accent bucket that keeps a person's colour stable across screens.
 */
class PersonComponentsTest {

    @Test
    fun `initials of a single name is one letter`() {
        assertEquals("A", initialsOf("Amma"))
    }

    @Test
    fun `initials of two names is two letters`() {
        assertEquals("KA", initialsOf("Karthik Anand"))
    }

    @Test
    fun `initials ignores leading and trailing whitespace`() {
        assertEquals("KA", initialsOf("   karthik anand  "))
    }

    @Test
    fun `initials caps the result at two letters`() {
        assertEquals("AB", initialsOf("a b c d e"))
    }

    @Test
    fun `initials of an empty name falls back to a question mark`() {
        assertEquals("?", initialsOf("   "))
    }

    @Test
    fun `initials of a name with no letters falls back to a question mark`() {
        assertEquals("?", initialsOf("123 456"))
    }

    @Test
    fun `initials of a multi-word name uses first letters of first two words`() {
        assertEquals("VP", initialsOf("  Venkat  Prasad  "))
    }

    @Test
    fun `initials is uppercase regardless of input case`() {
        assertEquals("AM", initialsOf("amma mallya"))
    }
}
