package com.chargemonitor.platform.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chargemonitor.app.ChargeMonitorApplication
import com.chargemonitor.service.MonitoringServiceController

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val application = context.applicationContext as ChargeMonitorApplication
        if (application.container.settingsRepository.autoMonitoringEnabled.value) {
            MonitoringServiceController.start(context)
        }
    }
}
