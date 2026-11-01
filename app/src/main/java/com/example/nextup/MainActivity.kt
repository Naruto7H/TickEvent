package com.example.nextup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nextup.ui.navigation.NextUpNavHost
import com.example.nextup.ui.theme.NextUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialEventId = intent?.getLongExtra(EXTRA_EVENT_ID, -1L)?.takeIf { it > 0L }

        setContent {
            NextUpTheme {
                NextUpNavHost(startEventId = initialEventId)
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
