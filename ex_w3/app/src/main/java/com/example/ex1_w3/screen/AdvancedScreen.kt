package com.example.ex1_w3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedScreen(navController: NavController) {
    var isChecked by remember { mutableStateOf(false) }
    var isSwitched by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0.5f) }
    var selectedRadio by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Tự Tìm Hiểu") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
                Text("Checkbox (Đã chọn: $isChecked)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isSwitched, onCheckedChange = { isSwitched = it })
                Text("Switch (Bật/Tắt: $isSwitched)")
            }
            Text("Radio Button:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedRadio == 1, onClick = { selectedRadio = 1 })
                Text("Lựa chọn 1")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedRadio == 2, onClick = { selectedRadio = 2 })
                Text("Lựa chọn 2")
            }
            Text("Slider (Giá trị: ${String.format("%.2f", sliderValue)})")
            Slider(value = sliderValue, onValueChange = { sliderValue = it })

            HorizontalDivider()

            Text("Card Component:", fontWeight = FontWeight.Bold)
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Đây là một Card có đổ bóng.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}