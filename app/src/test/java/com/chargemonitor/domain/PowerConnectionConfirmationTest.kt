package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PowerConnectionConfirmationTest {
    private val confirmation = PowerConnectionConfirmation(confirmationWindowMillis = 30_000)

    @Test fun `holds an early stale discharge sample as checking after power connects`() {
        confirmation.onPowerConnected(1_000)

        assertEquals(
            MonitorStatus.STARTING,
            confirmation.resolve(MonitorStatus.DISCHARGING, sample(at = 2_000, currentMicroAmps = -500_000), 2.0),
        )
    }

    @Test fun `confirms charging from positive net current while battery broadcast is still stale`() {
        confirmation.onPowerConnected(1_000)

        assertEquals(
            MonitorStatus.CHARGING,
            confirmation.resolve(MonitorStatus.IDLE, sample(at = 5_000, currentMicroAmps = 900_000), 3.6),
        )
    }

    @Test fun `allows discharge immediately after power disconnects`() {
        confirmation.onPowerConnected(1_000)
        confirmation.onPowerDisconnected()

        assertEquals(
            MonitorStatus.DISCHARGING,
            confirmation.resolve(MonitorStatus.DISCHARGING, sample(at = 2_000, currentMicroAmps = -500_000), 2.0),
        )
    }

    private fun sample(at: Long, currentMicroAmps: Int) = BatterySample(
        voltageMillivolts = 4_000,
        currentMicroAmps = currentMicroAmps,
        levelPercent = 80,
        isCharging = false,
        isFull = false,
        isPlugged = false,
        capturedAtMillis = at,
    )
}
