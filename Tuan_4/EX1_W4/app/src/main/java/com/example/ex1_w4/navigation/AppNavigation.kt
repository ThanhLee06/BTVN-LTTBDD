package com.example.ex1_w4.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ex1_w4.model.OnboardingRepository
import com.example.ex1_w4.screens.OnboardingStepScreen
import com.example.ex1_w4.screens.StartScreen

interface IAppNavigator {
    fun navigateToStep(stepIndex: Int)
    fun navigateToStart()
    fun skipToEnd()
}

class AppNavigator(private val navController: NavController) : IAppNavigator {
    override fun navigateToStep(stepIndex: Int) {
        navController.navigate("onboarding/$stepIndex")
    }

    override fun navigateToStart() {
        navController.navigate("start") {
            popUpTo("start") { inclusive = true }
        }
    }

    override fun skipToEnd() {
        navController.navigate("onboarding/2")
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    repository: OnboardingRepository = remember { OnboardingRepository() }
) {
    val navigator = remember(navController) { AppNavigator(navController) }
    val totalSteps = repository.getTotalSteps()
    val metaData = repository.getMetaData()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        composable("start") {
            StartScreen(
                metaData = metaData,
                onNavigateNext = { navigator.navigateToStep(0) }
            )
        }
        composable(
            route = "onboarding/{stepIndex}",
            arguments = listOf(
                navArgument("stepIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val stepIndex = backStackEntry.arguments?.getInt("stepIndex") ?: 0
            val step = repository.getStep(stepIndex)

            OnboardingStepScreen(
                appName = metaData.appName,
                step = step,
                totalSteps = totalSteps,
                onNext = {
                    if (stepIndex < totalSteps - 1) {
                        navigator.navigateToStep(stepIndex + 1)
                    } else {
                        navigator.navigateToStart()
                    }
                },
                onSkip = {
                    if (stepIndex < totalSteps - 1) {
                        navigator.skipToEnd()
                    } else {
                        navigator.navigateToStart()
                    }
                }
            )
        }
    }
}
