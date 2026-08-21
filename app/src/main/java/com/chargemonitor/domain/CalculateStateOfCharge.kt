package com.chargemonitor.domain

import kotlin.math.roundToInt

class CalculateStateOfCharge {
    operator fun invoke(level: Int?, scale: Int?): Int? {
        if (level == null || scale == null || level < 0 || scale <= 0) return null
        return (level * 100.0 / scale).roundToInt().coerceIn(0, 100)
    }
}
