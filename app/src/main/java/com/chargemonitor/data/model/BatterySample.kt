package com.chargemonitor.data.model

data class BatterySample(
    val voltageMillivolts: Int?,
    val currentMicroAmps: Int?,
    val levelPercent: Int?,
    val isCharging: Boolean,
    val isFull: Boolean,
    val isPlugged: Boolean,
    val capturedAtMillis: Long,
    val averageCurrentMicroAmps: Int? = null,
    val temperatureTenthsCelsius: Int? = null,
    val health: BatteryHealth? = null,
    val powerSources: Set<BatteryPowerSource> = emptySet(),
    val cycleCount: Int? = null,
    val chargeCounterMicroampHours: Int? = null,
    val energyCounterNanowattHours: Long? = null,
    val chargeTimeRemainingMillis: Long? = null,
    val technology: String? = null,
    val isPresent: Boolean? = null,
)
