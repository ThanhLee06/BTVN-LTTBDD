package com.example.ex_w4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ex_w4.ui.screens.BookListScreen
import com.example.ex_w4.ui.screens.ManagementScreen
import com.example.ex_w4.ui.screens.StudentListScreen
import com.example.ex_w4.ui.theme.EX_W4Theme

sealed class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Management : NavDestination("management", "Quản lý", Icons.Filled.Home)
    object Books : NavDestination("books", "DS Sách", Icons.Filled.Description)
    object Students : NavDestination("students", "Sinh viên", Icons.Filled.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EX_W4Theme {
                LibraryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryApp() {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Management) }

    val destinations = listOf(
        NavDestination.Management,
        NavDestination.Books,
        NavDestination.Students
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFDCDCDC))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.forEach { dest ->
                        val selected = currentDestination == dest
                        val tintColor = if (selected) Color(0xFF4D6BB4) else Color(0xFFC4C7CC)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { currentDestination = dest },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label,
                                tint = tintColor,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dest.label,
                                fontSize = 17.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = tintColor
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.Management -> ManagementScreen()
                NavDestination.Books -> BookListScreen()
                NavDestination.Students -> StudentListScreen()
            }
        }
    }
}