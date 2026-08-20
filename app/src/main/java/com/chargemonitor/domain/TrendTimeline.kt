package com.chargemonitor.domain

import java.time.Instant
import java.time.ZoneId
import com.chargemonitor.data.model.TrendRecordingInterval
import java.util.Locale
import kotlin.math.floor

object TrendTimeline {
    private const val SECONDS_PER_DAY = 24 * 60 * 60
    const val MINUTES_PER_DAY = 24 * 60
    fun dayFraction(capturedAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Float {
        val seconds = Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay()
        return seconds.toFloat() / SECONDS_PER_DAY
    }

    fun minuteForFraction(fraction: Float): Int = floor(fraction.coerceIn(0f, 0.99999f) * MINUTES_PER_DAY)
        .toInt()
        .coerceIn(0, MINUTES_PER_DAY - 1)

    fun fractionForMinute(minute: Int): Float = minute.coerceIn(0, MINUTES_PER_DAY - 1).toFloat() / MINUTES_PER_DAY

    fun minuteForTimestamp(capturedAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int =
        Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay() / 60

    fun timeLabelForMinute(minute: Int): String {
        val clampedMinute = minute.coerceIn(0, MINUTES_PER_DAY - 1)
        return String.format(Locale.ROOT, "%02d:%02d", clampedMinute / 60, clampedMinute % 60)
    }

    fun bucketForFraction(
        fraction: Float,
        interval: TrendRecordingInterval,
    ): Int = floor(fraction.coerceIn(0f, 0.99999f) * interval.bucketsPerDay)
        .toInt()
        .coerceIn(0, interval.bucketsPerDay - 1)

    fun fractionForBucket(bucket: Int, interval: TrendRecordingInterval): Float =
        bucket.coerceIn(0, interval.bucketsPerDay - 1).toFloat() / interval.bucketsPerDay

    fun bucketForTimestamp(
        capturedAtMillis: Long,
        interval: TrendRecordingInterval,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Int =
        (Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay() / (interval.minutes * 60))
            .coerceIn(0, interval.bucketsPerDay - 1)

    fun timeLabel(bucket: Int, interval: TrendRecordingInterval): String {
        val minutes = bucket.coerceIn(0, interval.bucketsPerDay - 1) * interval.minutes
        return String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
    }
}
