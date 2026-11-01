package com.example.nextup.alarm

import android.content.Context
import android.content.Intent
import android.net.Uri

object ExternalActionLauncher {

    fun launchEventAction(
        context: Context,
        actionPackage: String?,
        actionUrl: String?
    ) {
        if (!actionPackage.isNullOrBlank()) {
            val launchAppIntent = context.packageManager.getLaunchIntentForPackage(actionPackage)
            if (launchAppIntent != null) {
                launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchAppIntent)
                return
            }
        }

        val targetUrl = if (!actionUrl.isNullOrBlank()) actionUrl else "https://www.google.com"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(browserIntent)
        }
    }
}
