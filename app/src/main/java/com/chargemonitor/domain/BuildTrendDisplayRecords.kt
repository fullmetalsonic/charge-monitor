package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.data.model.TrendRecordingInterval
import kotlin.math.roundToInt

/**
 * Produces chart points without inventing missing detail.
 * Fine one-minute points can be safely averaged into five-minute points; existing five-minute
 * points remain five-minute points when the user changes to the precision view later.
 */
class BuildTrendDisplayRecords {
    operator fun invoke(
        records: List<TrendRecord>,
        displayInterval: TrendRecordingInterval,
    ): List<TrendRecord> {
        if (displayInterval == TrendRecordingInterval.PRECISION) return records.sortedBy { it.capturedAtMillis }

        return records
            .groupBy { it.capturedAtMillis / displayInterval.millis }
            .toSortedMap()
            .map { (bucket, group) -> aggregate(bucket * displayInterval.millis, group, displayInterval) }
    }

    private fun aggregate(
        bucketStartMillis: Long,
        records: List<TrendRecord>,
        interval: TrendRecordingInterval,
    ): TrendRecord {
        val sampleCount = records.sumOf { it.sampleCount }
        val powerSampleCount = records.sumOf { it.powerSampleCount }
        val batteryPercent = records.sumOf { it.batteryPercent * it.sampleCount }.toDouble() / sampleCount
        val powerWatts = if (powerSampleCount == 0) {
            null
        } else {
            records.sumOf { (it.powerWatts ?: 0.0) * it.powerSampleCount } / powerSampleCount
        }
        val latest = records.maxBy { it.capturedAtMillis }
        return latest.copy(
            capturedAtMillis = bucketStartMillis,
            batteryPercent = batteryPercent.roundToInt(),
            powerWatts = powerWatts,
            intervalMinutes = interval.minutes,
            sampleCount = sampleCount,
            powerSampleCount = powerSampleCount,
        )
    }
}
