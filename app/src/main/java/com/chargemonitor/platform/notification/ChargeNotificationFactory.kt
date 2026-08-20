package com.chargemonitor.platform.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chargemonitor.R
import com.chargemonitor.data.model.ChargeReading
import com.chargemonitor.data.model.MonitorStatus
import com.chargemonitor.ui.MainActivity
import com.chargemonitor.util.PowerFormatter

class ChargeNotificationFactory(private val context: Context) {
    fun create(reading: ChargeReading, requestStatusBarWatt: Boolean): Notification {
        NotificationChannelManager.ensureCreated(context)
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val content = when (reading.status) {
            MonitorStatus.STARTING -> context.getString(R.string.status_checking)
            MonitorStatus.DISCHARGING -> reading.powerWatts?.let { context.getString(R.string.notification_discharging, PowerFormatter.watts(it)) } ?: context.getString(R.string.status_discharging)
            MonitorStatus.CHARGING -> context.getString(R.string.notification_charging, PowerFormatter.watts(reading.powerWatts))
            MonitorStatus.FULL -> context.getString(R.string.notification_full, reading.sample?.levelPercent ?: 100)
            MonitorStatus.MEASUREMENT_UNAVAILABLE -> context.getString(R.string.status_unavailable)
            MonitorStatus.IDLE -> context.getString(R.string.status_idle)
            MonitorStatus.DISABLED -> context.getString(R.string.status_disabled)
        }
        if (requestStatusBarWatt && LiveUpdateSupport.canRequestPromotedOngoing()) {
            return Notification.Builder(context, NotificationChannelManager.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setStyle(Notification.BigTextStyle().bigText(content))
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_STATUS)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setRequestPromotedOngoing(true)
                .setShortCriticalText(shortStatusBarText(reading, content))
                .build()
        }

        return NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun shortStatusBarText(reading: ChargeReading, fallback: String): String = when (reading.status) {
        MonitorStatus.CHARGING,
        MonitorStatus.DISCHARGING -> reading.powerWatts?.let(PowerFormatter::watts) ?: fallback
        MonitorStatus.FULL -> context.getString(R.string.charge_complete)
        else -> fallback
    }.take(MAX_STATUS_BAR_TEXT_LENGTH)

    private companion object {
        const val MAX_STATUS_BAR_TEXT_LENGTH = 7
    }
}
