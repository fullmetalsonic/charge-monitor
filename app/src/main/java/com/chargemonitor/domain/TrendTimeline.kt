package com.chargemonitor.domain

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.floor

object TrendTimeline {
    private const val SECONDS_PER_DAY = 24 * 60 * 60
    const val BUCKET_COUNT = 24 * 12

    fun dayFraction(capturedAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Float {
        val seconds = Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay()
        return seconds.toFloat() / SECONDS_PER_DAY
    }

    fun bucketForFraction(fraction: Float): Int = floor(fraction.coerceIn(0f, 0.99999f) * BUCKET_COUNT)
        .toInt()
        .coerceIn(0, BUCKET_COUNT - 1)

    fun fractionForBucket(bucket: Int): Float = bucket.coerceIn(0, BUCKET_COUNT - 1).toFloat() / BUCKET_COUNT

    fun bucketForTimestamp(capturedAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int =
        (Instant.ofEpochMilli(capturedAtMillis).atZone(zoneId).toLocalTime().toSecondOfDay() / (5 * 60))
            .coerceIn(0, BUCKET_COUNT - 1)

    fun timeLabel(bucket: Int): String {
        val minutes = bucket.coerceIn(0, BUCKET_COUNT - 1) * 5
        return String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
    }
}
