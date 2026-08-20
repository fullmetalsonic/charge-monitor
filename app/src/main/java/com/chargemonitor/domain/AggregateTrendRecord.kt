package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendRecord
import kotlin.math.roundToInt

class AggregateTrendRecord {
    operator fun invoke(previous: TrendRecord, next: TrendRecord): TrendRecord {
        require(previous.capturedAtMillis == next.capturedAtMillis) { "Only records from the same time bucket can be merged." }
        val sampleCount = previous.sampleCount + next.sampleCount
        val powerSampleCount = previous.powerSampleCount + next.powerSampleCount
        val powerWatts = if (powerSampleCount == 0) {
            null
        } else {
            ((previous.powerWatts ?: 0.0) * previous.powerSampleCount +
                (next.powerWatts ?: 0.0) * next.powerSampleCount) / powerSampleCount
        }
        return next.copy(
            batteryPercent = ((previous.batteryPercent * previous.sampleCount + next.batteryPercent * next.sampleCount).toDouble() / sampleCount).roundToInt(),
            powerWatts = powerWatts,
            sampleCount = sampleCount,
            powerSampleCount = powerSampleCount,
        )
    }
}
