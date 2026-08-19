package com.chargemonitor.platform.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.ui.MainActivity
import com.chargemonitor.util.PowerFormatter

class ChargeNotificationFactory(private val context: Context) {
    fun create(reading: ChargeReading): android.app.Notification {
        NotificationChannelManager.ensureCreated(context)
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val content = when (reading.status) {
            MonitorStatus.CHARGING -> "${PowerFormatter.watts(reading.powerWatts)} 충전 중"
            MonitorStatus.MEASUREMENT_UNAVAILABLE -> "충전 중 · 전류 측정 불가"
            else -> "충전 모니터"
        }
        return NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("충전 모니터")
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }
}
