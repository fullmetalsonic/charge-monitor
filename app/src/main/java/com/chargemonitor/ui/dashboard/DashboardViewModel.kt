package com.chargemonitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chargemonitor.data.repository.ChargeMonitorRepository
import com.chargemonitor.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel(
    private val monitorRepository: ChargeMonitorRepository,
    private val settingsRepository: SettingsRepository,
    private val startMonitoring: () -> Boolean,
    private val stopMonitoring: () -> Unit,
) : ViewModel() {
    val reading = monitorRepository.reading
    val autoMonitoringEnabled = settingsRepository.autoMonitoringEnabled
    val trendRecordingEnabled = settingsRepository.trendRecordingEnabled
    private val _monitoringStartFailed = MutableStateFlow(false)
    val monitoringStartFailed = _monitoringStartFailed.asStateFlow()

    init {
        if (settingsRepository.isAutoMonitoringEnabled()) {
            val started = startMonitoring()
            _monitoringStartFailed.value = !started
            if (!started) settingsRepository.setAutoMonitoringEnabled(false)
        }
    }

    fun setAutoMonitoringEnabled(enabled: Boolean) {
        if (enabled) {
            val started = startMonitoring()
            settingsRepository.setAutoMonitoringEnabled(started)
            _monitoringStartFailed.value = !started
        } else {
            settingsRepository.setAutoMonitoringEnabled(false)
            _monitoringStartFailed.value = false
            stopMonitoring()
        }
    }

    fun setTrendRecordingEnabled(enabled: Boolean) {
        settingsRepository.setTrendRecordingEnabled(enabled)
    }

    companion object {
        fun factory(
            monitorRepository: ChargeMonitorRepository,
            settingsRepository: SettingsRepository,
            startMonitoring: () -> Boolean,
            stopMonitoring: () -> Unit,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(monitorRepository, settingsRepository, startMonitoring, stopMonitoring) }
        }
    }
}
