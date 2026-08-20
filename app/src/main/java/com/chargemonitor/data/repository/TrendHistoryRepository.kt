package com.chargemonitor.data.repository

import android.content.Context
import android.util.AtomicFile
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.data.model.TrendRecordingInterval
import com.chargemonitor.domain.AggregateTrendRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class TrendHistoryRepository(context: Context) {
    private val storageFile = AtomicFile(context.filesDir.resolve(FILE_NAME))
    private val lock = Any()
    private val aggregateTrendRecord = AggregateTrendRecord()
    private val _records = MutableStateFlow(loadRecords())
    val records: StateFlow<List<TrendRecord>> = _records.asStateFlow()

    fun record(reading: ChargeReading, interval: TrendRecordingInterval) {
        val sample = reading.sample ?: return
        val batteryPercent = sample.levelPercent ?: return
        synchronized(lock) {
            val current = _records.value
            val bucketStartMillis = sample.capturedAtMillis / interval.millis * interval.millis
            val next = TrendRecord(
                capturedAtMillis = bucketStartMillis,
                batteryPercent = batteryPercent.coerceIn(0, 100),
                powerWatts = reading.powerWatts,
                direction = reading.status.toTrendDirection(),
                intervalMinutes = interval.minutes,
            )
            val existingBucketIndex = current.indexOfLast {
                it.capturedAtMillis == bucketStartMillis && it.intervalMinutes == interval.minutes
            }
            val isNewBucket = existingBucketIndex == -1
            val updated = if (!isNewBucket) {
                current.toMutableList().apply {
                    this[existingBucketIndex] = aggregateTrendRecord(this[existingBucketIndex], next)
                }
            } else {
                (current + next).sortedBy { it.capturedAtMillis }
            }.filter { it.capturedAtMillis >= bucketStartMillis - RETENTION_MILLIS }
            _records.value = updated
            if (isNewBucket) persist(updated)
        }
    }

    /** Persists the current five-minute aggregate when monitoring stops. */
    fun flush() = synchronized(lock) { persist(_records.value) }

    private fun loadRecords(): List<TrendRecord> = runCatching {
        val array = JSONArray(storageFile.openRead().bufferedReader().use { it.readText() })
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TrendRecord(
                        capturedAtMillis = item.getLong("capturedAtMillis"),
                        batteryPercent = item.getInt("batteryPercent"),
                        powerWatts = item.takeIf { it.has("powerWatts") }?.getDouble("powerWatts"),
                        direction = TrendDirection.valueOf(item.getString("direction")),
                        intervalMinutes = item.optInt("intervalMinutes", TrendRecordingInterval.STANDARD.minutes),
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
                put("intervalMinutes", record.intervalMinutes)
                put("sampleCount", record.sampleCount)
                put("powerSampleCount", record.powerSampleCount)
            })
        }
        val output = storageFile.startWrite()
        try {
            output.write(array.toString().toByteArray())
            storageFile.finishWrite(output)
        } catch (exception: Exception) {
            storageFile.failWrite(output)
            throw exception
        }
    }

    private fun MonitorStatus.toTrendDirection(): TrendDirection = when (this) {
        MonitorStatus.CHARGING,
        MonitorStatus.FULL -> TrendDirection.CHARGING
        MonitorStatus.DISCHARGING -> TrendDirection.DISCHARGING
        else -> TrendDirection.IDLE
    }

    companion object {
        const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1_000L
        private const val FILE_NAME = "trend-history.json"
    }
}
