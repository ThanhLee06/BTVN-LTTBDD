package com.example.uthtodo.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.auth.AuthManager
import com.example.uthtodo.auth.GoogleSignInHelper
import com.example.uthtodo.components.AppLogoIcon
import com.example.uthtodo.components.GoogleLogoIcon
import com.example.uthtodo.ui.theme.PrimaryBlue
import com.example.uthtodo.ui.theme.TextDescription
import com.example.uthtodo.ui.theme.TextTitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun signIn(withGoogle: Boolean) {
        if (isLoading) return
        isLoading = true
        scope.launch {
            try {
                val user = if (withGoogle) GoogleSignInHelper.signIn(context)
                    else AuthManager.signInAnonymously()
                val message = if (user.isAnonymous) "Đăng nhập khách thành công!"
                    else "Chào mừng ${user.displayName ?: "bạn"}!"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Toast.makeText(context, AuthManager.errorMessage(error), Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Main Middle Branding Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            AppLogoIcon(isLarge = true)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Chào mừng đến với",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitle,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "UTH Todo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitle,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Đăng nhập để lưu công việc và đồng bộ trên mọi thiết bị của bạn.",
                fontSize = 15.sp,
                color = TextDescription,
                lineHeight = 22.sp
            )
        }

        // Bottom Actions Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nút "Tiếp tục với Google"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLoading) {
                        signIn(withGoogle = true)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        GoogleLogoIcon(size = 20)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tiếp tục với Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phân cách "hoặc"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0),
                    thickness = 1.dp
                )
                Text(
                    text = "hoặc",
                    modifier = Modifier.padding(horizontal = 14.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE2E8F0),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nút "Dùng thử không cần tài khoản" (Anonymous Auth)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isLoading) {
                        signIn(withGoogle = false)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dùng thử không cần tài khoản",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Điều khoản sử dụng
            Text(
                text = "Khi tiếp tục, bạn đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư của UTH Todo.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

