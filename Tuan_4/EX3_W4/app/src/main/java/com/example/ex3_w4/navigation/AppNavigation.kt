package com.example.ex3_w4.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ex3_w4.screens.ConfirmScreen
import com.example.ex3_w4.screens.ForgotPasswordScreen
import com.example.ex3_w4.screens.ResetPasswordScreen
import com.example.ex3_w4.screens.VerificationScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "forgot",
        modifier = modifier
    ) {
        // Màn hình 1: Nav (Data Flow) - Forget Password
        composable("forgot") {
            ForgotPasswordScreen(navController = navController)
        }

        // Màn hình 2: Verification
        composable(
            route = "verify/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = "uth@gmail.com"
                }
            )
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: "uth@gmail.com"
            val email = Uri.decode(rawEmail)
            VerificationScreen(
                navController = navController,
                email = email
            )
        }

        // Màn hình 3: Reset Password
        composable(
            route = "reset/{email}/{otp}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = "uth@gmail.com"
                },
                navArgument("otp") {
                    type = NavType.StringType
                    defaultValue = "12345"
                }
            )
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: "uth@gmail.com"
            val rawOtp = backStackEntry.arguments?.getString("otp") ?: "12345"
            val email = Uri.decode(rawEmail)
            val otp = Uri.decode(rawOtp)

            ResetPasswordScreen(
                navController = navController,
                email = email,
                otp = otp
            )
        }

        // Màn hình 4: Confirm
        composable(
            route = "confirm/{email}/{otp}/{password}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = "uth@gmail.com"
                },
                navArgument("otp") {
                    type = NavType.StringType
                    defaultValue = "12345"
                },
                navArgument("password") {
                    type = NavType.StringType
                    defaultValue = "12345678"
                }
            )
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: "uth@gmail.com"
            val rawOtp = backStackEntry.arguments?.getString("otp") ?: "12345"
            val rawPassword = backStackEntry.arguments?.getString("password") ?: "12345678"
            val email = Uri.decode(rawEmail)
            val otp = Uri.decode(rawOtp)
            val password = Uri.decode(rawPassword)

            ConfirmScreen(
                navController = navController,
                email = email,
                otp = otp,
                password = password
            )
        }
    }
}
