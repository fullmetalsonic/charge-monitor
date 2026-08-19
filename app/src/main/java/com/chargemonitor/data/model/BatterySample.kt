package com.chargemonitor.data.model

data class BatterySample(
    val voltageMillivolts: Int?,
    val currentMicroAmps: Int?,
    val levelPercent: Int?,
    val isCharging: Boolean,
    val isFull: Boolean,
    val isPlugged: Boolean,
    val capturedAtMillis: Long,
)
