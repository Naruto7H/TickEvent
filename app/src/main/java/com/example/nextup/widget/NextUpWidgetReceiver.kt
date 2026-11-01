package com.example.nextup.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NextUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextUpGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_TICK -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_WIDGET_PINNED -> {
                val pinnedEventId = intent.getLongExtra(EXTRA_PINNED_EVENT_ID, -1L)
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (pinnedEventId > 0L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val manager = GlanceAppWidgetManager(context)
                            val glanceId = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                manager.getGlanceIdBy(appWidgetId)
                            } else {
                                manager.getGlanceIds(NextUpGlanceWidget::class.java).lastOrNull()
                            }
                            if (glanceId != null) {
                                updateAppWidgetState(
                                    context = context,
                                    definition = PreferencesGlanceStateDefinition,
                                    glanceId = glanceId
                                ) { prefs ->
                                    prefs.toMutablePreferences().apply {
                                        this[NextUpGlanceWidget.KEY_PINNED_EVENT_ID] = pinnedEventId
                                    }
                                }
                                NextUpGlanceWidget().update(context, glanceId)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_TICK = "com.example.nextup.action.WIDGET_TICK"
        const val ACTION_WIDGET_PINNED = "com.example.nextup.action.WIDGET_PINNED"
        const val EXTRA_PINNED_EVENT_ID = "extra_pinned_event_id"
    }
}
