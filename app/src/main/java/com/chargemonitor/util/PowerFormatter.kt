package com.chargemonitor.util

import java.util.Locale

object PowerFormatter {
    fun watts(value: Double?): String = value?.let { String.format(Locale.US, "%.1fW", it) } ?: "측정 불가"
    fun volts(millivolts: Int?): String = millivolts?.let { String.format(Locale.US, "%.2f V", it / 1_000.0) } ?: "—"
    fun amps(microAmps: Int?): String = microAmps?.let { String.format(Locale.US, "%.2f A", it / 1_000_000.0) } ?: "—"
}
