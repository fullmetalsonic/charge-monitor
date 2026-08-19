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
import androidx.compose.ui.unit.dp
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.util.PowerFormatter

@Composable
fun DiagnosticScreen(reading: ChargeReading) {
    val sample = reading.sample
    Column(Modifier.fillMaxSize().padding(28.dp)) {
        Text("상세 진단", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(30.dp))
        DiagnosticRow("충전 전력", PowerFormatter.watts(reading.powerWatts))
        DiagnosticRow("전압", PowerFormatter.volts(sample?.voltageMillivolts))
        DiagnosticRow("전류", PowerFormatter.amps(sample?.currentMicroAmps))
        DiagnosticRow("배터리", sample?.levelPercent?.let { "$it%" } ?: "—")
        DiagnosticRow("상태", reading.status.name)
        Spacer(Modifier.height(18.dp))
        Text("표시값은 배터리에 들어가는 전력 추정값이며, 충전기 출력값과 다를 수 있습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
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
