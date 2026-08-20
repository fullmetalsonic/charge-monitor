package com.chargemonitor.domain

import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class MonitorUpdatePolicyTest {
    @Test
    fun `notification refreshes only when its visible values change`() {
        val policy = MonitorUpdatePolicy()
        val initial = reading(MonitorStatus.CHARGING, 10.04, 75)

        assertTrue(policy.shouldRefreshNotification(initial))
        assertFalse(policy.shouldRefreshNotification(reading(MonitorStatus.CHARGING, 10.03, 75)))
        assertTrue(policy.shouldRefreshNotification(reading(MonitorStatus.CHARGING, 10.14, 75)))
        assertTrue(policy.shouldRefreshNotification(reading(MonitorStatus.DISCHARGING, 10.14, 75)))
    }

    @Test
    fun `sampling slows when screen is off and charging is not active`() {
        val policy = MonitorUpdatePolicy()

        assertEquals(2_000L, policy.nextSampleDelayMillis(MonitorStatus.CHARGING, false))
        assertEquals(5_000L, policy.nextSampleDelayMillis(MonitorStatus.DISCHARGING, true))
        assertEquals(15_000L, policy.nextSampleDelayMillis(MonitorStatus.DISCHARGING, false))
        assertEquals(30_000L, policy.nextSampleDelayMillis(MonitorStatus.FULL, false))
    }

    private fun reading(status: MonitorStatus, watts: Double, level: Int) = ChargeReading(
        sample = BatterySample(
            voltageMillivolts = 4_300,
            currentMicroAmps = 1_000_000,
            levelPercent = level,
            isCharging = status == MonitorStatus.CHARGING,
            isFull = status == MonitorStatus.FULL,
            isPlugged = status == MonitorStatus.CHARGING || status == MonitorStatus.FULL,
            capturedAtMillis = 0,
        ),
        powerWatts = watts,
        status = status,
    )
}
