package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.MonitorStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilterPowerForStatusTest {
    private val filter = FilterPowerForStatus()

    @Test fun `hides positive current power while status is discharging`() {
        val result = filter(sample(currentMicroAmps = 2_400_000), MonitorStatus.DISCHARGING, 9.8)

        assertNull(result)
    }

    @Test fun `keeps negative current power while status is discharging`() {
        val result = filter(sample(currentMicroAmps = -450_000), MonitorStatus.DISCHARGING, 1.9)

        assertEquals(1.9, result!!, 0.0)
    }

    @Test fun `does not change charging power`() {
        val result = filter(sample(currentMicroAmps = 2_400_000), MonitorStatus.CHARGING, 9.8)

        assertEquals(9.8, result!!, 0.0)
    }

    private fun sample(currentMicroAmps: Int) = BatterySample(
        voltageMillivolts = 4_200,
        currentMicroAmps = currentMicroAmps,
        levelPercent = 80,
        isPlugged = false,
        isCharging = false,
        isFull = false,
        capturedAtMillis = 0L,
    )
}
