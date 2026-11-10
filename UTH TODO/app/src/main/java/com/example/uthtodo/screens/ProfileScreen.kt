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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.uthtodo.data.TaskGraph
import com.example.uthtodo.data.TaskRules
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.auth.AuthManager
import com.example.uthtodo.auth.GoogleSignInHelper
import coil.compose.AsyncImage
import com.example.uthtodo.components.GoogleLogoIcon
import com.example.uthtodo.model.TodoRepository
import com.example.uthtodo.ui.theme.PrimaryBlue
import com.example.uthtodo.ui.theme.TextDescription
import com.example.uthtodo.ui.theme.TextTitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val user = TodoRepository.currentUser.value
    val taskState by TaskGraph.repository.state.collectAsState()
    val weeklyStats = TaskRules.weeklyStats(taskState.tasks, TaskGraph.clock.today())

    val completedCount = taskState.tasks.count { it.isCompleted }
    val totalCount = taskState.tasks.size
    val pendingCount = totalCount - completedCount
    val rate = TaskRules.percent(taskState.tasks)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Bar (< Cài đặt       Hồ sơ)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoading) { onNavigateBack() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Quay lại",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cài đặt",
                        fontSize = 16.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Hồ sơ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar tròn lớn (như hình mẫu MA xanh nhạt mềm mại)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.initial,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Ảnh đại diện của ${user.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên người dùng
            Text(
                text = user.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Email
            Text(
                text = if (user.isGoogleLinked) {
                    user.email ?: "Đã liên kết tài khoản Google"
                } else {
                    "Tài khoản khách"
                },
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Badge / Nút Liên kết Google
            if (!user.isGoogleLinked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .clickable(enabled = !isLoading) {
                            isLoading = true
                            scope.launch {
                                try {
                                    GoogleSignInHelper.signIn(context, linkAnonymous = true)
                                    Toast.makeText(context, "Liên kết Google thành công!", Toast.LENGTH_SHORT).show()
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    Toast.makeText(context, AuthManager.errorMessage(error), Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            GoogleLogoIcon(size = 16)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liên kết Google",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đã liên kết tài khoản Google",
                        fontSize = 12.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Thống kê 3 cột (Việc đã xong | Đang chờ | Hoàn thành) bo tròn 22.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    number = completedCount.toString(),
                    label = "Việc đã xong",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    number = pendingCount.toString(),
                    label = "Đang chờ",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    number = "$rate%",
                    label = "Hoàn thành",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card Biểu đồ Tuần này (Bo cong lớn 24.dp, thanh bo tròn mềm)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x10000000),
                        ambientColor = Color(0x06000000)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tuần này",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Việc xong mỗi ngày",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Biểu đồ cột bo tròn đầu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyStats.forEach { stat ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                val barHeight = (stat.completedCount * 65f / stat.maxCount.coerceAtLeast(1)).dp.coerceAtLeast(6.dp)
                                val barColor = when {
                                    stat.isToday -> PrimaryBlue
                                    stat.completedCount > 0 -> Color(0xFFA5B4FC)
                                    else -> Color(0xFFE2E8F0)
                                }

                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(barColor)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = stat.dayLabel,
                                    fontSize = 12.sp,
                                    color = if (stat.isToday) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Nút Đăng xuất dạng viên thuốc bo tròn lớn 26.dp chuẩn y hình
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x0C000000)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .clickable(enabled = !isLoading) {
                        isLoading = true
                        scope.launch {
                            try {
                                AuthManager.signOut(context)
                                Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                                onLogoutSuccess()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                Toast.makeText(context, "Không thể đăng xuất. Vui lòng thử lại.", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Đăng xuất",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng xuất",
                        color = Color(0xFF991B1B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x10000000),
                ambientColor = Color(0x06000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(vertical = 18.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

