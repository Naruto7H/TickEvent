package com.example.nextup.domain.model

import java.util.concurrent.TimeUnit
import kotlin.math.max

data class CountdownTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isCompleted: Boolean
) {
    fun formatDaysHoursMinutes(): String =
        "%02d : %02d : %02d".format(days, hours, minutes)

    companion object {
        fun fromTargetTimestamp(
            targetMillis: Long,
            nowMillis: Long = System.currentTimeMillis()
        ): CountdownTime {
            val diff = max(0L, targetMillis - nowMillis)
            val isCompleted = targetMillis <= nowMillis

            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

            return CountdownTime(
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                isCompleted = isCompleted
            )
        }
    }
}
