package com.example.ex1_w3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("TextField") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Thông tin nhập") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (inputText.isEmpty()) "Tự động cập nhật dữ liệu theo textfield" else "Bạn đã nhập: $inputText",
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}