package com.schoolerz.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.schoolerz.domain.model.Post
import com.schoolerz.presentation.feed.FeedScreen
import com.schoolerz.presentation.feed.PostDetailScreen
import com.schoolerz.presentation.profile.ProfileScreen
import com.schoolerz.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
}

// Temporary storage for post navigation (in production, use a shared ViewModel or repository)
private var selectedPostForNavigation: Post? = null

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
                    onClick = { selectedTab = 1; navController.navigate(Screen.Profile.route) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
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
            composable(Screen.Feed.route) {
                FeedScreen(
                    onOpenPost = { post ->
                        selectedPostForNavigation = post
                        navController.navigate(Screen.PostDetail.createRoute(post.id))
                    }
                )
            }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) {
                selectedPostForNavigation?.let { post ->
                    PostDetailScreen(
                        post = post,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
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
