package com.example.ex1_w3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Text Detail") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("The quick ")
                    withStyle(style = SpanStyle(color = Color(0xFFA52A2A), fontWeight = FontWeight.Bold)) { append("Brown") }
                    append(" fox jumps ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) { append("over") }
                    append(" the ")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic)) { append("lazy") }
                    append(" dog.")
                },
                fontSize = 24.sp,
                lineHeight = 36.sp
            )
        }
    }
}