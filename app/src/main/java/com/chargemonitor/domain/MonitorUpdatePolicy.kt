package com.chargemonitor.domain

import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import kotlin.math.roundToInt

/** Keeps monitor work responsive while avoiding needless foreground-notification redraws. */
class MonitorUpdatePolicy {
    private var lastNotificationKey: NotificationKey? = null

    fun shouldRefreshNotification(reading: ChargeReading): Boolean {
        val key = NotificationKey(
            status = reading.status,
            powerTenths = reading.powerWatts?.times(10)?.roundToInt(),
            batteryPercent = reading.sample?.levelPercent,
        )
        return (key != lastNotificationKey).also { changed ->
            if (changed) lastNotificationKey = key
        }
    }

    fun nextSampleDelayMillis(status: MonitorStatus, isScreenInteractive: Boolean): Long = when (status) {
        MonitorStatus.STARTING,
        MonitorStatus.CHARGING -> CHARGING_DELAY_MILLIS
        MonitorStatus.DISCHARGING -> if (isScreenInteractive) DISCHARGING_ACTIVE_DELAY_MILLIS else DISCHARGING_IDLE_DELAY_MILLIS
        MonitorStatus.FULL,
        MonitorStatus.IDLE,
        MonitorStatus.MEASUREMENT_UNAVAILABLE -> if (isScreenInteractive) IDLE_ACTIVE_DELAY_MILLIS else IDLE_SCREEN_OFF_DELAY_MILLIS
        MonitorStatus.DISABLED -> IDLE_SCREEN_OFF_DELAY_MILLIS
    }

    private data class NotificationKey(
        val status: MonitorStatus,
        val powerTenths: Int?,
        val batteryPercent: Int?,
    )

    companion object {
        const val CHARGING_DELAY_MILLIS = 2_000L
        const val DISCHARGING_ACTIVE_DELAY_MILLIS = 5_000L
        const val DISCHARGING_IDLE_DELAY_MILLIS = 15_000L
        const val IDLE_ACTIVE_DELAY_MILLIS = 10_000L
        const val IDLE_SCREEN_OFF_DELAY_MILLIS = 30_000L
    }
}
