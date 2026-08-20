package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class AggregateTrendRecordTest {
    private val aggregate = AggregateTrendRecord()

    @Test fun `averages samples in one five minute bucket`() {
        val record = aggregate(
            TrendRecord(1_000, 40, 10.0, TrendDirection.CHARGING),
            TrendRecord(1_000, 44, 14.0, TrendDirection.CHARGING),
        )

        assertEquals(42, record.batteryPercent)
        assertEquals(12.0, record.powerWatts!!, 0.01)
        assertEquals(2, record.sampleCount)
        assertEquals(2, record.powerSampleCount)
    }

    @Test fun `does not count unavailable power in the power average`() {
        val record = aggregate(
            TrendRecord(1_000, 40, null, TrendDirection.CHARGING),
            TrendRecord(1_000, 42, 18.0, TrendDirection.CHARGING),
        )

        assertEquals(18.0, record.powerWatts!!, 0.01)
        assertEquals(1, record.powerSampleCount)
    }
}
