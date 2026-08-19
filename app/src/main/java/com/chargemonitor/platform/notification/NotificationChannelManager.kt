package com.chargemonitor.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelManager {
    const val CHANNEL_ID = "charging_monitor"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "충전 모니터",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "충전 전력 상태를 조용히 표시합니다."
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
