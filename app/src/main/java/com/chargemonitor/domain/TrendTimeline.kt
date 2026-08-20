package com.chargemonitor.domain

import java.time.Instant
import java.time.ZoneId

object TrendTimeline {
    private const val SECONDS_PER_DAY = 24 * 60 * 60

    fun dayFraction(capturedAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Float {
        val seconds = Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay()
        return seconds.toFloat() / SECONDS_PER_DAY
    }
}
