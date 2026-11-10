package com.example.uthtodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.uthtodo.components.*
import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.data.TaskRules
import com.example.uthtodo.data.TasksState
import com.example.uthtodo.model.UserProfile

@Composable
fun HomeScreen(state: TasksState, clock: TaskClock, user: UserProfile,
    onTab: (String) -> Unit, onAdd: () -> Unit, onTask: (String) -> Unit, onToggle: (String) -> Unit,
    onCategory: (String) -> Unit, onProfile: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val todayTasks = TaskRules.today(state.tasks, clock.today())
    val completed = todayTasks.count { it.isCompleted }
    val percent = TaskRules.percent(todayTasks)
    val topPadding = taskTopPadding()
    TaskPage("home", onTab, onAdd) {
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = topPadding, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(TaskColors.StudyBg).clickable(onClick = onProfile),
                        contentAlignment = Alignment.Center) {
                        Text(user.initial, color = TaskColors.Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        user.photoUrl?.let { AsyncImage(it, "Hồ sơ", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(clock.greetingDate(), fontSize = 14.sp, lineHeight = 18.sp, color = TaskColors.Grey)
                        Text(if (user.isAnonymous) "Chào bạn!" else "Chào ${user.name}!", fontSize = 25.sp,
                            lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                    }
                    NotificationBellButton({ onTab("notifications") })
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(TaskColors.Blue)
                    .padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tiến độ hôm nay", color = Color.White.copy(alpha = .85f), fontSize = 14.sp, lineHeight = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("$completed/${todayTasks.size} việc", fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(5.dp))
                        Text(when {
                            todayTasks.isEmpty() -> "Hôm nay chưa có công việc."
                            percent == 100 -> "Tuyệt vời, bạn đã hoàn thành hết!"
                            percent >= 50 -> "Đã hơn nửa chặng, cố lên!"
                            else -> "Từng bước một, bạn làm được!"
                        }, fontSize = 13.sp, lineHeight = 18.sp, color = Color.White.copy(alpha = .9f))
                    }
                    CircularProgressRing(percent)
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryCard("Học tập", state.tasks.count { !it.isCompleted && it.category == "Học tập" },
                        TaskIcons.School, Modifier.weight(1f)) { onCategory("Học tập") }
                    CategoryCard("Cuộc sống", state.tasks.count { !it.isCompleted && it.category == "Cuộc sống" },
                        TaskIcons.Home, Modifier.weight(1f)) { onCategory("Cuộc sống") }
                }
                Spacer(Modifier.height(27.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Việc hôm nay", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Xem tất cả", Modifier.clickable { onTab("list") }.padding(vertical = 6.dp),
                        color = TaskColors.Blue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(9.dp))
                TaskStatus(state, onRetry)
                if (todayTasks.isEmpty() && !state.loading && state.error == null) {
                    Text("Chưa có công việc hôm nay.", color = TaskColors.Grey, modifier = Modifier.padding(vertical = 16.dp))
                }
            }
            items(todayTasks, key = { it.id }) { task ->
                TaskItemRow(task, { onToggle(task.id) }, { onTask(task.id) }, busy = task.id in state.busy)
            }
        }
    }
}

@Composable
private fun CategoryCard(title: String, remaining: Int, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val study = title == "Học tập"
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).clickable(onClick = onClick).padding(16.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (study) TaskColors.StudyBg else TaskColors.LifeBg),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (study) TaskColors.Blue else TaskColors.LifeText, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        Text("$remaining việc còn lại", fontSize = 14.sp, color = TaskColors.Grey)
    }
}
