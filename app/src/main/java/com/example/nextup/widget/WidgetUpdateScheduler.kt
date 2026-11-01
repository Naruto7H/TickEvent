package com.example.nextup.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.nextup.di.WidgetEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WidgetSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(applicationContext)
        return Result.success()
    }
}

object WidgetUpdateScheduler {

    private const val WIDGET_ALARM_REQUEST_CODE = 9001
    private const val WORK_NAME_PERIODIC_WIDGET_SYNC = "nextup_periodic_widget_sync"

    fun enqueuePeriodicWidgetSync(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WidgetSyncWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC_WIDGET_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    suspend fun refreshAllWidgetsAndScheduleNext(context: Context) {
        NextUpGlanceWidget().updateAll(context)

        val now = System.currentTimeMillis()
        val dao = WidgetEntryPoint.getEventDao(context)
        val nearestTarget = withContext(Dispatchers.IO) {
            dao.getAllEventsOneShot()
                .firstOrNull { it.targetDate > now }
                ?.targetDate
        } ?: return

        val remainingMillis = nearestTarget - now
        val nextTriggerMillis = when {
            remainingMillis <= TimeUnit.HOURS.toMillis(24) -> {
                now + TimeUnit.MINUTES.toMillis(1) - (now % TimeUnit.MINUTES.toMillis(1))
            }
            else -> {
                now + TimeUnit.HOURS.toMillis(1) - (now % TimeUnit.HOURS.toMillis(1))
            }
        }

        scheduleNextTickAlarm(context, nextTriggerMillis)
    }

    private fun scheduleNextTickAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NextUpWidgetReceiver::class.java).apply {
            action = NextUpWidgetReceiver.ACTION_WIDGET_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WIDGET_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setWindow(
                AlarmManager.RTC,
                triggerAtMillis,
                TimeUnit.MINUTES.toMillis(5),
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
