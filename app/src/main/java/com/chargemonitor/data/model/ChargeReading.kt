package com.chargemonitor.data.model

enum class MonitorStatus {
    DISABLED,
    IDLE,
    CHARGING,
    MEASUREMENT_UNAVAILABLE,
}

data class ChargeReading(
    val sample: BatterySample? = null,
    val powerWatts: Double? = null,
    val status: MonitorStatus = MonitorStatus.DISABLED,
)
