package com.chargemonitor.platform.notification

import android.os.Build

object LiveUpdateSupport {
    private const val ANDROID_16_QPR1_FULL_SDK = 3_600_001

    fun canRequestPromotedOngoing(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            Build.VERSION.SDK_INT_FULL >= ANDROID_16_QPR1_FULL_SDK
}
