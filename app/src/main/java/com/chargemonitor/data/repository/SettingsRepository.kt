package com.chargemonitor.data.repository

import android.content.Context
import com.chargemonitor.data.model.TrendRecordingInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("charge_monitor_settings", Context.MODE_PRIVATE)
    private val _autoMonitoringEnabled = MutableStateFlow(preferences.getBoolean(KEY_AUTO_MONITORING, false))
    private val _trendRecordingEnabled = MutableStateFlow(preferences.getBoolean(KEY_TREND_RECORDING, false))
    private val _trendRecordingInterval = MutableStateFlow(
        TrendRecordingInterval.fromPreference(preferences.getString(KEY_TREND_RECORDING_INTERVAL, null)),
    )
    val autoMonitoringEnabled: StateFlow<Boolean> = _autoMonitoringEnabled.asStateFlow()
    val trendRecordingEnabled: StateFlow<Boolean> = _trendRecordingEnabled.asStateFlow()
    val trendRecordingInterval: StateFlow<TrendRecordingInterval> = _trendRecordingInterval.asStateFlow()

    fun setAutoMonitoringEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_MONITORING, enabled).apply()
        _autoMonitoringEnabled.value = enabled
    }

    fun isAutoMonitoringEnabled(): Boolean = preferences.getBoolean(KEY_AUTO_MONITORING, false)

    fun setTrendRecordingEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_TREND_RECORDING, enabled).apply()
        _trendRecordingEnabled.value = enabled
    }

    fun isTrendRecordingEnabled(): Boolean = preferences.getBoolean(KEY_TREND_RECORDING, false)

    fun setTrendRecordingInterval(interval: TrendRecordingInterval) {
        preferences.edit().putString(KEY_TREND_RECORDING_INTERVAL, interval.name).apply()
        _trendRecordingInterval.value = interval
    }

    companion object {
        private const val KEY_AUTO_MONITORING = "auto_monitoring"
        private const val KEY_TREND_RECORDING = "trend_recording"
        private const val KEY_TREND_RECORDING_INTERVAL = "trend_recording_interval"
    }
}
