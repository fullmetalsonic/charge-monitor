package com.chargemonitor.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

object BatteryDiagnosticFormatter {
    fun temperature(tenthsCelsius: Int?): String = tenthsCelsius?.let {
        String.format(Locale.getDefault(), "%.1f °C", it / 10.0)
    } ?: "—"

    fun milliampHours(microampHours: Int?): String = microampHours?.let {
        String.format(Locale.getDefault(), "%,.0f mAh", it / 1_000.0)
    } ?: "—"

    fun wattHours(nanowattHours: Long?): String = nanowattHours?.let {
        String.format(Locale.getDefault(), "%.2f Wh", it / 1_000_000_000.0)
    } ?: "—"

    fun capturedAt(millis: Long?): String = millis?.let {
        DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date(it))
    } ?: "—"
}
