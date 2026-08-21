package com.chargemonitor.data.model

/** A highest recorded average-power point for one direction within a day. */
data class TrendPeak(
    val capturedAtMillis: Long,
    val batteryPercent: Int,
    val powerWatts: Double,
)
