package com.chargemonitor.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chargemonitor.R
import com.chargemonitor.data.model.TrendRecordingInterval
import com.chargemonitor.ui.design.Muted

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TrendRecordingSettingsSheet(
    selectedInterval: TrendRecordingInterval,
    onIntervalSelected: (TrendRecordingInterval) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)) {
            Text(stringResource(R.string.trend_recording_settings), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            IntervalOption(TrendRecordingInterval.STANDARD, selectedInterval, onIntervalSelected)
            IntervalOption(TrendRecordingInterval.PRECISION, selectedInterval, onIntervalSelected)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IntervalOption(
    interval: TrendRecordingInterval,
    selectedInterval: TrendRecordingInterval,
    onIntervalSelected: (TrendRecordingInterval) -> Unit,
) {
    val title = when (interval) {
        TrendRecordingInterval.STANDARD -> stringResource(R.string.trend_recording_standard)
        TrendRecordingInterval.PRECISION -> stringResource(R.string.trend_recording_precision)
    }
    val description = when (interval) {
        TrendRecordingInterval.STANDARD -> stringResource(R.string.trend_recording_description_standard)
        TrendRecordingInterval.PRECISION -> stringResource(R.string.trend_recording_description_precision)
    }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onIntervalSelected(interval) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = interval == selectedInterval, onClick = null)
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
