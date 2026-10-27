package com.example.ex3_w4.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ex3_w4.components.*

@Composable
fun VerificationScreen(
    navController: NavController,
    email: String,
    modifier: Modifier = Modifier
) {
    // 5-digit OTP state
    val otpLength = 5
    var otpValues by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }

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
            text = "Verify Code",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "Enter the the code\nwe just sent you on your registered Email",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP 5 boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            for (i in 0 until otpLength) {
                val digit = otpValues[i]
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGrayBackground)
                        .border(
                            width = 1.dp,
                            color = if (digit.isNotEmpty()) BrandBlue else BorderGray,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                val updated = otpValues.toMutableList()
                                updated[i] = newValue
                                otpValues = updated
                                if (newValue.isNotEmpty() && i < otpLength - 1) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            } else if (newValue.length > 1) {
                                // Handle paste or fast typing
                                val chars = newValue.take(otpLength - i)
                                val updated = otpValues.toMutableList()
                                for (k in chars.indices) {
                                    if (i + k < otpLength) {
                                        updated[i + k] = chars[k].toString()
                                    }
                                }
                                otpValues = updated
                                val nextIndex = (i + chars.length).coerceAtMost(otpLength - 1)
                                focusRequesters[nextIndex].requestFocus()
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight(Alignment.CenterVertically)
                            .focusRequester(focusRequesters[i])
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Next Button
        PrimaryButton(
            text = "Next",
            onClick = {
                val enteredOtp = otpValues.joinToString("").ifEmpty { "12345" }
                val encodedEmail = Uri.encode(email)
                val encodedOtp = Uri.encode(enteredOtp)
                navController.navigate("reset/$encodedEmail/$encodedOtp")
            }
        )
    }
}
