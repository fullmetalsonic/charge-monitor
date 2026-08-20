package com.chargemonitor.ui.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.chargemonitor.data.model.DailyTrendSummary
import com.chargemonitor.data.model.TrendRecordingInterval
import com.chargemonitor.data.model.TrendRecord
import com.chargemonitor.data.repository.SettingsRepository
import com.chargemonitor.data.repository.TrendHistoryRepository
import com.chargemonitor.domain.BuildDailyTrendSummary
import com.chargemonitor.domain.BuildTrendDisplayRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class TrendUiState(
    val selectedDate: LocalDate,
    val recentDates: List<LocalDate>,
    val summary: DailyTrendSummary,
    val displayRecords: List<TrendRecord>,
    val recordingInterval: TrendRecordingInterval,
)

class TrendViewModel(
    historyRepository: TrendHistoryRepository,
    settingsRepository: SettingsRepository,
    private val buildDailyTrendSummary: BuildDailyTrendSummary = BuildDailyTrendSummary(),
    private val buildTrendDisplayRecords: BuildTrendDisplayRecords = BuildTrendDisplayRecords(),
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val visibleEndDate = MutableStateFlow(LocalDate.now())

    val uiState = combine(historyRepository.records, selectedDate, visibleEndDate, settingsRepository.trendRecordingInterval) { records, date, endDate, interval ->
        val summary = buildDailyTrendSummary(records, date)
        TrendUiState(
            selectedDate = date,
            recentDates = (6 downTo 0).map { endDate.minusDays(it.toLong()) },
            summary = summary,
            displayRecords = buildTrendDisplayRecords(summary.records, interval),
            recordingInterval = interval,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TrendUiState(
            selectedDate = LocalDate.now(),
            recentDates = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) },
            summary = buildDailyTrendSummary(emptyList(), LocalDate.now()),
            displayRecords = emptyList(),
            recordingInterval = TrendRecordingInterval.STANDARD,
        ),
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun showPreviousDay() {
        val endDate = visibleEndDate.value.minusDays(DAYS_PER_SWIPE)
        visibleEndDate.value = endDate
        selectedDate.value = endDate
    }

    fun showNextDay() {
        val endDate = minOf(visibleEndDate.value.plusDays(DAYS_PER_SWIPE), LocalDate.now())
        visibleEndDate.value = endDate
        selectedDate.value = endDate
    }

    fun showToday() {
        val today = LocalDate.now()
        visibleEndDate.value = today
        selectedDate.value = today
    }

    companion object {
        private const val DAYS_PER_SWIPE = 1L
        fun factory(historyRepository: TrendHistoryRepository, settingsRepository: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TrendViewModel(historyRepository, settingsRepository) }
        }
    }
}
