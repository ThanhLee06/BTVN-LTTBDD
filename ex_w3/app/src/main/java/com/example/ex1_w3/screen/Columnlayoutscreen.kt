package com.example.ex1_w3.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
@Composable
fun ColumnLayoutScreen(navController: NavHostController) {
    Scaffold(
        topBar = { DetailTopBar("Column Layout", navController) }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(RowBackground, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorBox(LightBlueBox, Modifier.fillMaxWidth())
                    ColorBox(DarkBlueBox, Modifier.fillMaxWidth())
                    ColorBox(LightBlueBox, Modifier.fillMaxWidth())
                    ColorBox(DarkBlueBox, Modifier.fillMaxWidth())
                }
            }
        }
    }
}