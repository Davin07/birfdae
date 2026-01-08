package com.birthdayreminder.data.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BootReceiverTest {
    @Test
    fun `BootReceiver class is properly defined`() {
        val bootReceiver = BootReceiver()
        assertNotNull(bootReceiver)
    }

    @Test
    fun `BootReceiver has correct class name`() {
        val className = BootReceiver::class.java.simpleName
        assertEquals("BootReceiver", className)
    }

    @Test
    fun `BootReceiver package is correct`() {
        val packageName = BootReceiver::class.java.packageName
        assertEquals("com.birthdayreminder.data.notification", packageName)
    }
}
