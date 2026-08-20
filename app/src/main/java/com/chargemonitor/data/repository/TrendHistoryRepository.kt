package com.chargemonitor.data.repository

import android.content.Context
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.domain.AggregateTrendRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TrendHistoryRepository(context: Context) {
    private val storageFile = File(context.filesDir, FILE_NAME)
    private val lock = Any()
    private val aggregateTrendRecord = AggregateTrendRecord()
    private val _records = MutableStateFlow(loadRecords())
    val records: StateFlow<List<TrendRecord>> = _records.asStateFlow()

    fun record(reading: ChargeReading) {
        val sample = reading.sample ?: return
        val batteryPercent = sample.levelPercent ?: return
        synchronized(lock) {
            val current = _records.value
            val bucketStartMillis = sample.capturedAtMillis / RECORD_INTERVAL_MILLIS * RECORD_INTERVAL_MILLIS
            val next = TrendRecord(
                capturedAtMillis = bucketStartMillis,
                batteryPercent = batteryPercent.coerceIn(0, 100),
                powerWatts = reading.powerWatts,
                direction = reading.status.toTrendDirection(),
            )
            val updated = if (current.lastOrNull()?.capturedAtMillis == bucketStartMillis) {
                current.dropLast(1) + aggregateTrendRecord(current.last(), next)
            } else {
                current + next
            }.filter { it.capturedAtMillis >= bucketStartMillis - RETENTION_MILLIS }
            persist(updated)
            _records.value = updated
        }
    }

    private fun loadRecords(): List<TrendRecord> = runCatching {
        val array = JSONArray(storageFile.readText())
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TrendRecord(
                        capturedAtMillis = item.getLong("capturedAtMillis"),
                        batteryPercent = item.getInt("batteryPercent"),
                        powerWatts = item.takeIf { it.has("powerWatts") }?.getDouble("powerWatts"),
                        direction = TrendDirection.valueOf(item.getString("direction")),
                        sampleCount = item.optInt("sampleCount", 1),
                        powerSampleCount = item.optInt("powerSampleCount", if (item.has("powerWatts")) 1 else 0),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun persist(records: List<TrendRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(JSONObject().apply {
                put("capturedAtMillis", record.capturedAtMillis)
                put("batteryPercent", record.batteryPercent)
                record.powerWatts?.let { put("powerWatts", it) }
                put("direction", record.direction.name)
                put("sampleCount", record.sampleCount)
                put("powerSampleCount", record.powerSampleCount)
            })
        }
        storageFile.writeText(array.toString())
    }

    private fun MonitorStatus.toTrendDirection(): TrendDirection = when (this) {
        MonitorStatus.CHARGING,
        MonitorStatus.FULL -> TrendDirection.CHARGING
        MonitorStatus.DISCHARGING -> TrendDirection.DISCHARGING
        else -> TrendDirection.IDLE
    }

    companion object {
        const val RECORD_INTERVAL_MILLIS = 5 * 60 * 1_000L
        const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1_000L
        private const val FILE_NAME = "trend-history.json"
    }
}
