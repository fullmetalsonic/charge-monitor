package com.chargemonitor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StabilizePowerReadingTest {
    @Test fun `uses median to reject one extreme reading`() {
        val stabilizer = StabilizePowerReading(3)
        stabilizer.add(20.0)
        stabilizer.add(80.0)
        assertEquals(21.0, stabilizer.add(21.0)!!, 0.0)
    }

    @Test fun `clears historical samples when power is unavailable`() {
        val stabilizer = StabilizePowerReading()
        stabilizer.add(20.0)
        assertNull(stabilizer.add(null))
    }
}
