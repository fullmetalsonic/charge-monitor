package com.chargemonitor.app

import android.app.Application

class ChargeMonitorApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
