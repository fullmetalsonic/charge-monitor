package com.chargemonitor.domain

import kotlin.math.round

class StabilizePowerReading(private val windowSize: Int = 3) {
    private val samples = ArrayDeque<Double>()

    fun add(value: Double?): Double? {
        if (value == null) {
            samples.clear()
            return null
        }
        samples.addLast(value)
        while (samples.size > windowSize) samples.removeFirst()
        val ordered = samples.sorted()
        val median = ordered[ordered.size / 2]
        return round(median * 10) / 10
    }
}
