package com.example.uthtodo.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.components.CircularProgressRing
import com.example.uthtodo.components.NotificationBellButton
import com.example.uthtodo.components.TaskItemRow
import com.example.uthtodo.components.TodoBottomBar
import com.example.uthtodo.components.AdMobBanner
import com.example.uthtodo.components.AdService
import com.example.uthtodo.services.RemoteConfigService
import com.example.uthtodo.model.TodoTask
import androidx.compose.material3.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.uthtodo.model.TodoRepository
import com.example.uthtodo.ui.theme.PrimaryBlue
import com.example.uthtodo.ui.theme.TextDescription
import com.example.uthtodo.ui.theme.TextTitle
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user = TodoRepository.currentUser.value
    val tasks = TodoRepository.tasks

    val completedCount = TodoRepository.getCompletedCount()
    val totalCount = TodoRepository.getTotalCount()
    val progressPercent = TodoRepository.getProgressPercent()

    val studyRemaining = TodoRepository.getCategoryRemainingCount("Học tập")
    val lifeRemaining = TodoRepository.getCategoryRemainingCount("Cuộc sống")

    var selectedTab by remember { mutableStateOf("home") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoTask?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main Scrollable Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Header Bar (Avatar + Greeting + Bell)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar (Click to go to Profile)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF))
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.initial,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Mở hồ sơ của ${user.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = SimpleDateFormat("EEEE, dd 'tháng' MM", Locale.forLanguageTag("vi-VN")).format(Date()),
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chào ${if (user.isAnonymous) "bạn!" else "${user.name}!"}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        NotificationBellButton(
                            onClick = onNavigateToNotifications
                        )
                    }
                }

                // Banner Tiến độ hôm nay (Bo tròn lớn 26.dp chuẩn reference)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(PrimaryBlue)
                            .padding(horizontal = 24.dp, vertical = 22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tiến độ hôm nay",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$completedCount/$totalCount việc",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (progressPercent >= 100) {
                                        "Tuyệt vời, bạn đã hoàn thành hết!"
                                    } else {
                                        RemoteConfigService.banner.value
                                    },
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            CircularProgressRing(
                                percentage = progressPercent,
                                size = 80
                            )
                        }
                    }
                }

                // Hai thẻ Danh mục (Học tập & Cuộc sống)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Thẻ Học tập
                        CategoryCard(
                            title = "Học tập",
                            subtitle = "$studyRemaining việc còn lại",
                            iconColor = Color(0xFF4362E5),
                            iconBgColor = Color(0xFFEEF2FF),
                            icon = Icons.Default.School,
                            modifier = Modifier.weight(1f)
                        )

                        // Thẻ Cuộc sống
                        CategoryCard(
                            title = "Cuộc sống",
                            subtitle = "$lifeRemaining việc còn lại",
                            iconColor = Color(0xFFD97706),
                            iconBgColor = Color(0xFFFEF3C7),
                            icon = Icons.Default.Home,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Tiêu đề "Việc hôm nay" + "Xem tất cả"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Việc hôm nay",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "Xem tất cả",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { TodoRepository.retrySync() }
                        )
                    }
                }

                item {
                    if (TodoRepository.isLoading.value) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    TodoRepository.syncError.value?.let { error ->
                        Text(error, color = Color(0xFFB91C1C), modifier = Modifier.clickable { TodoRepository.retrySync() })
                    }
                    if (TodoRepository.hasPendingWrites.value) {
                        Text("Thay đổi đang chờ đồng bộ", color = TextDescription, fontSize = 12.sp)
                    } else if (TodoRepository.isFromCache.value) {
                        Text("Đang hiển thị dữ liệu đã lưu trên thiết bị", color = TextDescription, fontSize = 12.sp)
                    }
                    if (tasks.isEmpty() && !TodoRepository.isLoading.value) {
                        Text("Chưa có công việc. Nhấn + để thêm việc đầu tiên.", color = TextDescription)
                    }
                }

                // Firestore updates this observable list, including empty snapshots.
                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = {
                            TodoRepository.toggleTask(task.id)
                        },
                        onEdit = { editingTask = task }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (RemoteConfigService.showAds.value && AdService.ready.value) AdMobBanner()

            // Thanh điều hướng đáy (Bottom Bar)
            TodoBottomBar(
                currentTab = selectedTab,
                onTabSelected = {
                    when (it) {
                        "settings" -> onNavigateToSettings()
                        "notifications" -> onNavigateToNotifications()
                        else -> selectedTab = it
                    }
                },
                onAddClick = { showAddTaskDialog = true }
            )
        }

        // Dialog tạo công việc trong Firestore.
        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, category, time ->
                    if (TodoRepository.addTask(title, category, time)) showAddTaskDialog = false
                }
            )
        }
        editingTask?.let { task ->
            AddTaskDialog(
                initialTask = task,
                onDismiss = { editingTask = null },
                onDelete = { TodoRepository.deleteTask(task.id); editingTask = null },
                onAdd = { title, category, time ->
                    if (TodoRepository.editTask(task, title, category, time)) editingTask = null
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    iconBgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x10000000),
                ambientColor = Color(0x06000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitle
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextDescription
            )
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    initialTask: TodoTask? = null,
    onDelete: () -> Unit = {},
    onAdd: (title: String, category: String, time: String) -> Unit
) {
    var title by remember(initialTask?.id) { mutableStateOf(initialTask?.title ?: "") }
    var category by remember(initialTask?.id) { mutableStateOf(initialTask?.category ?: "Học tập") }
    var time by remember(initialTask?.id) { mutableStateOf(initialTask?.time ?: "15:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (initialTask == null) "Thêm công việc mới" else "Sửa công việc", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(200) },
                    label = { Text("Tên công việc") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Thời gian (VD: 08:30)") },
                    isError = !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time),
                    supportingText = { Text("Nhập giờ từ 00:00 đến 23:59") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Danh mục:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category == "Học tập",
                        onClick = { category = "Học tập" }
                    )
                    Text("Học tập", modifier = Modifier.clickable { category = "Học tập" })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = category == "Cuộc sống",
                        onClick = { category = "Cuộc sống" }
                    )
                    Text("Cuộc sống", modifier = Modifier.clickable { category = "Cuộc sống" })
                }
                if (initialTask != null) {
                    TextButton(onClick = onDelete) { Text("Xóa công việc", color = Color(0xFFB91C1C)) }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = TodoRepository.isValidTask(title, category, time),
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, category, time)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (initialTask == null) "Thêm việc" else "Lưu thay đổi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF64748B))
            }
        }
    )
}

