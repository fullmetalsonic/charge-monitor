package com.chargemonitor.ui.diagnostic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chargemonitor.R
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.util.PowerFormatter

@Composable
fun DiagnosticScreen(reading: ChargeReading) {
    val sample = reading.sample
    Column(Modifier.fillMaxSize().padding(28.dp)) {
        Text(stringResource(R.string.diagnostics), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(30.dp))
        DiagnosticRow(stringResource(R.string.diagnostic_power), PowerFormatter.watts(reading.powerWatts))
        DiagnosticRow(stringResource(R.string.diagnostic_voltage), PowerFormatter.volts(sample?.voltageMillivolts))
        DiagnosticRow(stringResource(R.string.diagnostic_current), PowerFormatter.amps(sample?.currentMicroAmps))
        DiagnosticRow(stringResource(R.string.diagnostic_battery), sample?.levelPercent?.let { "$it%" } ?: "—")
        DiagnosticRow(stringResource(R.string.diagnostic_status), statusText(reading.status))
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.diagnostic_disclaimer), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun statusText(status: MonitorStatus): String = when (status) {
    MonitorStatus.STARTING -> stringResource(R.string.status_checking)
    MonitorStatus.DISCHARGING -> stringResource(R.string.status_discharging)
    MonitorStatus.CHARGING -> stringResource(R.string.status_charging)
    MonitorStatus.FULL -> stringResource(R.string.status_full)
    MonitorStatus.MEASUREMENT_UNAVAILABLE -> stringResource(R.string.status_unavailable)
    MonitorStatus.IDLE -> stringResource(R.string.status_idle)
    MonitorStatus.DISABLED -> stringResource(R.string.status_disabled)
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}
