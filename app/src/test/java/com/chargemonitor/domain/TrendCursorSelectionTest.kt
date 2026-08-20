package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TrendCursorSelectionTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val date = LocalDate.of(2026, 8, 20)

    @Test
    fun `five minute record remains selectable after changing to precision`() {
        val standard = recordAt(9, 15, intervalMinutes = 5)

        assertSame(standard, TrendCursorSelection.recordForMinute(listOf(standard), 9 * 60 + 17, zone))
    }

    @Test
    fun `one minute record remains selectable after changing to standard`() {
        val precision = recordAt(9, 17, intervalMinutes = 1)

        assertSame(precision, TrendCursorSelection.recordForMinute(listOf(precision), 9 * 60 + 17, zone))
    }

    @Test
    fun `finer record wins when an interval change creates overlap`() {
        val standard = recordAt(9, 15, intervalMinutes = 5)
        val precision = recordAt(9, 17, intervalMinutes = 1)

        assertEquals(precision, TrendCursorSelection.recordForMinute(listOf(standard, precision), 9 * 60 + 17, zone))
    }

    private fun recordAt(hour: Int, minute: Int, intervalMinutes: Int): TrendRecord = TrendRecord(
        capturedAtMillis = LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli(),
        batteryPercent = 80,
        powerWatts = 10.0,
        direction = TrendDirection.CHARGING,
        intervalMinutes = intervalMinutes,
    )
}
