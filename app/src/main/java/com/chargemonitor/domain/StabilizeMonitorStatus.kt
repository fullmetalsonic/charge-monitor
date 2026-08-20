package com.chargemonitor.domain

import com.chargemonitor.data.model.MonitorStatus

class StabilizeMonitorStatus(private val initialCheckingSamples: Int = 2) {
    private var consecutiveUnavailableSamples = 0

    fun update(observedStatus: MonitorStatus): MonitorStatus {
        if (observedStatus != MonitorStatus.MEASUREMENT_UNAVAILABLE) {
            consecutiveUnavailableSamples = 0
            return observedStatus
        }

        consecutiveUnavailableSamples += 1
        return if (consecutiveUnavailableSamples <= initialCheckingSamples) {
            MonitorStatus.STARTING
        } else {
            MonitorStatus.MEASUREMENT_UNAVAILABLE
        }
    }
}
