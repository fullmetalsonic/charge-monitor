package com.chargemonitor.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chargemonitor.app.ChargeMonitorApplication
import com.chargemonitor.service.MonitoringServiceController
import com.chargemonitor.ui.dashboard.DashboardScreen
import com.chargemonitor.ui.dashboard.DashboardViewModel
import com.chargemonitor.ui.design.ChargeMonitorTheme
import com.chargemonitor.ui.diagnostic.DiagnosticScreen
import com.chargemonitor.ui.trend.TrendScreen
import com.chargemonitor.ui.trend.TrendViewModel

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as ChargeMonitorApplication).container }
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.factory(
            monitorRepository = container.chargeMonitorRepository,
            settingsRepository = container.settingsRepository,
            startMonitoring = { MonitoringServiceController.start(this) },
            stopMonitoring = { MonitoringServiceController.stop(this) },
        )
    }
    private val requestNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val trendViewModel: TrendViewModel by viewModels {
        TrendViewModel.factory(container.trendHistoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            var screen by mutableStateOf(Screen.DASHBOARD)
            ChargeMonitorTheme {
                Surface {
                    BackHandler(enabled = screen != Screen.DASHBOARD) { screen = Screen.DASHBOARD }
                    when (screen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            viewModel,
                            onOpenDiagnostic = { screen = Screen.DIAGNOSTIC },
                            onOpenTrend = { screen = Screen.TREND },
                        )
                        Screen.DIAGNOSTIC -> {
                            val reading by viewModel.reading.collectAsStateWithLifecycle()
                            DiagnosticScreen(reading)
                        }
                        Screen.TREND -> TrendScreen(trendViewModel)
                    }
                }
            }
        }
    }

    private enum class Screen { DASHBOARD, DIAGNOSTIC, TREND }
}
