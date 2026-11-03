package com.example.uthtodo

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.uthtodo.navigation.AppNavigation
import com.example.uthtodo.ui.theme.EX1_W4Theme
import com.example.uthtodo.services.NotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationService.handleIntent(intent)
        setContent {
            EX1_W4Theme {
                AppNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificationService.handleIntent(intent)
    }
}
