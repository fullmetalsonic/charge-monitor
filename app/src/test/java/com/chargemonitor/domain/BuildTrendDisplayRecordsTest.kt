package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.data.model.TrendRecordingInterval
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildTrendDisplayRecordsTest {
    private val buildDisplayRecords = BuildTrendDisplayRecords()

    @Test
    fun `standard view averages five precision records into one point`() {
        val records = (0 until 5).map { minute -> record(minute, watts = 10.0 + minute, intervalMinutes = 1) }

        val displayed = buildDisplayRecords(records, TrendRecordingInterval.STANDARD)

        assertEquals(1, displayed.size)
        assertEquals(12.0, displayed.single().powerWatts!!, 0.01)
        assertEquals(5, displayed.single().intervalMinutes)
    }

    @Test
    fun `precision view keeps legacy five minute points and new one minute points`() {
        val legacy = record(0, watts = 10.0, intervalMinutes = 5)
        val precision = record(5, watts = 15.0, intervalMinutes = 1)

        val displayed = buildDisplayRecords(listOf(legacy, precision), TrendRecordingInterval.PRECISION)

        assertEquals(listOf(5, 1), displayed.map { it.intervalMinutes })
        assertEquals(listOf(0L, 60_000L * 5), displayed.map { it.capturedAtMillis })
    }

    private fun record(minute: Int, watts: Double, intervalMinutes: Int) = TrendRecord(
        capturedAtMillis = minute * 60_000L,
        batteryPercent = 80,
        powerWatts = watts,
        direction = TrendDirection.CHARGING,
        intervalMinutes = intervalMinutes,
    )
}
