package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BuildDailyTrendSummaryTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val build = BuildDailyTrendSummary(zone)
    private val date = LocalDate.of(2026, 8, 20)

    @Test fun `summarizes charge, discharge, and power for one day`() {
        val records = listOf(
            record(8, 0, 40, 5.0, TrendDirection.DISCHARGING),
            record(8, 5, 35, 4.0, TrendDirection.DISCHARGING),
            record(8, 10, 45, 16.0, TrendDirection.CHARGING),
            record(8, 15, 55, 20.0, TrendDirection.CHARGING),
        )

        val summary = build(records, date)

        assertEquals(1, summary.chargingSessions)
        assertEquals(20, summary.gainedPercent)
        assertEquals(5, summary.dischargedPercent)
        assertEquals(11.25, summary.averageWatts!!, 0.01)
        assertEquals(20.0, summary.peakWatts!!, 0.01)
    }

    private fun record(hour: Int, minute: Int, level: Int, watts: Double, direction: TrendDirection): TrendRecord =
        TrendRecord(
            capturedAtMillis = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli(),
            batteryPercent = level,
            powerWatts = watts,
            direction = direction,
        )
}
