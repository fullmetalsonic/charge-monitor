package com.chargemonitor.data.model

/** Averaging interval selected for local trend history. */
enum class TrendRecordingInterval(val minutes: Int) {
    STANDARD(minutes = 5),
    PRECISION(minutes = 1),
    ;

    val millis: Long get() = minutes * 60_000L
    val bucketsPerDay: Int get() = 24 * 60 / minutes

    companion object {
        fun fromPreference(value: String?): TrendRecordingInterval =
            entries.firstOrNull { it.name == value } ?: STANDARD
    }
}
