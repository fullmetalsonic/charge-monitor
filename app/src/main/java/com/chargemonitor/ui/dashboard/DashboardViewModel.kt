package com.chargemonitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chargemonitor.data.repository.ChargeMonitorRepository
import com.chargemonitor.data.repository.SettingsRepository
import com.chargemonitor.service.MonitoringServiceController

class DashboardViewModel(
    private val monitorRepository: ChargeMonitorRepository,
    private val settingsRepository: SettingsRepository,
    private val startMonitoring: () -> Unit,
    private val stopMonitoring: () -> Unit,
) : ViewModel() {
    val reading = monitorRepository.reading
    val autoMonitoringEnabled = settingsRepository.autoMonitoringEnabled
    val statusBarWattEnabled = settingsRepository.statusBarWattEnabled

    fun setAutoMonitoringEnabled(enabled: Boolean) {
        settingsRepository.setAutoMonitoringEnabled(enabled)
        if (enabled) startMonitoring() else stopMonitoring()
    }

    fun setStatusBarWattEnabled(enabled: Boolean) {
        settingsRepository.setStatusBarWattEnabled(enabled)
    }

    companion object {
        fun factory(
            monitorRepository: ChargeMonitorRepository,
            settingsRepository: SettingsRepository,
            startMonitoring: () -> Unit,
            stopMonitoring: () -> Unit,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(monitorRepository, settingsRepository, startMonitoring, stopMonitoring) }
        }
    }
}
