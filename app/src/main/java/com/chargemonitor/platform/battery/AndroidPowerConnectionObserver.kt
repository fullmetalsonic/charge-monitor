package com.chargemonitor.platform.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

/** Delivers physical power connection changes while the monitoring service is running. */
class AndroidPowerConnectionObserver(
    context: Context,
    private val onEvent: (PowerConnectionEvent) -> Unit,
) {
    private val appContext = context.applicationContext
    private var receiver: BroadcastReceiver? = null

    fun register() {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> onEvent(PowerConnectionEvent.CONNECTED)
                    Intent.ACTION_POWER_DISCONNECTED -> onEvent(PowerConnectionEvent.DISCONNECTED)
                }
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            requireNotNull(receiver),
            IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun unregister() {
        receiver?.let(appContext::unregisterReceiver)
        receiver = null
    }
}
