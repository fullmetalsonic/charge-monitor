package com.chargemonitor.platform.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.chargemonitor.data.model.BatterySample

class AndroidBatteryDataSource(private val context: Context) : BatteryDataSource {
    private val batteryManager = context.getSystemService(BatteryManager::class.java)

    override fun readSample(): BatterySample {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val rawCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        return BatterySample(
            voltageMillivolts = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 },
            currentMicroAmps = rawCurrent.takeIf { it != Int.MIN_VALUE },
            levelPercent = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.takeIf { it in 0..100 },
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
            isPlugged = plugged != 0,
            capturedAtMillis = System.currentTimeMillis(),
        )
    }
}
