package com.chargemonitor.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("charge_monitor_settings", Context.MODE_PRIVATE)
    private val _autoMonitoringEnabled = MutableStateFlow(preferences.getBoolean(KEY_AUTO_MONITORING, false))
    val autoMonitoringEnabled: StateFlow<Boolean> = _autoMonitoringEnabled.asStateFlow()

    fun setAutoMonitoringEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_MONITORING, enabled).apply()
        _autoMonitoringEnabled.value = enabled
    }

    companion object {
        private const val KEY_AUTO_MONITORING = "auto_monitoring"
    }
}
