package com.example.calculate_ui
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorScreen() }
    }
}
@Composable
fun CalculatorScreen() {
    var inputOne by remember { mutableStateOf("") }
    var inputTwo by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Kết quả: ") }
    fun formatResult(value: Double): String {
        return if (value % 1 == 0.0) value.toInt().toString() else value.toString()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(text = "Thực hành 03", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(30.dp))
        OutlinedTextField(
            value = inputOne,
            onValueChange = { inputOne = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val num1 = inputOne.toDoubleOrNull() ?: 0.0
                    val num2 = inputTwo.toDoubleOrNull() ?: 0.0
                    resultText = "Kết quả: ${formatResult(num1 + num2)}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("+", color = Color.White, fontSize = 24.sp) }
            Button(
                onClick = {
                    val num1 = inputOne.toDoubleOrNull() ?: 0.0
                    val num2 = inputTwo.toDoubleOrNull() ?: 0.0
                    resultText = "Kết quả: ${formatResult(num1 - num2)}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDF9F35)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("-", color = Color.White, fontSize = 24.sp) }
            Button(
                onClick = {
                    val num1 = inputOne.toDoubleOrNull() ?: 0.0
                    val num2 = inputTwo.toDoubleOrNull() ?: 0.0
                    resultText = "Kết quả: ${formatResult(num1 * num2)}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("*", color = Color.White, fontSize = 24.sp) }
            Button(
                onClick = {
                    val num1 = inputOne.toDoubleOrNull() ?: 0.0
                    val num2 = inputTwo.toDoubleOrNull() ?: 0.0
                    resultText = if (num2 == 0.0) "Kết quả: Lỗi chia cho 0" else "Kết quả: ${formatResult(num1 / num2)}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("/", color = Color.White, fontSize = 24.sp) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = inputTwo,
            onValueChange = { inputTwo = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = resultText, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
    }
}