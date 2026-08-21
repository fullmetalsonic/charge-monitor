package com.chargemonitor.data.model

import java.time.LocalDate

data class DailyTrendSummary(
    val date: LocalDate,
    val records: List<TrendRecord>,
    val chargingSessions: Int,
    val gainedPercent: Int,
    val dischargedPercent: Int,
    val averageWatts: Double?,
    val peakWatts: Double?,
    val chargingPeak: TrendPeak?,
    val dischargingPeak: TrendPeak?,
)
