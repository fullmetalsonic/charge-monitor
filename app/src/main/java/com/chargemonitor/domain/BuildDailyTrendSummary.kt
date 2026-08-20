package com.chargemonitor.domain

import com.chargemonitor.data.model.DailyTrendSummary
import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BuildDailyTrendSummary(private val zoneId: ZoneId = ZoneId.systemDefault()) {
    operator fun invoke(records: List<TrendRecord>, date: LocalDate): DailyTrendSummary {
        val dailyRecords = records
            .filter { Instant.ofEpochMilli(it.capturedAtMillis).atZone(zoneId).toLocalDate() == date }
            .sortedBy(TrendRecord::capturedAtMillis)
        val changes = dailyRecords.zipWithNext { previous, current -> current.batteryPercent - previous.batteryPercent }
        val watts = dailyRecords.mapNotNull(TrendRecord::powerWatts)
        return DailyTrendSummary(
            date = date,
            records = dailyRecords,
            chargingSessions = dailyRecords.zipWithNext().count { (previous, current) ->
                previous.direction != TrendDirection.CHARGING && current.direction == TrendDirection.CHARGING
            } + if (dailyRecords.firstOrNull()?.direction == TrendDirection.CHARGING) 1 else 0,
            gainedPercent = changes.filter { it > 0 }.sum(),
            dischargedPercent = -changes.filter { it < 0 }.sum(),
            averageWatts = watts.takeIf { it.isNotEmpty() }?.average(),
            peakWatts = watts.maxOrNull(),
        )
    }
}
