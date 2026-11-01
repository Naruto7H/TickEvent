package com.example.nextup

import com.example.nextup.domain.model.CountdownTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountdownTimeTest {

    @Test
    fun `calculates remaining days hours minutes and seconds accurately`() {
        val now = 1_000_000_000L
        // 2 days, 3 hours, 15 minutes, 40 seconds in ms
        val offset = ((2L * 86_400L) + (3L * 3_600L) + (15L * 60L) + 40L) * 1_000L
        val result = CountdownTime.fromTargetTimestamp(targetMillis = now + offset, nowMillis = now)

        assertFalse(result.isCompleted)
        assertEquals(2L, result.days)
        assertEquals(3L, result.hours)
        assertEquals(15L, result.minutes)
        assertEquals(40L, result.seconds)
        assertEquals("02 : 03 : 15", result.formatDaysHoursMinutes())
    }

    @Test
    fun `marks countdown as completed when target timestamp is reached or past`() {
        val now = 1_000_000_000L
        val result = CountdownTime.fromTargetTimestamp(targetMillis = now - 5_000L, nowMillis = now)

        assertTrue(result.isCompleted)
        assertEquals(0L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(0L, result.seconds)
    }
}
