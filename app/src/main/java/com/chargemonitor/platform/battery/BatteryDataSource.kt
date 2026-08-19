package com.chargemonitor.platform.battery

import com.chargemonitor.data.model.BatterySample

interface BatteryDataSource {
    fun readSample(): BatterySample
}
