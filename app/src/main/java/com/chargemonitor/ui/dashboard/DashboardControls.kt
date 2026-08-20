package com.chargemonitor.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chargemonitor.R
import com.chargemonitor.data.model.TrendRecordingInterval
import com.chargemonitor.ui.design.Muted

@Composable
internal fun DashboardControls(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    trendRecordingEnabled: Boolean,
    onTrendRecordingEnabledChange: (Boolean) -> Unit,
    trendRecordingInterval: TrendRecordingInterval,
    onOpenTrendRecordingSettings: () -> Unit,
    monitoringStartFailed: Boolean,
    onOpenTrend: () -> Unit,
    onOpenDiagnostic: () -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    SettingRow(
        title = stringResource(R.string.auto_monitoring),
        description = stringResource(R.string.monitoring_description),
        checked = enabled,
        onCheckedChange = onEnabledChange,
        error = monitoringStartFailed,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    SettingRow(
        title = stringResource(R.string.trend_recording),
        description = trendRecordingDescription(trendRecordingInterval),
        checked = trendRecordingEnabled,
        onCheckedChange = onTrendRecordingEnabledChange,
        onContentClick = onOpenTrendRecordingSettings,
        contentActionLabel = stringResource(R.string.trend_recording_settings),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    DashboardLink(stringResource(R.string.trend_history), onOpenTrend)
    DashboardLink(stringResource(R.string.diagnostics), onOpenDiagnostic)
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onContentClick: (() -> Unit)? = null,
    contentActionLabel: String? = null,
    error: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (onContentClick != null) Modifier.clickable(onClick = onContentClick) else Modifier),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
            if (contentActionLabel != null) {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "$contentActionLabel  ›",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            if (error) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.monitoring_start_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun trendRecordingDescription(interval: TrendRecordingInterval): String = when (interval) {
    TrendRecordingInterval.STANDARD -> stringResource(R.string.trend_recording_description_standard)
    TrendRecordingInterval.PRECISION -> stringResource(R.string.trend_recording_description_precision)
}

@Composable
private fun DashboardLink(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.titleMedium)
        Text("›", color = Muted, style = MaterialTheme.typography.headlineSmall)
    }
}
