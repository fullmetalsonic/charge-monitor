package com.chargemonitor.ui.diagnostic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chargemonitor.R
import com.chargemonitor.data.model.BatteryHealth
import com.chargemonitor.data.model.BatteryPowerSource
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.util.BatteryDiagnosticFormatter
import com.chargemonitor.util.PowerFormatter

@Composable
fun DiagnosticScreen(
    reading: ChargeReading,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val sample = reading.sample
    val batteryHealth = sample?.health
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.diagnostics), style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onRefresh, enabled = !isRefreshing) {
                Text(stringResource(if (isRefreshing) R.string.diagnostic_refreshing else R.string.diagnostic_refresh))
            }
        }
        Spacer(Modifier.height(18.dp))

        DiagnosticSection(stringResource(R.string.diagnostic_live_measurements))
        DiagnosticRow(
            stringResource(R.string.diagnostic_battery_power),
            reading.powerWatts?.let(PowerFormatter::watts) ?: "—",
        )
        DiagnosticRow(stringResource(R.string.diagnostic_voltage), PowerFormatter.volts(sample?.voltageMillivolts))
        DiagnosticRow(stringResource(R.string.diagnostic_current), PowerFormatter.amps(sample?.currentMicroAmps))
        DiagnosticRow(stringResource(R.string.diagnostic_average_current), PowerFormatter.amps(sample?.averageCurrentMicroAmps))
        DiagnosticRow(stringResource(R.string.diagnostic_soc), sample?.levelPercent?.let { "$it%" } ?: "—")
        DiagnosticRow(stringResource(R.string.diagnostic_updated_at), BatteryDiagnosticFormatter.capturedAt(sample?.capturedAtMillis))

        Spacer(Modifier.height(26.dp))
        DiagnosticSection(stringResource(R.string.diagnostic_battery_condition))
        DiagnosticRow(stringResource(R.string.diagnostic_temperature), BatteryDiagnosticFormatter.temperature(sample?.temperatureTenthsCelsius))
        DiagnosticRow(
            stringResource(R.string.diagnostic_health_status),
            if (batteryHealth == null) "—" else healthText(batteryHealth),
        )
        DiagnosticRow(stringResource(R.string.diagnostic_state_of_health), stringResource(R.string.diagnostic_system_not_provided))
        DiagnosticRow(stringResource(R.string.diagnostic_cycle_count), sample?.cycleCount?.toString() ?: "—")
        DiagnosticRow(stringResource(R.string.diagnostic_remaining_capacity), BatteryDiagnosticFormatter.milliampHours(sample?.chargeCounterMicroampHours))
        DiagnosticRow(stringResource(R.string.diagnostic_remaining_energy), BatteryDiagnosticFormatter.wattHours(sample?.energyCounterNanowattHours))
        DiagnosticRow(stringResource(R.string.diagnostic_time_remaining), timeRemainingText(sample?.chargeTimeRemainingMillis))

        Spacer(Modifier.height(26.dp))
        DiagnosticSection(stringResource(R.string.diagnostic_connection))
        DiagnosticRow(stringResource(R.string.diagnostic_power_source), powerSourceText(sample?.powerSources.orEmpty()))
        DiagnosticRow(stringResource(R.string.diagnostic_technology), sample?.technology ?: "—")
        DiagnosticRow(stringResource(R.string.diagnostic_status), statusText(reading.status))
        DiagnosticRow(stringResource(R.string.diagnostic_battery_present), presentText(sample?.isPresent))

        Spacer(Modifier.height(18.dp))
        Text(
            stringResource(R.string.diagnostic_disclaimer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DiagnosticSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun statusText(status: MonitorStatus): String = when (status) {
    MonitorStatus.STARTING -> stringResource(R.string.status_checking)
    MonitorStatus.DISCHARGING -> stringResource(R.string.status_discharging)
    MonitorStatus.CHARGING -> stringResource(R.string.status_charging)
    MonitorStatus.FULL -> stringResource(R.string.charge_complete)
    MonitorStatus.MEASUREMENT_UNAVAILABLE -> stringResource(R.string.status_unavailable)
    MonitorStatus.IDLE -> stringResource(R.string.status_idle)
    MonitorStatus.DISABLED -> stringResource(R.string.status_disabled)
}

@Composable
private fun healthText(health: BatteryHealth): String = stringResource(
    when (health) {
        BatteryHealth.GOOD -> R.string.diagnostic_health_good
        BatteryHealth.OVERHEAT -> R.string.diagnostic_health_overheat
        BatteryHealth.DEAD -> R.string.diagnostic_health_dead
        BatteryHealth.OVER_VOLTAGE -> R.string.diagnostic_health_over_voltage
        BatteryHealth.UNSPECIFIED_FAILURE -> R.string.diagnostic_health_failure
        BatteryHealth.COLD -> R.string.diagnostic_health_cold
        BatteryHealth.UNKNOWN -> R.string.diagnostic_health_unknown
    },
)

@Composable
private fun powerSourceText(sources: Set<BatteryPowerSource>): String {
    if (sources.isEmpty()) return stringResource(R.string.diagnostic_power_source_none)
    val labels = mutableListOf<String>()
    if (BatteryPowerSource.AC in sources) labels += stringResource(R.string.diagnostic_power_source_ac)
    if (BatteryPowerSource.USB in sources) labels += stringResource(R.string.diagnostic_power_source_usb)
    if (BatteryPowerSource.WIRELESS in sources) labels += stringResource(R.string.diagnostic_power_source_wireless)
    if (BatteryPowerSource.DOCK in sources) labels += stringResource(R.string.diagnostic_power_source_dock)
    return labels.joinToString(" · ")
}

@Composable
private fun timeRemainingText(millis: Long?): String {
    if (millis == null) return "—"
    val totalMinutes = (millis / 60_000).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.diagnostic_time_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.diagnostic_time_minutes, minutes)
    }
}

@Composable
private fun presentText(isPresent: Boolean?): String = when (isPresent) {
    true -> stringResource(R.string.diagnostic_yes)
    false -> stringResource(R.string.diagnostic_no)
    null -> "—"
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 18.dp))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}
