package com.chargemonitor.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chargemonitor.R
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.ui.design.GaugeTrack
import com.chargemonitor.ui.design.Lime
import com.chargemonitor.ui.design.Muted
import com.chargemonitor.util.PowerFormatter

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onOpenDiagnostic: () -> Unit, onOpenTrend: () -> Unit) {
    val reading by viewModel.reading.collectAsStateWithLifecycle()
    val enabled by viewModel.autoMonitoringEnabled.collectAsStateWithLifecycle()
    val trendRecordingEnabled by viewModel.trendRecordingEnabled.collectAsStateWithLifecycle()
    val monitoringStartFailed by viewModel.monitoringStartFailed.collectAsStateWithLifecycle()
    DashboardContent(
        reading = reading,
        enabled = enabled,
        onEnabledChange = viewModel::setAutoMonitoringEnabled,
        trendRecordingEnabled = trendRecordingEnabled,
        onTrendRecordingEnabledChange = viewModel::setTrendRecordingEnabled,
        monitoringStartFailed = monitoringStartFailed,
        onOpenDiagnostic = onOpenDiagnostic,
        onOpenTrend = onOpenTrend,
    )
}

@Composable
private fun DashboardContent(
    reading: ChargeReading,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    trendRecordingEnabled: Boolean,
    onTrendRecordingEnabledChange: (Boolean) -> Unit,
    monitoringStartFailed: Boolean,
    onOpenDiagnostic: () -> Unit,
    onOpenTrend: () -> Unit,
) {
    val sample = reading.sample
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 16.dp),
    ) {
        if (maxWidth > maxHeight) {
            DashboardLandscape(
                reading = reading,
                batteryPercent = sample?.levelPercent,
                enabled = enabled,
                onEnabledChange = onEnabledChange,
                trendRecordingEnabled = trendRecordingEnabled,
                onTrendRecordingEnabledChange = onTrendRecordingEnabledChange,
                monitoringStartFailed = monitoringStartFailed,
                onOpenTrend = onOpenTrend,
                onOpenDiagnostic = onOpenDiagnostic,
            )
        } else {
            DashboardPortrait(
                reading = reading,
                batteryPercent = sample?.levelPercent,
                enabled = enabled,
                onEnabledChange = onEnabledChange,
                trendRecordingEnabled = trendRecordingEnabled,
                onTrendRecordingEnabledChange = onTrendRecordingEnabledChange,
                monitoringStartFailed = monitoringStartFailed,
                onOpenTrend = onOpenTrend,
                onOpenDiagnostic = onOpenDiagnostic,
            )
        }
    }
}

@Composable
private fun DashboardPortrait(
    reading: ChargeReading,
    batteryPercent: Int?,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    trendRecordingEnabled: Boolean,
    onTrendRecordingEnabledChange: (Boolean) -> Unit,
    monitoringStartFailed: Boolean,
    onOpenTrend: () -> Unit,
    onOpenDiagnostic: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        PowerGauge(reading = reading, batteryPercent = batteryPercent)
        Spacer(Modifier.height(18.dp))
        MeasurementSummary(reading, batteryPercent)
        Spacer(Modifier.height(28.dp))
        DashboardControls(
            enabled, onEnabledChange, trendRecordingEnabled, onTrendRecordingEnabledChange,
            monitoringStartFailed, onOpenTrend, onOpenDiagnostic,
        )
    }
}

@Composable
private fun DashboardLandscape(
    reading: ChargeReading,
    batteryPercent: Int?,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    trendRecordingEnabled: Boolean,
    onTrendRecordingEnabledChange: (Boolean) -> Unit,
    monitoringStartFailed: Boolean,
    onOpenTrend: () -> Unit,
    onOpenDiagnostic: () -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PowerGauge(reading, batteryPercent, 250.dp)
            Spacer(Modifier.height(8.dp))
            MeasurementSummary(reading, batteryPercent)
        }
        Spacer(Modifier.size(28.dp))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DashboardControls(
                enabled, onEnabledChange, trendRecordingEnabled, onTrendRecordingEnabledChange,
                monitoringStartFailed, onOpenTrend, onOpenDiagnostic,
            )
        }
    }
}

@Composable
private fun MeasurementSummary(reading: ChargeReading, batteryPercent: Int?) {
    val sample = reading.sample
    Text(
        text = listOfNotNull(
            batteryPercent?.let { "$it%" },
            PowerFormatter.volts(sample?.voltageMillivolts),
            PowerFormatter.amps(sample?.currentMicroAmps),
        ).joinToString("  ·  "),
        color = Muted,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun PowerGauge(reading: ChargeReading, batteryPercent: Int?, gaugeSize: androidx.compose.ui.unit.Dp = 310.dp) {
    val power = reading.powerWatts
    val status = when (reading.status) {
        MonitorStatus.STARTING -> stringResource(R.string.status_checking)
        MonitorStatus.DISCHARGING -> stringResource(R.string.status_discharging)
        MonitorStatus.CHARGING -> stringResource(R.string.status_charging)
        MonitorStatus.FULL -> stringResource(R.string.charge_complete)
        MonitorStatus.MEASUREMENT_UNAVAILABLE -> stringResource(R.string.status_unavailable)
        MonitorStatus.IDLE -> stringResource(R.string.status_idle)
        MonitorStatus.DISABLED -> stringResource(R.string.status_disabled)
    }
    Box(modifier = Modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(GaugeTrack, 136f, 268f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            val sweep = ((batteryPercent ?: 0).coerceIn(0, 100) / 100f) * 268f
            drawArc(Lime, 136f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.BatteryChargingFull, null, tint = Lime, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(14.dp))
            if (reading.status == MonitorStatus.FULL) {
                Text(stringResource(R.string.charge_complete), fontSize = 42.sp, fontWeight = FontWeight.Light)
            } else if (reading.status == MonitorStatus.STARTING) {
                Text(stringResource(R.string.checking), fontSize = 42.sp, fontWeight = FontWeight.Light)
            } else if (power != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(PowerFormatter.watts(power).removeSuffix("W"), fontSize = 64.sp, fontWeight = FontWeight.Light)
                    Text("W", modifier = Modifier.padding(start = 8.dp, bottom = 11.dp), fontSize = 28.sp)
                }
            } else {
                Text(stringResource(R.string.measurement_unavailable), fontSize = 42.sp, fontWeight = FontWeight.Light)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                status,
                color = if (reading.status == MonitorStatus.CHARGING || reading.status == MonitorStatus.FULL) Lime else Muted,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
