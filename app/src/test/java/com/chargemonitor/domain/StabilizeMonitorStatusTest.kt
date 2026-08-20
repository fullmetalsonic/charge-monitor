package com.chargemonitor.domain

import com.chargemonitor.data.model.MonitorStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class StabilizeMonitorStatusTest {
    private val stabilize = StabilizeMonitorStatus(initialCheckingSamples = 2)

    @Test fun `shows checking state for the first unavailable samples`() {
        assertEquals(MonitorStatus.STARTING, stabilize.update(MonitorStatus.MEASUREMENT_UNAVAILABLE))
        assertEquals(MonitorStatus.STARTING, stabilize.update(MonitorStatus.MEASUREMENT_UNAVAILABLE))
        assertEquals(MonitorStatus.MEASUREMENT_UNAVAILABLE, stabilize.update(MonitorStatus.MEASUREMENT_UNAVAILABLE))
    }

    @Test fun `resets initial checking state after a valid reading`() {
        stabilize.update(MonitorStatus.MEASUREMENT_UNAVAILABLE)
        stabilize.update(MonitorStatus.CHARGING)
        assertEquals(MonitorStatus.STARTING, stabilize.update(MonitorStatus.MEASUREMENT_UNAVAILABLE))
    }
}
