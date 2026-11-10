package com.example.uthtodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.components.*
import com.example.uthtodo.data.TaskRules
import com.example.uthtodo.data.TasksState
import com.example.uthtodo.model.TodoTask

@Composable
fun taskTopPadding(target: androidx.compose.ui.unit.Dp = 60.dp, minimum: androidx.compose.ui.unit.Dp = 8.dp): androidx.compose.ui.unit.Dp =
    (target - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(minimum)

@Composable
fun TaskPage(currentTab: String, onTab: (String) -> Unit, onAdd: () -> Unit,
    content: @Composable ColumnScope.() -> Unit) {
    TaskStyle {
        Column(Modifier.fillMaxSize().background(Color.White).navigationBarsPadding()) {
            Column(Modifier.weight(1f).background(TaskColors.Background).statusBarsPadding()) { content() }
            TodoBottomBar(currentTab, onTab, onAdd)
        }
    }
}

@Composable
fun TaskStatus(state: TasksState, onRetry: () -> Unit) {
    if (state.loading) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.size(24.dp), color = TaskColors.Blue, strokeWidth = 2.dp)
        }
    }
    state.error?.let { message ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(message, Modifier.weight(1f), color = TaskColors.Red, fontSize = 13.sp)
            TextButton(onClick = onRetry) { Text("Thử lại") }
        }
    }
}

@Composable
fun TaskEditorDialog(initialTask: TodoTask?, today: String, busy: Boolean, error: String? = null,
    onDismiss: () -> Unit, onSave: (TodoTask) -> Unit,
    requestReminderPermission: ((() -> Unit) -> Unit)? = null, reminderAvailable: Boolean = true,
    extrasContent: @Composable () -> Unit = {}) {
    var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title ?: "") }
    var note by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.note ?: "") }
    var category by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.category ?: "Học tập") }
    var date by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.date ?: today) }
    var time by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.time ?: "09:00") }
    var priority by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.priority ?: "medium") }
    var remind by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.remind ?: true) }
    val task = (initialTask ?: TodoTask("", "", category, time)).copy(
        title = title.trim(), note = note.trim(), category = category, date = date, time = time,
        priority = priority, remind = remind)
    TaskStyle {
        AlertDialog(onDismissRequest = { if (!busy) onDismiss() },
            title = { Text(if (initialTask == null) "Thêm công việc" else "Sửa công việc", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    error?.let { Text(it, color = TaskColors.Red, fontSize = 13.sp) }
                    OutlinedTextField(title, { title = it.take(200) }, label = { Text("Tên công việc") }, enabled = !busy)
                    OutlinedTextField(note, { note = it }, label = { Text("Ghi chú") }, enabled = !busy)
                    OutlinedTextField(date, { date = it }, label = { Text("Ngày (yyyy-MM-dd)") }, singleLine = true, enabled = !busy)
                    OutlinedTextField(time, { time = it }, label = { Text("Giờ (HH:mm)") }, singleLine = true, enabled = !busy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Học tập", "Cuộc sống").forEach { value ->
                            FilterChip(category == value, { category = value }, { Text(value, fontSize = 12.sp) }, enabled = !busy)
                        }
                    }
                    Text("Mức ưu tiên", fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("low" to "Thấp", "medium" to "Vừa", "high" to "Cao").forEach { (value, label) ->
                            FilterChip(priority == value, { priority = value }, { Text(label) }, enabled = !busy)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(remind, { enabled ->
                            if (enabled && requestReminderPermission != null) requestReminderPermission { remind = true }
                            else remind = enabled
                        }, enabled = !busy)
                        Text("Nhắc trước 15 phút", fontSize = 14.sp)
                    }
                    if (remind && !reminderAvailable) Text("Thông báo đang bị chặn. Hãy cấp quyền để lịch nhắc hoạt động.", fontSize = 12.sp)
                    extrasContent()
                }
            },
            confirmButton = {
                TextButton(onClick = { onSave(task) }, enabled = !busy && TaskRules.valid(task)) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Lưu")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Hủy") } }
        )
    }
}
