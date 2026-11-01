package com.example.nextup.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nextup.di.WidgetEntryPoint
import com.example.nextup.widget.WidgetUpdateScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = WidgetEntryPoint.getEventDao(context)
                    val now = System.currentTimeMillis()
                    val upcomingEvents = dao.getAllEventsOneShot().filter { it.targetDate > now }

                    upcomingEvents.forEach { event ->
                        MilestoneNotificationScheduler.scheduleMilestonesForEvent(context, event)
                    }

                    WidgetUpdateScheduler.enqueuePeriodicWidgetSync(context)
                    WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
