package com.chargemonitor.domain

class CalculateChargingPower {
    operator fun invoke(voltageMillivolts: Int?, currentMicroAmps: Int?): Double? {
        if (voltageMillivolts == null || voltageMillivolts <= 0) return null
        if (currentMicroAmps == null || currentMicroAmps == Int.MIN_VALUE || currentMicroAmps <= 0) return null

        val volts = voltageMillivolts / 1_000.0
        val amps = currentMicroAmps / 1_000_000.0
        return volts * amps
    }
}
