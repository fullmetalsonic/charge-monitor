package com.chargemonitor.data.model

enum class TrendDirection {
    CHARGING,
    DISCHARGING,
    IDLE,
}

data class TrendRecord(
    val capturedAtMillis: Long,
    val batteryPercent: Int,
    val powerWatts: Double?,
    val direction: TrendDirection,
    val sampleCount: Int = 1,
    val powerSampleCount: Int = if (powerWatts == null) 0 else 1,
)
