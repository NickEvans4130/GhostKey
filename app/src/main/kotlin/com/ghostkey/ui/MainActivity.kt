package com.ghostkey.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ghostkey.ui.navigation.GhostKeyNavGraph
import com.ghostkey.ui.navigation.Screen
import com.ghostkey.ui.theme.GhostKeyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhostKeyTheme {
                val navController = rememberNavController()
                GhostKeyApp(navController)
            }
        }
    }
}

@Composable
private fun GhostKeyApp(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = { navController.navigate(Screen.Dashboard.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profiles") },
                    label = { Text("Profiles") },
                    selected = currentRoute == Screen.Profiles.route,
                    onClick = { navController.navigate(Screen.Profiles.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SmartToy, "Model") },
                    label = { Text("Model") },
                    selected = currentRoute == Screen.ModelManager.route,
                    onClick = { navController.navigate(Screen.ModelManager.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, "History") },
                    label = { Text("History") },
                    selected = currentRoute == Screen.History.route,
                    onClick = { navController.navigate(Screen.History.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    ) { innerPadding ->
        GhostKeyNavGraph(navController = navController)
    }
}
