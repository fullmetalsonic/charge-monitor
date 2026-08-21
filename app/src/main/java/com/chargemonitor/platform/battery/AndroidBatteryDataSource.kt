package com.chargemonitor.platform.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.chargemonitor.data.model.BatteryHealth
import com.chargemonitor.data.model.BatteryPowerSource
import com.chargemonitor.data.model.BatterySample
import com.chargemonitor.domain.CalculateStateOfCharge

class AndroidBatteryDataSource(private val context: Context) : BatteryDataSource {
    private val batteryManager = context.getSystemService(BatteryManager::class.java)
    private val batteryChangedFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private val calculateStateOfCharge = CalculateStateOfCharge()

    override fun readSample(): BatterySample {
        val intent = context.registerReceiver(null, batteryChangedFilter)
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val powerSources = powerSources(plugged)
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.takeIf { it >= 0 }
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)?.takeIf { it > 0 }

        return BatterySample(
            voltageMillivolts = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 },
            currentMicroAmps = intProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW),
            levelPercent = calculateStateOfCharge(level, scale),
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
            isFull = status == BatteryManager.BATTERY_STATUS_FULL,
            isPlugged = powerSources.isNotEmpty(),
            capturedAtMillis = System.currentTimeMillis(),
            averageCurrentMicroAmps = intProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE),
            temperatureTenthsCelsius = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.takeIf { it >= 0 },
            health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)?.takeIf { it >= 0 }?.let(::health),
            powerSources = powerSources,
            cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)?.takeIf { it >= 0 },
            chargeCounterMicroampHours = intProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER),
            energyCounterNanowattHours = longProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER),
            chargeTimeRemainingMillis = batteryManager.computeChargeTimeRemaining().takeIf { it >= 0 },
            technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() },
            isPresent = intent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true),
        )
    }

    private fun intProperty(property: Int): Int? = batteryManager
        .getIntProperty(property)
        .takeIf { it != Int.MIN_VALUE }

    private fun longProperty(property: Int): Long? = batteryManager
        .getLongProperty(property)
        .takeIf { it != Long.MIN_VALUE }

    private fun powerSources(plugged: Int): Set<BatteryPowerSource> = buildSet {
        if (plugged and BatteryManager.BATTERY_PLUGGED_AC != 0) add(BatteryPowerSource.AC)
        if (plugged and BatteryManager.BATTERY_PLUGGED_USB != 0) add(BatteryPowerSource.USB)
        if (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0) add(BatteryPowerSource.WIRELESS)
        if (plugged and BatteryManager.BATTERY_PLUGGED_DOCK != 0) add(BatteryPowerSource.DOCK)
    }

    private fun health(value: Int): BatteryHealth = when (value) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
        else -> BatteryHealth.UNKNOWN
    }
}
