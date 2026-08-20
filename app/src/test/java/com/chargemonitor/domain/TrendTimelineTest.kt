package com.chargemonitor.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import com.chargemonitor.data.model.TrendRecordingInterval
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
    fun `touch position maps to the selected interval bucket`() {
        assertEquals(0, TrendTimeline.bucketForFraction(0f, TrendRecordingInterval.STANDARD))
        assertEquals(111, TrendTimeline.bucketForFraction(111f / 288f, TrendRecordingInterval.STANDARD))
        assertEquals(1_439, TrendTimeline.bucketForFraction(1f, TrendRecordingInterval.PRECISION))
    }

    @Test
    fun `bucket label uses the selected resolution`() {
        assertEquals("00:00", TrendTimeline.timeLabel(0, TrendRecordingInterval.STANDARD))
        assertEquals("09:15", TrendTimeline.timeLabel(111, TrendRecordingInterval.STANDARD))
        assertEquals("09:17", TrendTimeline.timeLabel(557, TrendRecordingInterval.PRECISION))
    }

    @Test
    fun `timestamp stays in its exact selected resolution bucket`() {
        val millis = LocalDateTime.of(date, LocalTime.of(9, 15)).atZone(zone).toInstant().toEpochMilli()
        assertEquals(111, TrendTimeline.bucketForTimestamp(millis, TrendRecordingInterval.STANDARD, zone))
        assertEquals(555, TrendTimeline.bucketForTimestamp(millis, TrendRecordingInterval.PRECISION, zone))
    }

    @Test
    fun `minute cursor keeps exact one minute precision`() {
        assertEquals(557, TrendTimeline.minuteForFraction(557f / TrendTimeline.MINUTES_PER_DAY))
        assertEquals("09:17", TrendTimeline.timeLabelForMinute(557))
    }

    private fun fractionAt(hour: Int, minute: Int): Float {
        val millis = LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()
        return TrendTimeline.dayFraction(millis, zone)
    }
}
