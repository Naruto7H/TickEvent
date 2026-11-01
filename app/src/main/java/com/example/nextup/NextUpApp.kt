package com.example.nextup

import android.app.Application
import com.example.nextup.widget.WidgetUpdateScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NextUpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WidgetUpdateScheduler.enqueuePeriodicWidgetSync(this)
    }
}
