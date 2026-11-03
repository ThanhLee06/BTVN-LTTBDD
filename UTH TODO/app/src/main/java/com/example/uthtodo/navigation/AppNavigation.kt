package com.example.uthtodo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uthtodo.auth.AuthManager
import com.example.uthtodo.model.OnboardingRepository
import com.example.uthtodo.screens.HomeScreen
import com.example.uthtodo.screens.LoginScreen
import com.example.uthtodo.screens.ProfileScreen
import com.example.uthtodo.screens.StartScreen
import com.example.uthtodo.screens.NotificationsScreen
import com.example.uthtodo.screens.SettingsScreen
import com.example.uthtodo.services.AnalyticsService
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.model.TodoRepository
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    repository: OnboardingRepository = remember { OnboardingRepository() }
) {
    val metaData = repository.getMetaData()

    val startDestination = remember {
        AuthManager.initAuthState()
        if (AuthManager.isUserLoggedIn()) "home" else "start"
    }
    val entry by navController.currentBackStackEntryAsState()
    val openInbox = NotificationService.openInbox.value
    LaunchedEffect(entry?.destination?.route) {
        entry?.destination?.route?.let { AnalyticsService.logScreen(it) }
    }
    LaunchedEffect(openInbox, entry?.destination?.route) {
        if (openInbox && TodoRepository.currentUser.value.uid.isNotBlank() && entry != null) {
            NotificationService.openInbox.value = false
            navController.navigate("notifications") { launchSingleTop = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Màn hình khởi đầu (StartScreen - Splash)
        composable("start") {
            StartScreen(
                metaData = metaData,
                onNavigateNext = {
                    navController.navigate("login") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }

        // 1. Màn hình Đăng nhập (LoginScreen)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. Màn hình Trang chủ (HomeScreen)
        composable("home") {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("notifications") { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() }, onProfile = { navController.navigate("profile") })
        }

        // 3. Màn hình Hồ sơ (ProfileScreen)
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
