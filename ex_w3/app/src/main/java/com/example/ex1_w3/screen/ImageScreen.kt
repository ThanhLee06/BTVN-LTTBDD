package com.example.ex1_w3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Images") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/University_of_Transport_Ho_Chi_Minh_City_logo.png/800px-University_of_Transport_Ho_Chi_Minh_City_logo.png",
                contentDescription = "Logo UTH",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "https://diaonline.vanthinhphat.edu.vn/...",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            AsyncImage(
                model = "https://diaonline.vanthinhphat.edu.vn/wp-content/uploads/2023/01/Logo-GTVT.png",
                contentDescription = "Trường UTH",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "In app", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}