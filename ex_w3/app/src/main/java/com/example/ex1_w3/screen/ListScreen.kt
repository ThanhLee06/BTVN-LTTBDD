package com.example.ex1_w3.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ListScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "UI Components List", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Display")
        ComponentItem(title = "Text", desc = "Displays text") { navController.navigate("text_detail") }
        ComponentItem(title = "Image", desc = "Displays an image") { navController.navigate("image_detail") }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Input")
        ComponentItem(title = "TextField", desc = "Input field for text") { navController.navigate("textfield_detail") }
        ComponentItem(title = "PasswordField", desc = "Input field for passwords") { /* Để trống hoặc làm thêm */ }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Layout")
        ComponentItem(title = "Column", desc = "Arranges elements vertically") { /* Để trống */ }
        ComponentItem(title = "Row", desc = "Arranges elements horizontally") { navController.navigate("row_detail") }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Tự tìm hiểu")
        ComponentItem(
            title = "Advanced Components",
            desc = "Checkbox, Radio, Slider, Switch...",
            color = Color(0xFFFFCDD2)
        ) {
            navController.navigate("advanced_detail")
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun ComponentItem(title: String, desc: String, color: Color = Color(0xFFE3F2FD), onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(text = desc, fontSize = 12.sp, color = Color.Gray)
        }
    }
}