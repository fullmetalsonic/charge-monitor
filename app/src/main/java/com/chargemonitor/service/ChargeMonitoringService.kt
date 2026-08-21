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
import com.chargemonitor.domain.FilterPowerForStatus
import com.chargemonitor.domain.ObserveChargingState
import com.chargemonitor.domain.PowerConnectionConfirmation
import com.chargemonitor.domain.StabilizeMonitorStatus
import com.chargemonitor.domain.StabilizePowerReading
import com.chargemonitor.domain.MonitorUpdatePolicy
import com.chargemonitor.platform.battery.AndroidPowerConnectionObserver
import com.chargemonitor.platform.battery.PowerConnectionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

class ChargeMonitoringService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private val calculateChargingPower = CalculateChargingPower()
    private val filterPowerForStatus = FilterPowerForStatus()
    private val stabilizePowerReading = StabilizePowerReading()
    private val observeChargingState = ObserveChargingState()
    private val stabilizeMonitorStatus = StabilizeMonitorStatus()
    private val monitorUpdatePolicy = MonitorUpdatePolicy()
    private val powerConnectionConfirmation = PowerConnectionConfirmation()
    private val powerConnectionEvents = Channel<PowerConnectionEvent>(Channel.CONFLATED)

    private val container by lazy { (application as ChargeMonitorApplication).container }
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }
    private val powerConnectionObserver by lazy {
        AndroidPowerConnectionObserver(this) { event -> powerConnectionEvents.trySend(event) }
    }

    override fun onCreate() {
        super.onCreate()
        powerConnectionObserver.register()
    }

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
                val observedStatus = stabilizeMonitorStatus.update(observeChargingState(sample, stablePower))
                val confirmedStatus = powerConnectionConfirmation.resolve(observedStatus, sample, stablePower)
                val reading = ChargeReading(
                    sample = sample,
                    powerWatts = filterPowerForStatus(sample, confirmedStatus, stablePower),
                    status = confirmedStatus,
                )
                container.chargeMonitorRepository.publish(reading)
                    if (container.settingsRepository.isTrendRecordingEnabled()) {
                        container.trendHistoryRepository.record(
                            reading,
                            container.settingsRepository.trendRecordingInterval.value,
                        )
                    }
                    if (monitorUpdatePolicy.shouldRefreshNotification(reading)) {
                        startAsForeground(reading)
                    }
                    waitForNextSample(monitorUpdatePolicy.nextSampleDelayMillis(reading.status, powerManager.isInteractive))
                }
            } finally {
                container.trendHistoryRepository.flush()
            }
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        powerConnectionObserver.unregister()
        powerConnectionEvents.close()
        container.chargeMonitorRepository.publish(ChargeReading(status = MonitorStatus.DISABLED))
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun waitForNextSample(delayMillis: Long) {
        select<Unit> {
            onTimeout(delayMillis) { }
            powerConnectionEvents.onReceive { event ->
                when (event) {
                    PowerConnectionEvent.CONNECTED -> {
                        stabilizePowerReading.clear()
                        powerConnectionConfirmation.onPowerConnected(System.currentTimeMillis())
                    }
                    PowerConnectionEvent.DISCONNECTED -> {
                        stabilizePowerReading.clear()
                        powerConnectionConfirmation.onPowerDisconnected()
                    }
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
