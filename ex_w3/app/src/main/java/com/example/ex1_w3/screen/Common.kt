package com.example.ex1_w3.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
val TitleBlue = Color(0xFF2196F3)
val CardBlue = Color(0xFFBBDEFB)
val CardPink = Color(0xFFF8B4B4)
val BrownColor = Color(0xFFC77D22)
val LightBlueBox = Color(0xFFA3BFF0)
val DarkBlueBox = Color(0xFF5588EE)
val RowBackground = Color(0xFFF2F3F5)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(title: String, navController: NavHostController) {
    CenterAlignedTopAppBar(
        title = { Text(title, color = TitleBlue, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("<", fontSize = 24.sp, color = TitleBlue)
            }
        }
    )
}
@Composable
fun ColorBox(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(72.dp)
            .background(color, RoundedCornerShape(12.dp))
    )
}