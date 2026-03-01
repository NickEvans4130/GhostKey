package com.ghostkey.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghostkey.ui.dashboard.DashboardScreen
import com.ghostkey.ui.history.HistoryScreen
import com.ghostkey.ui.model.ModelManagerScreen
import com.ghostkey.ui.profiles.ProfileEditorScreen
import com.ghostkey.ui.profiles.ProfileListScreen
import com.ghostkey.ui.settings.SettingsScreen
import com.ghostkey.ui.stylometry.StylometryScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Profiles : Screen("profiles")
    object ProfileEditor : Screen("profile_editor/{profileId}") {
        fun createRoute(profileId: String) = "profile_editor/$profileId"
    }
    object Stylometry : Screen("stylometry")
    object ModelManager : Screen("model_manager")
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun GhostKeyNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Profiles.route) {
            ProfileListScreen(navController = navController)
        }
        composable(Screen.ProfileEditor.route) { backStack ->
            val profileId = backStack.arguments?.getString("profileId") ?: return@composable
            ProfileEditorScreen(profileId = profileId, navController = navController)
        }
        composable(Screen.Stylometry.route) {
            StylometryScreen()
        }
        composable(Screen.ModelManager.route) {
            ModelManagerScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
