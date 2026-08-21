package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus

/**
 * Avoids recording a stale screen-off sample as discharge immediately after the
 * operating system reports that external power was connected.
 */
class PowerConnectionConfirmation(
    private val confirmationWindowMillis: Long = CONFIRMATION_WINDOW_MILLIS,
) {
    private var confirmationDeadlineMillis: Long? = null

    fun onPowerConnected(atMillis: Long) {
        confirmationDeadlineMillis = atMillis + confirmationWindowMillis
    }

    fun onPowerDisconnected() {
        confirmationDeadlineMillis = null
    }

    fun resolve(
        observedStatus: MonitorStatus,
        sample: BatterySample,
        powerWatts: Double?,
    ): MonitorStatus {
        val deadline = confirmationDeadlineMillis ?: return observedStatus
        if (sample.capturedAtMillis > deadline) {
            confirmationDeadlineMillis = null
            return observedStatus
        }

        if (observedStatus == MonitorStatus.CHARGING || observedStatus == MonitorStatus.FULL) {
            confirmationDeadlineMillis = null
            return observedStatus
        }

        val hasNetChargingCurrent = sample.currentMicroAmps?.let { it > 0 } == true
        if (hasNetChargingCurrent && (powerWatts ?: 0.0) >= ObserveChargingState.MIN_VISIBLE_WATTS) {
            confirmationDeadlineMillis = null
            return MonitorStatus.CHARGING
        }

        return MonitorStatus.STARTING
    }

    companion object {
        const val CONFIRMATION_WINDOW_MILLIS = 30_000L
    }
}
