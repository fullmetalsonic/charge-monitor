package com.chargemonitor.data.repository

import com.chargemonitor.data.model.ChargeReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChargeMonitorRepository {
    private val _reading = MutableStateFlow(ChargeReading())
    val reading: StateFlow<ChargeReading> = _reading.asStateFlow()

    fun publish(reading: ChargeReading) {
        _reading.value = reading
    }
}
