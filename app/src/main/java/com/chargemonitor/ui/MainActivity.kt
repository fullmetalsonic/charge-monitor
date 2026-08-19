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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            var diagnosticOpen by mutableStateOf(false)
            ChargeMonitorTheme {
                Surface {
                    BackHandler(enabled = diagnosticOpen) { diagnosticOpen = false }
                    if (diagnosticOpen) {
                        val reading by viewModel.reading.collectAsStateWithLifecycle()
                        DiagnosticScreen(reading)
                    } else {
                        DashboardScreen(viewModel, onOpenDiagnostic = { diagnosticOpen = true })
                    }
                }
            }
        }
    }
}
