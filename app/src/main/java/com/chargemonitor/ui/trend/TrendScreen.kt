package com.chargemonitor.ui.trend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chargemonitor.R
import com.chargemonitor.data.model.DailyTrendSummary
import com.chargemonitor.data.model.TrendDirection
import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.domain.TrendTimeline
import com.chargemonitor.ui.design.Divider
import com.chargemonitor.ui.design.GaugeTrack
import com.chargemonitor.ui.design.Ink
import com.chargemonitor.ui.design.Lime
import com.chargemonitor.ui.design.Muted
import com.chargemonitor.ui.design.SlateSurface
import com.chargemonitor.util.PowerFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@Composable
fun TrendScreen(viewModel: TrendViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrendContent(
        state = state,
        onSelectDate = viewModel::selectDate,
        onShowPreviousDay = viewModel::showPreviousDay,
        onShowNextDay = viewModel::showNextDay,
    )
}

@Composable
private fun TrendContent(
    state: TrendUiState,
    onSelectDate: (LocalDate) -> Unit,
    onShowPreviousDay: () -> Unit,
    onShowNextDay: () -> Unit,
) {
    val summary = state.summary
    var selectedBucket by remember(summary.date) { mutableStateOf<Int?>(null) }
    val selectedRecord = selectedBucket?.let { bucket ->
        summary.records.firstOrNull { TrendTimeline.bucketForTimestamp(it.capturedAtMillis) == bucket }
    }
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 16.dp),
    ) {
        Text(stringResource(R.string.trend_history), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        DateStrip(state.recentDates, state.selectedDate, onSelectDate, onShowPreviousDay, onShowNextDay)
        Spacer(Modifier.height(26.dp))
        Text(stringResource(R.string.battery_flow), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (summary.records.isEmpty()) {
            EmptyTrendState()
        } else {
            Surface(color = SlateSurface, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp)) {
                    BatteryFlowChart(summary.records, selectedBucket) { selectedBucket = it }
                    selectedBucket?.let { bucket ->
                        Spacer(Modifier.height(10.dp))
                        TrendCursorInfo(bucket, selectedRecord)
                    }
                    Spacer(Modifier.height(4.dp))
                    DayTimelineLabels()
                    Spacer(Modifier.height(10.dp))
                    Legend()
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(stringResource(R.string.today_flow), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        SummaryRow(summary)
        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = Divider)
        Spacer(Modifier.height(22.dp))
        Text(stringResource(R.string.power_change), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (summary.records.isEmpty()) {
            EmptyTrendState()
        } else {
            PowerChart(summary.records, summary.peakWatts, selectedBucket) { selectedBucket = it }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.trend_average_note), color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onShowPreviousDay: () -> Unit,
    onShowNextDay: () -> Unit,
) {
    var totalDrag by remember { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        when {
                            totalDrag <= -SWIPE_THRESHOLD -> onShowNextDay()
                            totalDrag >= SWIPE_THRESHOLD -> onShowPreviousDay()
                        }
                        totalDrag = 0f
                    },
                    onDragCancel = { totalDrag = 0f },
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        dates.forEach { date ->
            val selected = date == selectedDate
            Column(
                modifier = Modifier
                    .width(42.dp)
                    .clickable { onSelectDate(date) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(date.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN)), color = if (selected) Lime else Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Surface(color = if (selected) GaugeTrack else androidx.compose.ui.graphics.Color.Transparent, shape = RoundedCornerShape(18.dp)) {
                    Text(
                        date.dayOfMonth.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (selected) Lime else Ink,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

private const val SWIPE_THRESHOLD = 72f
private const val DAILY_BUCKET_COUNT = 288f

@Composable
private fun EmptyTrendState() {
    Surface(color = SlateSurface, shape = RoundedCornerShape(24.dp)) {
        Text(
            stringResource(R.string.no_trend_records),
            modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp, horizontal = 24.dp),
            color = Muted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun BatteryFlowChart(
    records: List<TrendRecord>,
    selectedBucket: Int?,
    onBucketSelected: (Int) -> Unit,
) {
    val cursorInput = rememberTrendCursorInput(onBucketSelected)
    Canvas(Modifier.fillMaxWidth().height(250.dp).then(cursorInput)) {
        val bottom = size.height - 18.dp.toPx()
        val chartHeight = bottom - 12.dp.toPx()
        val strokeWidth = max(2.dp.toPx(), size.width / DAILY_BUCKET_COUNT * 0.55f)
        records.forEach { record ->
            val x = size.width * TrendTimeline.dayFraction(record.capturedAtMillis)
            val y = bottom - chartHeight * record.batteryPercent / 100f
            val color = if (record.direction == TrendDirection.CHARGING) Lime else GaugeTrack
            drawLine(color, Offset(x, bottom), Offset(x, y), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        drawTrendCursor(selectedBucket, records) { record -> bottom - chartHeight * record.batteryPercent / 100f }
    }
}

@Composable
private fun PowerChart(
    records: List<TrendRecord>,
    peakWatts: Double?,
    selectedBucket: Int?,
    onBucketSelected: (Int) -> Unit,
) {
    val cursorInput = rememberTrendCursorInput(onBucketSelected)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.power_watts), color = Muted, style = MaterialTheme.typography.bodyMedium)
            peakWatts?.let { Text("${stringResource(R.string.power_peak)} ${PowerFormatter.watts(it)}", color = Lime, style = MaterialTheme.typography.bodyMedium) }
        }
        Spacer(Modifier.height(8.dp))
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(SlateSurface, RoundedCornerShape(20.dp))
                .then(cursorInput),
        ) {
            val values = records.map { it.powerWatts ?: 0.0 }
            val scaleMax = max(30.0, values.maxOrNull() ?: 0.0)
            val bottom = size.height - 14.dp.toPx()
            val chartHeight = bottom - 12.dp.toPx()
            val strokeWidth = max(2.dp.toPx(), size.width / DAILY_BUCKET_COUNT * 0.55f)
            values.forEachIndexed { index, value ->
                val x = size.width * TrendTimeline.dayFraction(records[index].capturedAtMillis)
                val y = bottom - (value / scaleMax * chartHeight).toFloat()
                val color = if (records[index].direction == TrendDirection.CHARGING) Lime else GaugeTrack
                drawLine(color, Offset(x, bottom), Offset(x, y), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            }
            drawTrendCursor(selectedBucket, records) { record ->
                record.powerWatts?.let { bottom - (it / scaleMax * chartHeight).toFloat() }
            }
        }
        Spacer(Modifier.height(4.dp))
        DayTimelineLabels()
    }
}

@Composable
private fun rememberTrendCursorInput(onBucketSelected: (Int) -> Unit): Modifier {
    val latestOnBucketSelected = rememberUpdatedState(onBucketSelected)
    fun selectAt(x: Float, width: IntSize) {
        if (width.width > 0) {
            latestOnBucketSelected.value(TrendTimeline.bucketForFraction(x / width.width))
        }
    }
    return Modifier
        .pointerInput(Unit) {
            detectTapGestures { offset -> selectAt(offset.x, size) }
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { offset -> selectAt(offset.x, size) },
                onHorizontalDrag = { change, _ -> selectAt(change.position.x, size) },
            )
        }
}

private fun DrawScope.drawTrendCursor(
    selectedBucket: Int?,
    records: List<TrendRecord>,
    yForRecord: (TrendRecord) -> Float?,
) {
    val bucket = selectedBucket ?: return
    val x = size.width * TrendTimeline.fractionForBucket(bucket)
    drawLine(Lime.copy(alpha = 0.8f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5.dp.toPx())
    records.firstOrNull { TrendTimeline.bucketForTimestamp(it.capturedAtMillis) == bucket }
        ?.let(yForRecord)
        ?.let { y -> drawCircle(Lime, radius = 4.dp.toPx(), center = Offset(x, y)) }
}

@Composable
private fun TrendCursorInfo(selectedBucket: Int, record: TrendRecord?) {
    Surface(color = GaugeTrack.copy(alpha = 0.45f), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(TrendTimeline.timeLabel(selectedBucket), color = Lime, style = MaterialTheme.typography.titleSmall)
            if (record == null) {
                Text(stringResource(R.string.trend_no_record_at_time), color = Muted, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    stringResource(R.string.trend_battery_level, record.batteryPercent),
                    color = Ink,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(cursorPowerLabel(record), color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun cursorPowerLabel(record: TrendRecord): String = when (record.direction) {
    TrendDirection.CHARGING -> record.powerWatts?.let { stringResource(R.string.notification_charging, PowerFormatter.watts(it)) }
        ?: stringResource(R.string.status_charging)
    TrendDirection.DISCHARGING -> record.powerWatts?.let { stringResource(R.string.notification_discharging, PowerFormatter.watts(it)) }
        ?: stringResource(R.string.status_discharging)
    TrendDirection.IDLE -> stringResource(R.string.status_idle)
}

@Composable
private fun DayTimelineLabels() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("00", "06", "12", "18", "24").forEach { hour ->
            Text(hour, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        LegendItem(Lime, stringResource(R.string.charging))
        LegendItem(GaugeTrack, stringResource(R.string.discharging))
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(10.dp).background(color, RoundedCornerShape(10.dp)))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryRow(summary: DailyTrendSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SummaryMetric(stringResource(R.string.charge_sessions), "${summary.chargingSessions}${stringResource(R.string.times)}")
        SummaryMetric(stringResource(R.string.gained), "+${summary.gainedPercent}%", Lime)
        SummaryMetric(stringResource(R.string.discharged), "${summary.dischargedPercent}%")
        SummaryMetric(stringResource(R.string.power_peak), summary.peakWatts?.let(PowerFormatter::watts) ?: "—", Lime)
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = Ink) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Text(value, color = valueColor, fontSize = 19.sp, fontWeight = FontWeight.Medium)
    }
}
