package com.example.ex1_w3
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ex1_w3.screen.ColumnLayoutScreen
import com.example.ex1_w3.screen.ImageScreen
import com.example.ex1_w3.screen.PasswordScreen
import com.example.ex1_w3.screen.RowLayoutScreen
import com.example.ex1_w3.screen.TextDetailScreen
import com.example.ex1_w3.screen.TextFieldScreen
import com.example.ex1_w3.screen.ListScreen
import com.example.ex1_w3.screen.HomeScreen
import com.example.ex1_w3.ui.theme.Ex1_w3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ex1_w3Theme {
                AppNavigation()
            }
        }
    }
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { HomeScreen(navController) }
        composable("list") { ListScreen(navController) }
        composable("text_detail") { TextDetailScreen(navController) }
        composable("image_detail") { ImageScreen(navController) }
        composable("textfield_detail") { TextFieldScreen(navController) }
        composable("password_detail") { PasswordScreen(navController) }
        composable("row_detail") { RowLayoutScreen(navController) }
        composable("column_detail") { ColumnLayoutScreen(navController) }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    Ex1_w3Theme {
        AppNavigation()
    }
}