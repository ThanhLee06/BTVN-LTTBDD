package com.example.ex3_w4.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ex3_w4.components.*

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    // Listen for returned data from Screen 4 via savedStateHandle
    val currentBackStackEntry = navController.currentBackStackEntry
    val returnedEmail = currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("returned_email", null)?.collectAsState()?.value
    val returnedOtp = currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("returned_otp", null)?.collectAsState()?.value
    val returnedPass = currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("returned_pass", null)?.collectAsState()?.value

    val hasReturnedData = !returnedEmail.isNullOrEmpty() || !returnedOtp.isNullOrEmpty() || !returnedPass.isNullOrEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Top UTH and SmartTasks Header
        HeaderSection()

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "Forget Password?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "Enter your Email, we will send you a verification code.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Email TextField
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Your Email",
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Next Button
        PrimaryButton(
            text = "Next",
            onClick = {
                val inputEmail = if (email.trim().isEmpty()) "uth@gmail.com" else email.trim()
                val encodedEmail = Uri.encode(inputEmail)
                navController.navigate("verify/$encodedEmail")
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Bottom Box - Receives and displays data flow returned from Screen 4
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SoftBlueBackground)
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasReturnedData) {
                    Text(
                        text = "email: ${returnedEmail ?: ""}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "otp: ${returnedOtp ?: ""}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "pass: ${returnedPass ?: ""}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "(từ màn hình 4)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "email",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "otp",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "pass",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "(từ màn hình 4)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
