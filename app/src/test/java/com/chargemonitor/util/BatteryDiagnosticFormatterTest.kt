package com.chargemonitor.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryDiagnosticFormatterTest {
    @Test
    fun `formats detailed battery units`() {
        assertEquals("35.8 °C", BatteryDiagnosticFormatter.temperature(358))
        assertEquals("3,914 mAh", BatteryDiagnosticFormatter.milliampHours(3_914_400))
        assertEquals("3.91 Wh", BatteryDiagnosticFormatter.wattHours(3_914_400_000))
    }

    @Test
    fun `uses placeholder for unavailable values`() {
        assertEquals("—", BatteryDiagnosticFormatter.temperature(null))
        assertEquals("—", BatteryDiagnosticFormatter.milliampHours(null))
        assertEquals("—", BatteryDiagnosticFormatter.wattHours(null))
    }

    @Test
    fun `formats the sample time`() {
        assertTrue(BatteryDiagnosticFormatter.capturedAt(0).isNotBlank())
    }
}
