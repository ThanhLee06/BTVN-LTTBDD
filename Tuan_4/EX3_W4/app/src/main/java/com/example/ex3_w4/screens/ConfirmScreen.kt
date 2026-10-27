package com.example.ex3_w4.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
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
fun ConfirmScreen(
    navController: NavController,
    email: String,
    otp: String,
    password: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPassVisible by remember { mutableStateOf(false) }

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
            text = "Confirm",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "We are here to help you!",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Field 1: Email (displaying email passed from Screen 1)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGrayBackground)
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = email.ifEmpty { "uth@gmail.com" },
                    fontSize = 15.sp,
                    color = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Field 2: OTP (displaying OTP passed from Screen 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGrayBackground)
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Pin,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = otp.ifEmpty { "123456" },
                    fontSize = 15.sp,
                    color = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Field 3: Password (displaying password passed from Screen 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGrayBackground)
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isPassVisible) password.ifEmpty { "12345678" } else "•".repeat(password.ifEmpty { "12345678" }.length),
                    fontSize = 15.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Toggle password visibility",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isPassVisible = !isPassVisible }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button: returns data to Screen 1 (Nav Data Flow)
        PrimaryButton(
            text = "Summit",
            onClick = {
                // Pass data back to Screen 1 ("forgot")
                try {
                    navController.getBackStackEntry("forgot").savedStateHandle.apply {
                        set("returned_email", email.ifEmpty { "uth@gmail.com" })
                        set("returned_otp", otp.ifEmpty { "123456" })
                        set("returned_pass", password.ifEmpty { "12345678" })
                    }
                } catch (e: Exception) {
                    // Fallback to previousBackStackEntry if any
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("returned_email", email.ifEmpty { "uth@gmail.com" })
                        set("returned_otp", otp.ifEmpty { "123456" })
                        set("returned_pass", password.ifEmpty { "12345678" })
                    }
                }

                Toast.makeText(
                    context,
                    "Đã truyền dữ liệu email, otp, pass về Màn hình 1!",
                    Toast.LENGTH_SHORT
                ).show()

                // Pop back to Screen 1
                navController.popBackStack("forgot", inclusive = false)
            }
        )
    }
}
