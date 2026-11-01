package com.example.nextup.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object WidgetPinHelper {

    fun requestPinNextUpWidget(context: Context, eventId: Long) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return

        val provider = ComponentName(context, NextUpWidgetReceiver::class.java)
        val callbackIntent = Intent(context, NextUpWidgetReceiver::class.java).apply {
            action = NextUpWidgetReceiver.ACTION_WIDGET_PINNED
            putExtra(NextUpWidgetReceiver.EXTRA_PINNED_EVENT_ID, eventId)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val successCallback = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            callbackIntent,
            flags
        )

        appWidgetManager.requestPinAppWidget(provider, null, successCallback)
    }
}
