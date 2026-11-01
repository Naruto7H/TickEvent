package com.example.nextup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nextup.domain.model.IndianEventTemplate
import com.example.nextup.ui.addedit.AddEditEventScreen
import com.example.nextup.ui.detail.EventDetailScreen
import com.example.nextup.ui.home.HomeScreen
import com.example.nextup.ui.viewmodel.EventViewModel

@Composable
fun NextUpNavHost(
    startEventId: Long? = null,
    viewModel: EventViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var pendingTemplate by remember { mutableStateOf<IndianEventTemplate?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (startEventId != null && startEventId > 0L) {
            "detail/$startEventId"
        } else {
            "home"
        }
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddEvent = { template ->
                    pendingTemplate = template
                    navController.navigate("add_edit")
                },
                onNavigateToEventDetail = { eventId ->
                    navController.navigate("detail/$eventId")
                }
            )
        }

        composable("add_edit") {
            AddEditEventScreen(
                viewModel = viewModel,
                initialTemplate = pendingTemplate,
                onNavigateBack = {
                    pendingTemplate = null
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            EventDetailScreen(
                eventId = eventId,
                viewModel = viewModel,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate("home")
                    }
                }
            )
        }
    }
}
