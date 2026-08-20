package com.chargemonitor.platform.quicksettings

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.chargemonitor.R
import com.chargemonitor.app.ChargeMonitorApplication
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.service.MonitoringServiceController
import com.chargemonitor.util.PowerFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ChargePowerTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null
    private val container by lazy { (application as ChargeMonitorApplication).container }

    override fun onStartListening() {
        super.onStartListening()
        listeningJob?.cancel()
        listeningJob = scope.launch {
            combine(
                container.chargeMonitorRepository.reading,
                container.settingsRepository.autoMonitoringEnabled,
            ) { reading, enabled -> createTileContent(reading, enabled) }
                .collect(::render)
        }
    }

    override fun onStopListening() {
        listeningJob?.cancel()
        listeningJob = null
        super.onStopListening()
    }

    override fun onClick() {
        if (isLocked) {
            unlockAndRun { toggleMonitoring() }
        } else {
            toggleMonitoring()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun toggleMonitoring() {
        val enableMonitoring = !container.settingsRepository.isAutoMonitoringEnabled()
        if (enableMonitoring) {
            val started = MonitoringServiceController.start(this)
            container.settingsRepository.setAutoMonitoringEnabled(started)
        } else {
            container.settingsRepository.setAutoMonitoringEnabled(false)
            MonitoringServiceController.stop(this)
        }
        render(createTileContent(container.chargeMonitorRepository.reading.value, container.settingsRepository.isAutoMonitoringEnabled()))
    }

    private fun render(content: TileContent) {
        qsTile?.apply {
            label = content.label
            contentDescription = content.label
            state = if (content.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            icon = Icon.createWithResource(this@ChargePowerTileService, android.R.drawable.ic_lock_idle_charging)
            updateTile()
        }
    }

    private data class TileContent(val label: String, val isActive: Boolean)

    private fun createTileContent(reading: ChargeReading, monitoringEnabled: Boolean): TileContent {
        if (!monitoringEnabled) return TileContent(getString(R.string.status_disabled), false)

        val label = when (reading.status) {
            MonitorStatus.CHARGING -> reading.powerWatts?.let {
                getString(R.string.notification_charging, PowerFormatter.watts(it))
            } ?: getString(R.string.status_charging)
            MonitorStatus.DISCHARGING -> reading.powerWatts?.let {
                getString(R.string.notification_discharging, PowerFormatter.watts(it))
            } ?: getString(R.string.status_discharging)
            MonitorStatus.FULL -> getString(R.string.charge_complete)
            MonitorStatus.STARTING -> getString(R.string.status_checking)
            MonitorStatus.MEASUREMENT_UNAVAILABLE -> getString(R.string.status_unavailable)
            MonitorStatus.IDLE -> getString(R.string.status_idle)
            MonitorStatus.DISABLED -> getString(R.string.status_disabled)
        }
        return TileContent(label, true)
    }
}
