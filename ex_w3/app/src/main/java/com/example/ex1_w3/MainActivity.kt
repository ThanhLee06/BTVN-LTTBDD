package com.example.ex1_w3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ex1_w3.screens.*
import com.example.ex1_w3.ui.theme.Ex1_w3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ex1_w3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("list") { ListScreen(navController) }
                        composable("text_detail") { TextDetailScreen(navController) }
                        composable("image_detail") { ImageScreen(navController) }
                        composable("textfield_detail") { TextFieldScreen(navController) }
                        composable("row_detail") { RowLayoutScreen(navController) }
                        composable("advanced_detail") { AdvancedScreen(navController) }
                    }
                }
            }
        }
    }
}