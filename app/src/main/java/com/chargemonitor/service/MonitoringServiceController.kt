package com.chargemonitor.service

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

object MonitoringServiceController {
    fun start(context: Context): Boolean = try {
        ContextCompat.startForegroundService(context, Intent(context, ChargeMonitoringService::class.java))
        true
    } catch (exception: ForegroundServiceStartNotAllowedException) {
        Log.w(TAG, "Foreground service start was restricted by the system.", exception)
        false
    } catch (exception: SecurityException) {
        Log.w(TAG, "Foreground service start was denied.", exception)
        false
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, ChargeMonitoringService::class.java))
    }

    private const val TAG = "ChargeMonitor"
}
