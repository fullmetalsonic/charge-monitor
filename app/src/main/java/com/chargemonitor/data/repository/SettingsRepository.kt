package com.chargemonitor.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("charge_monitor_settings", Context.MODE_PRIVATE)
    private val _autoMonitoringEnabled = MutableStateFlow(preferences.getBoolean(KEY_AUTO_MONITORING, false))
    private val _statusBarWattEnabled = MutableStateFlow(preferences.getBoolean(KEY_STATUS_BAR_WATT, false))
    private val _trendRecordingEnabled = MutableStateFlow(preferences.getBoolean(KEY_TREND_RECORDING, false))
    val autoMonitoringEnabled: StateFlow<Boolean> = _autoMonitoringEnabled.asStateFlow()
    val statusBarWattEnabled: StateFlow<Boolean> = _statusBarWattEnabled.asStateFlow()
    val trendRecordingEnabled: StateFlow<Boolean> = _trendRecordingEnabled.asStateFlow()

    fun setAutoMonitoringEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_MONITORING, enabled).apply()
        _autoMonitoringEnabled.value = enabled
    }

    fun setStatusBarWattEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_STATUS_BAR_WATT, enabled).apply()
        _statusBarWattEnabled.value = enabled
    }

    fun isStatusBarWattEnabled(): Boolean = preferences.getBoolean(KEY_STATUS_BAR_WATT, false)

    fun setTrendRecordingEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_TREND_RECORDING, enabled).apply()
        _trendRecordingEnabled.value = enabled
    }

    fun isTrendRecordingEnabled(): Boolean = preferences.getBoolean(KEY_TREND_RECORDING, false)

    companion object {
        private const val KEY_AUTO_MONITORING = "auto_monitoring"
        private const val KEY_STATUS_BAR_WATT = "status_bar_watt"
        private const val KEY_TREND_RECORDING = "trend_recording"
    }
}
