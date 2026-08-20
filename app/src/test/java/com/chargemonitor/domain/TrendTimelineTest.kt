package com.chargemonitor.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TrendTimelineTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val date = LocalDate.of(2026, 8, 20)

    @Test
    fun `midnight starts at the left edge`() {
        assertEquals(0f, fractionAt(0, 0), 0.0001f)
    }

    @Test
    fun `six oclock is one quarter across the day`() {
        assertEquals(0.25f, fractionAt(6, 0), 0.0001f)
    }

    @Test
    fun `late night stays near the right edge`() {
        assertEquals(0.9993f, fractionAt(23, 59), 0.0001f)
    }

    @Test
    fun `touch position maps to its five minute bucket`() {
        assertEquals(0, TrendTimeline.bucketForFraction(0f))
        assertEquals(111, TrendTimeline.bucketForFraction(111f / TrendTimeline.BUCKET_COUNT))
        assertEquals(287, TrendTimeline.bucketForFraction(1f))
    }

    @Test
    fun `bucket label uses twenty four hour time`() {
        assertEquals("00:00", TrendTimeline.timeLabel(0))
        assertEquals("09:15", TrendTimeline.timeLabel(111))
        assertEquals("23:55", TrendTimeline.timeLabel(287))
    }

    @Test
    fun `timestamp stays in its exact five minute bucket`() {
        val millis = LocalDateTime.of(date, LocalTime.of(9, 15)).atZone(zone).toInstant().toEpochMilli()
        assertEquals(111, TrendTimeline.bucketForTimestamp(millis, zone))
    }

    private fun fractionAt(hour: Int, minute: Int): Float {
        val millis = LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()
        return TrendTimeline.dayFraction(millis, zone)
    }
}
