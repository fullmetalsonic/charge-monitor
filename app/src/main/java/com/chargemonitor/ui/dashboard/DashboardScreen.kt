package com.chargemonitor.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.ui.design.GaugeTrack
import com.chargemonitor.ui.design.Lime
import com.chargemonitor.ui.design.Muted
import com.chargemonitor.util.PowerFormatter

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onOpenDiagnostic: () -> Unit) {
    val reading by viewModel.reading.collectAsStateWithLifecycle()
    val enabled by viewModel.autoMonitoringEnabled.collectAsStateWithLifecycle()
    DashboardContent(
        reading = reading,
        enabled = enabled,
        onEnabledChange = viewModel::setAutoMonitoringEnabled,
        onOpenDiagnostic = onOpenDiagnostic,
    )
}

@Composable
private fun DashboardContent(
    reading: ChargeReading,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onOpenDiagnostic: () -> Unit,
) {
    val sample = reading.sample
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        PowerGauge(reading = reading, batteryPercent = sample?.levelPercent)
        Spacer(Modifier.height(18.dp))
        Text(
            text = listOfNotNull(
                sample?.levelPercent?.let { "$it%" },
                PowerFormatter.volts(sample?.voltageMillivolts),
                PowerFormatter.amps(sample?.currentMicroAmps),
            ).joinToString("  ·  "),
            color = Muted,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("자동 모니터링", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(5.dp))
                Text("충전·방전 상태를 알림으로 표시", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        Text(
            text = "상세 진단  ›",
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp).clickable(onClick = onOpenDiagnostic),
            color = Muted,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun PowerGauge(reading: ChargeReading, batteryPercent: Int?) {
    val power = reading.powerWatts
    val status = when (reading.status) {
        MonitorStatus.STARTING -> "충전 상태 확인 중"
        MonitorStatus.DISCHARGING -> "방전 중"
        MonitorStatus.CHARGING -> "충전 중"
        MonitorStatus.FULL -> "100% 충전됨"
        MonitorStatus.MEASUREMENT_UNAVAILABLE -> "전류 측정 불가"
        MonitorStatus.IDLE -> "충전 대기 중"
        MonitorStatus.DISABLED -> "자동 모니터링 꺼짐"
    }
    Box(modifier = Modifier.size(310.dp), contentAlignment = Alignment.Center) {
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
                Text("충전 완료", fontSize = 42.sp, fontWeight = FontWeight.Light)
            } else if (reading.status == MonitorStatus.STARTING) {
                Text("확인 중", fontSize = 42.sp, fontWeight = FontWeight.Light)
            } else if (power != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(PowerFormatter.watts(power).removeSuffix("W"), fontSize = 64.sp, fontWeight = FontWeight.Light)
                    Text("W", modifier = Modifier.padding(start = 8.dp, bottom = 11.dp), fontSize = 28.sp)
                }
            } else {
                Text("측정 불가", fontSize = 42.sp, fontWeight = FontWeight.Light)
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
