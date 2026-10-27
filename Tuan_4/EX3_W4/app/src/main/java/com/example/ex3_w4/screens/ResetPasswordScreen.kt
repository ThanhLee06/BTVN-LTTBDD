package com.example.ex3_w4.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ex3_w4.components.*

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String,
    otp: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Back button on top left
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            BackButton(onClick = { navController.popBackStack() })
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header Section
        HeaderSection()

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "Create new password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "Your new password must be different form\npreviously used password",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Password field
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            leadingIcon = Icons.Outlined.Lock,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Toggle password visibility",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isPasswordVisible = !isPasswordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password field
        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirm Password",
            leadingIcon = Icons.Outlined.Lock,
            visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = if (isConfirmVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Toggle confirm password visibility",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isConfirmVisible = !isConfirmVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Next Button
        PrimaryButton(
            text = "Next",
            onClick = {
                val passToSend = password.ifEmpty { "12345678" }
                val encodedEmail = Uri.encode(email)
                val encodedOtp = Uri.encode(otp)
                val encodedPass = Uri.encode(passToSend)
                navController.navigate("confirm/$encodedEmail/$encodedOtp/$encodedPass")
            }
        )
    }
}
