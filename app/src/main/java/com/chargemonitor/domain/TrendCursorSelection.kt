package com.chargemonitor.domain

import com.chargemonitor.data.model.TrendRecord
import java.time.ZoneId

/** Finds the historical point that covers a user-selected minute. */
object TrendCursorSelection {
    fun recordForMinute(
        records: List<TrendRecord>,
        selectedMinute: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): TrendRecord? = records
        .filter { record ->
            val startMinute = TrendTimeline.minuteForTimestamp(record.capturedAtMillis, zoneId)
            selectedMinute in startMinute until (startMinute + record.intervalMinutes).coerceAtMost(TrendTimeline.MINUTES_PER_DAY)
        }
        // When an interval change creates overlap, preserve and prefer the finer original record.
        .minByOrNull { it.intervalMinutes }
}
