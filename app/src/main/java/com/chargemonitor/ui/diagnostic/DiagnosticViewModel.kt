package com.chargemonitor.ui.diagnostic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.domain.CalculateChargingPower
import com.chargemonitor.domain.ObserveChargingState
import com.chargemonitor.platform.battery.BatteryDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosticViewModel(
    private val batteryDataSource: BatteryDataSource,
    private val calculateChargingPower: CalculateChargingPower = CalculateChargingPower(),
    private val observeChargingState: ObserveChargingState = ObserveChargingState(),
) : ViewModel() {
    private val _reading = MutableStateFlow(ChargeReading(status = MonitorStatus.STARTING))
    val reading = _reading.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch(Dispatchers.Default) {
            _isRefreshing.value = true
            try {
                val sample = batteryDataSource.readSample()
                val powerWatts = calculateChargingPower(sample.voltageMillivolts, sample.currentMicroAmps)
                _reading.value = ChargeReading(
                    sample = sample,
                    powerWatts = powerWatts,
                    status = observeChargingState(sample, powerWatts),
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun factory(batteryDataSource: BatteryDataSource): ViewModelProvider.Factory = viewModelFactory {
            initializer { DiagnosticViewModel(batteryDataSource) }
        }
    }
}
