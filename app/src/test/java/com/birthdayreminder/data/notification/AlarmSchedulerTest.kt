package com.birthdayreminder.data.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {
    @Test
    fun `getRequestCode generates correct code for birthday today`() {
        val requestCode = AlarmScheduler.getRequestCode(1L, 0)
        assertEquals(10, requestCode)
    }

    @Test
    fun `getRequestCode generates correct code for advance reminder`() {
        val requestCode = AlarmScheduler.getRequestCode(1L, 1)
        assertEquals(11, requestCode)
    }

    @Test
    fun `getRequestCode generates correct code for different birthday id`() {
        val requestCode = AlarmScheduler.getRequestCode(5L, 0)
        assertEquals(50, requestCode)
    }

    @Test
    fun `getRequestCode generates correct code for 7 day advance`() {
        val requestCode = AlarmScheduler.getRequestCode(2L, 7)
        assertEquals(27, requestCode)
    }

    @Test
    fun `getRequestCode with zero birthday id`() {
        val requestCode = AlarmScheduler.getRequestCode(0L, 0)
        assertEquals(0, requestCode)
    }

    @Test
    fun `getRequestCode with large birthday id`() {
        val requestCode = AlarmScheduler.getRequestCode(100L, 0)
        assertEquals(1000, requestCode)
    }
}
