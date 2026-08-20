package com.chargemonitor.app

import android.content.Context
import com.chargemonitor.data.repository.ChargeMonitorRepository
import com.chargemonitor.data.repository.SettingsRepository
import com.chargemonitor.data.repository.TrendHistoryRepository
import com.chargemonitor.platform.battery.AndroidBatteryDataSource
import com.chargemonitor.platform.notification.ChargeNotificationFactory

class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)
    val trendHistoryRepository = TrendHistoryRepository(context)
    val chargeMonitorRepository = ChargeMonitorRepository()
    val batteryDataSource = AndroidBatteryDataSource(context)
    val notificationFactory = ChargeNotificationFactory(context)
}
