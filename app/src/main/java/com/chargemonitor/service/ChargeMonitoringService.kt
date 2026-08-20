package com.chargemonitor.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.chargemonitor.app.ChargeMonitorApplication
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.domain.CalculateChargingPower
import com.chargemonitor.domain.ObserveChargingState
import com.chargemonitor.domain.StabilizeMonitorStatus
import com.chargemonitor.domain.StabilizePowerReading
import com.chargemonitor.domain.MonitorUpdatePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChargeMonitoringService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private val calculateChargingPower = CalculateChargingPower()
    private val stabilizePowerReading = StabilizePowerReading()
    private val observeChargingState = ObserveChargingState()
    private val stabilizeMonitorStatus = StabilizeMonitorStatus()
    private val monitorUpdatePolicy = MonitorUpdatePolicy()

    private val container by lazy { (application as ChargeMonitorApplication).container }
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground(ChargeReading(status = MonitorStatus.STARTING))
        if (monitorJob == null) startMonitoring()
        return START_STICKY
    }

    private fun startAsForeground(reading: ChargeReading) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            container.notificationFactory.create(reading),
            type,
        )
    }

    private fun startMonitoring() {
        monitorJob = scope.launch {
            try {
                while (isActive) {
                val sample = container.batteryDataSource.readSample()
                val rawPower = calculateChargingPower(sample.voltageMillivolts, sample.currentMicroAmps)
                val stablePower = stabilizePowerReading.add(rawPower)
                val reading = ChargeReading(
                    sample = sample,
                    powerWatts = stablePower,
                    status = stabilizeMonitorStatus.update(observeChargingState(sample, stablePower)),
                )
                container.chargeMonitorRepository.publish(reading)
                    if (container.settingsRepository.isTrendRecordingEnabled()) {
                        container.trendHistoryRepository.record(reading)
                    }
                    if (monitorUpdatePolicy.shouldRefreshNotification(reading)) {
                        startAsForeground(reading)
                    }
                    delay(monitorUpdatePolicy.nextSampleDelayMillis(reading.status, powerManager.isInteractive))
                }
            } finally {
                container.trendHistoryRepository.flush()
            }
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        container.chargeMonitorRepository.publish(ChargeReading(status = MonitorStatus.DISABLED))
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
