package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveChargingStateTest {
    private val observe = ObserveChargingState()

    @Test fun `reports full when plugged battery reports full and current is near zero`() {
        assertEquals(MonitorStatus.FULL, observe(sample(isFull = true, isCharging = false), null))
    }

    @Test fun `reports charging when plugged battery is accepting one watt or more`() {
        assertEquals(MonitorStatus.CHARGING, observe(sample(isCharging = true), 25.4))
    }

    @Test fun `reports discharging when the charger is disconnected`() {
        assertEquals(MonitorStatus.DISCHARGING, observe(sample(isPlugged = false), null))
    }

    private fun sample(
        isFull: Boolean = false,
        isCharging: Boolean = true,
        isPlugged: Boolean = true,
    ) = BatterySample(
        voltageMillivolts = 4_450,
        currentMicroAmps = 2_000_000,
        levelPercent = 80,
        isCharging = isCharging,
        isFull = isFull,
        isPlugged = isPlugged,
        capturedAtMillis = 0L,
    )
}
