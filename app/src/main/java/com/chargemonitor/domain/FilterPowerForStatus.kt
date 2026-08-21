package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus

/**
 * Prevents a delayed positive battery-current reading from being presented as
 * discharge power right after external power is removed.
 */
class FilterPowerForStatus {
    operator fun invoke(
        sample: BatterySample,
        status: MonitorStatus,
        powerWatts: Double?,
    ): Double? = when {
        status == MonitorStatus.DISCHARGING && sample.currentMicroAmps != null && sample.currentMicroAmps >= 0 -> null
        else -> powerWatts
    }
}
