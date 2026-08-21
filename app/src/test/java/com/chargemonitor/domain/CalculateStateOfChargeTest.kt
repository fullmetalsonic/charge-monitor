package com.chargemonitor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateStateOfChargeTest {
    private val calculateStateOfCharge = CalculateStateOfCharge()

    @Test
    fun `converts Android level and scale into percent`() {
        assertEquals(94, calculateStateOfCharge(level = 94, scale = 100))
        assertEquals(50, calculateStateOfCharge(level = 128, scale = 255))
    }

    @Test
    fun `rounds and bounds valid values`() {
        assertEquals(33, calculateStateOfCharge(level = 1, scale = 3))
        assertEquals(100, calculateStateOfCharge(level = 150, scale = 100))
    }

    @Test
    fun `rejects missing or invalid system values`() {
        assertNull(calculateStateOfCharge(level = null, scale = 100))
        assertNull(calculateStateOfCharge(level = 50, scale = 0))
        assertNull(calculateStateOfCharge(level = -1, scale = 100))
    }
}
