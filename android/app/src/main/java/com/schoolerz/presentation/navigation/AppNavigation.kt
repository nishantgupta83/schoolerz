package com.schoolerz.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.schoolerz.presentation.feed.FeedScreen
import com.schoolerz.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Explore : Screen("explore")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; navController.navigate(Screen.Feed.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; navController.navigate(Screen.Explore.route) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.Feed.route, Modifier.padding(padding)) {
            composable(Screen.Feed.route) { FeedScreen(onOpenComments = {}) }
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Explore") }) }) { padding ->
        Text("Coming Soon", modifier = Modifier.padding(padding))
    }
}
