package com.chargemonitor.ui.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.chargemonitor.data.model.DailyTrendSummary
import com.chargemonitor.data.repository.TrendHistoryRepository
import com.chargemonitor.domain.BuildDailyTrendSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class TrendUiState(
    val selectedDate: LocalDate,
    val recentDates: List<LocalDate>,
    val summary: DailyTrendSummary,
)

class TrendViewModel(
    historyRepository: TrendHistoryRepository,
    private val buildDailyTrendSummary: BuildDailyTrendSummary = BuildDailyTrendSummary(),
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val visibleEndDate = MutableStateFlow(LocalDate.now())

    val uiState = combine(historyRepository.records, selectedDate, visibleEndDate) { records, date, endDate ->
        TrendUiState(
            selectedDate = date,
            recentDates = (6 downTo 0).map { endDate.minusDays(it.toLong()) },
            summary = buildDailyTrendSummary(records, date),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TrendUiState(
            selectedDate = LocalDate.now(),
            recentDates = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) },
            summary = buildDailyTrendSummary(emptyList(), LocalDate.now()),
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

    companion object {
        private const val DAYS_PER_SWIPE = 1L
        fun factory(historyRepository: TrendHistoryRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TrendViewModel(historyRepository) }
        }
    }
}
