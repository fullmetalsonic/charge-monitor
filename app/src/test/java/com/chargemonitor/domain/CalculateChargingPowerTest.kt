package com.chargemonitor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateChargingPowerTest {
    private val calculate = CalculateChargingPower()

    @Test fun `converts millivolts and microamps to watts`() {
        assertEquals(25.4, calculate(9_120, 2_785_000)!!, 0.01)
    }

    @Test fun `does not present discharge or unavailable current as charging power`() {
        assertNull(calculate(9_120, -1_000_000))
        assertNull(calculate(9_120, Int.MIN_VALUE))
    }
}
