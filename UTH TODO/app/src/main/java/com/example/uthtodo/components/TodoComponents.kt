package com.example.uthtodo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.model.TodoTask

object TaskColors {
    val Blue = Color(0xFF495DE1)
    val Background = Color(0xFFF5F5F7)
    val Ink = Color(0xFF1C1C1E)
    val Grey = Color(0xFF6B6B70)
    val Line = Color(0xFFE9E9EE)
    val StudyBg = Color(0xFFE8EDFD)
    val LifeBg = Color(0xFFF9ECDB)
    val LifeText = Color(0xFF914F1F)
    val Red = Color(0xFFAC3E2B)
}

@Composable
fun TaskStyle(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = TaskColors.Blue,
        onSurface = TaskColors.Ink, onBackground = TaskColors.Ink)) {
        ProvideTextStyle(TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp,
            lineHeight = 20.sp, letterSpacing = 0.sp), content)
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier, size: Int = 20) {
    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Text("G", fontSize = (size * 0.9).sp, color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CircularProgressRing(percentage: Int, modifier: Modifier = Modifier, size: Int = 72) {
    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 7.dp.toPx()
            drawCircle(Color.White.copy(alpha = .28f), style = Stroke(stroke))
            drawArc(Color.White, -90f, percentage.coerceIn(0, 100) * 3.6f, false,
                style = Stroke(stroke, cap = StrokeCap.Butt))
        }
        Text("$percentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun NotificationBellButton(onClick: () -> Unit, modifier: Modifier = Modifier, hasUnread: Boolean = true) {
    Box(modifier.size(44.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center) {
        Icon(TaskIcons.Bell, "Thông báo", tint = TaskColors.Ink, modifier = Modifier.size(23.dp))
        if (hasUnread) Box(Modifier.offset(5.dp, (-6).dp).size(8.dp).background(Color(0xFFD25757), RoundedCornerShape(2.dp)))
    }
}

@Composable
fun TaskBadge(text: String, background: Color, foreground: Color, modifier: Modifier = Modifier) {
    Text(text, modifier.clip(RoundedCornerShape(5.dp)).background(background).padding(horizontal = 7.dp, vertical = 2.dp),
        color = foreground, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun CategoryBadge(category: String) {
    TaskBadge(category, if (category == "Học tập") TaskColors.StudyBg else TaskColors.LifeBg,
        if (category == "Học tập") Color(0xFF3C51C9) else TaskColors.LifeText)
}

@Composable
fun TaskCheck(checked: Boolean, onToggle: () -> Unit, label: String, enabled: Boolean = true, square: Boolean = false) {
    Box(Modifier.size(40.dp).semantics { contentDescription = label }
        .toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() }),
        contentAlignment = Alignment.Center) {
        val shape = if (square) RoundedCornerShape(5.dp) else CircleShape
        Box(Modifier.size(if (square) 24.dp else 26.dp).clip(shape)
            .background(if (checked) TaskColors.Blue else Color.Transparent)
            .border(if (checked) 0.dp else 2.dp, if (checked) Color.Transparent else Color(0xFFCDCDD2), shape),
            contentAlignment = Alignment.Center) {
            if (checked) Icon(TaskIcons.Check, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
fun TaskItemRow(task: TodoTask, onToggle: () -> Unit, onOpen: () -> Unit,
    modifier: Modifier = Modifier, listMode: Boolean = false, clock: TaskClock = TaskClock(), busy: Boolean = false) {
    val compact = listMode && task.isCompleted
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White)
        .clickable(onClick = onOpen).heightIn(min = if (compact) 60.dp else 66.dp)
        .padding(start = 10.dp, end = 12.dp, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        TaskCheck(task.isCompleted, onToggle, "Hoàn thành ${task.title}", enabled = !busy)
        Spacer(Modifier.width(7.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium,
                color = if (task.isCompleted) Color(0xFF8A8A8E) else TaskColors.Ink,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!compact) {
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    CategoryBadge(task.category)
                    Text(if (listMode) "${clock.dueLabel(task.date)}, ${task.time}" else task.time,
                        fontSize = 12.sp, color = TaskColors.Grey, maxLines = 1)
                }
            }
        }
        if (listMode && !task.isCompleted && task.priority == "high") {
            Spacer(Modifier.width(5.dp))
            TaskBadge("Gấp", Color(0xFFF8E9E7), TaskColors.Red)
        }
        if (!listMode) Icon(TaskIcons.ChevronRight, null,
            tint = Color(0xFFB6B6BB), modifier = Modifier.size(21.dp))
    }
}

@Composable
fun TodoBottomBar(currentTab: String, onTabSelected: (String) -> Unit, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(60.dp)) {
        Column(Modifier.fillMaxSize().background(Color.White)) {
            HorizontalDivider(color = TaskColors.Line, thickness = 1.dp)
            Row(Modifier.fillMaxSize().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                BottomBarItem("Trang chủ", TaskIcons.Home, currentTab == "home", Modifier.weight(1f)) { onTabSelected("home") }
                BottomBarItem("Danh sách", TaskIcons.List, currentTab == "list", Modifier.weight(1f)) { onTabSelected("list") }
                Spacer(Modifier.weight(1f))
                BottomBarItem("Thông báo", TaskIcons.Bell, currentTab == "notifications", Modifier.weight(1f)) { onTabSelected("notifications") }
                BottomBarItem("Cài đặt", TaskIcons.Settings, currentTab == "settings", Modifier.weight(1f)) { onTabSelected("settings") }
            }
        }
        Box(Modifier.align(Alignment.TopCenter).offset(y = (-14).dp).size(56.dp)
            .shadow(9.dp, CircleShape).clip(CircleShape).background(TaskColors.Blue).clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center) {
            Icon(TaskIcons.Add, "Thêm công việc", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun BottomBarItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) TaskColors.Blue else TaskColors.Grey
    Column(modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = color, fontSize = 11.sp, lineHeight = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}
