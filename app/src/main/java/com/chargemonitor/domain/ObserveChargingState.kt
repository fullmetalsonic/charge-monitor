package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus

class ObserveChargingState {
    operator fun invoke(sample: BatterySample, powerWatts: Double?): MonitorStatus = when {
        !sample.isPlugged || !sample.isCharging -> MonitorStatus.IDLE
        powerWatts == null -> MonitorStatus.MEASUREMENT_UNAVAILABLE
        powerWatts >= MIN_VISIBLE_WATTS -> MonitorStatus.CHARGING
        else -> MonitorStatus.IDLE
    }

    companion object {
        const val MIN_VISIBLE_WATTS = 1.0
    }
}
