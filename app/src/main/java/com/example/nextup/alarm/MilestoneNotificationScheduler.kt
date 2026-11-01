package com.example.nextup.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nextup.data.local.entity.EventEntity
import java.util.concurrent.TimeUnit

enum class MilestoneOffset(
    val code: Int,
    val offsetMillis: Long,
    val labelGenerator: (String) -> String
) {
    DAYS_7(
        code = 7,
        offsetMillis = TimeUnit.DAYS.toMillis(7),
        labelGenerator = { title -> "7 days left for $title!" }
    ),
    DAY_1(
        code = 1,
        offsetMillis = TimeUnit.DAYS.toMillis(1),
        labelGenerator = { title -> "Tomorrow: 24 hours left for $title!" }
    ),
    HOUR_1(
        code = 2,
        offsetMillis = TimeUnit.HOURS.toMillis(1),
        labelGenerator = { title -> "1 hour left for $title — get ready!" }
    ),
    ZERO_HOUR(
        code = 0,
        offsetMillis = 0L,
        labelGenerator = { title -> "$title has reached 00:00:00! Tap to open." }
    )
}

object MilestoneNotificationScheduler {

    fun scheduleMilestonesForEvent(context: Context, event: EventEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        MilestoneOffset.entries.forEach { milestone ->
            val triggerAt = event.targetDate - milestone.offsetMillis
            if (triggerAt > now) {
                val pendingIntent = buildPendingIntent(
                    context = context,
                    eventId = event.id,
                    eventTitle = event.title,
                    message = milestone.labelGenerator(event.title),
                    milestoneCode = milestone.code
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelMilestonesForEvent(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        MilestoneOffset.entries.forEach { milestone ->
            val pendingIntent = buildPendingIntent(
                context = context,
                eventId = eventId,
                eventTitle = "",
                message = "",
                milestoneCode = milestone.code
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun buildPendingIntent(
        context: Context,
        eventId: Long,
        eventTitle: String,
        message: String,
        milestoneCode: Int
    ): PendingIntent {
        val intent = Intent(context, MilestoneAlarmReceiver::class.java).apply {
            putExtra(MilestoneAlarmReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(MilestoneAlarmReceiver.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(MilestoneAlarmReceiver.EXTRA_MESSAGE, message)
        }
        val uniqueRequestCode = (eventId.toInt() * 10) + milestoneCode
        return PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
