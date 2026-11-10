package com.example.uthtodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.components.*
import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.data.TasksState
import com.example.uthtodo.model.TodoSubtask
import com.example.uthtodo.model.TodoTask

@Composable
fun TaskDetailScreen(id: String, source: String, state: TasksState, clock: TaskClock,
    onBack: () -> Unit, onEdit: (TodoTask) -> Unit, onRetry: () -> Unit,
    onSave: (TodoTask, () -> Unit) -> Unit, onDelete: () -> Unit,
    additionalContent: @Composable (TodoTask) -> Unit = {}) {
    val task = state.tasks.find { it.id == id }
    val busy = id in state.busy
    var newSubtask by rememberSaveable(id) { mutableStateOf("") }
    var editIndex by rememberSaveable(id) { mutableStateOf<Int?>(null) }
    var editTitle by rememberSaveable(id) { mutableStateOf("") }
    val topPadding = taskTopPadding(56.dp, 4.dp)
    TaskStyle {
        Column(Modifier.fillMaxSize().background(TaskColors.Background).statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(horizontal = 20.dp).padding(top = topPadding)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onBack).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(TaskIcons.ChevronLeft, null, tint = TaskColors.Blue, modifier = Modifier.size(19.dp))
                    Text(if (source == "list") "Danh sách" else "Trang chủ", color = TaskColors.Blue, fontSize = 16.sp)
                }
                Text("Sửa", Modifier.clip(RoundedCornerShape(6.dp)).clickable(enabled = task != null && !busy) { task?.let(onEdit) }
                    .padding(vertical = 10.dp, horizontal = 4.dp), color = TaskColors.Blue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            if (task == null) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (id in state.detailLoading) CircularProgressIndicator(color = TaskColors.Blue)
                    else {
                        Text(state.detailErrors[id] ?: "Không tìm thấy công việc.", color = TaskColors.Grey)
                        TextButton(onClick = onRetry) { Text("Thử lại") }
                    }
                }
            } else {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryBadge(task.category)
                        TaskBadge(when (task.priority) { "high" -> "Ưu tiên cao"; "low" -> "Ưu tiên thấp"; else -> "Ưu tiên vừa" },
                            TaskColors.Line, TaskColors.Grey)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(task.title, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)) {
                        DetailInfo(TaskIcons.Calendar, "Hạn chót", "${clock.dueLabel(task.date)}, ${task.time}")
                        HorizontalDivider(color = TaskColors.Line)
                        DetailInfo(TaskIcons.Bell, "Nhắc nhở", if (task.remind) "Trước 15 phút" else "Không nhắc nhở")
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Ghi chú", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TaskColors.Grey)
                    Spacer(Modifier.height(8.dp))
                    Text(task.note.ifBlank { "Chưa có ghi chú." }, Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                        .background(Color.White).padding(16.dp), fontSize = 16.sp, lineHeight = 22.sp,
                        color = if (task.note.isBlank()) TaskColors.Grey else TaskColors.Ink)
                    Spacer(Modifier.height(18.dp))
                    val done = task.subtasks.count { it.done }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Việc nhỏ", fontSize = 14.sp, color = TaskColors.Grey, fontWeight = FontWeight.SemiBold)
                        Text("$done/${task.subtasks.size}", fontSize = 14.sp, color = TaskColors.Grey, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(7.dp))
                    LinearProgressIndicator(progress = { if (task.subtasks.isEmpty()) 0f else done.toFloat() / task.subtasks.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = TaskColors.Blue, trackColor = TaskColors.Line, gapSize = 0.dp, drawStopIndicator = {})
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)) {
                        task.subtasks.forEachIndexed { index, subtask ->
                            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 9.dp, end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                TaskCheck(subtask.done, {
                                    onSave(task.copy(subtasks = task.subtasks.mapIndexed { i, item ->
                                        if (i == index) item.copy(done = !item.done) else item
                                    }), {})
                                }, "Hoàn thành việc nhỏ ${subtask.title}", enabled = !busy, square = true)
                                Spacer(Modifier.width(5.dp))
                                Text(subtask.title, Modifier.weight(1f), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = if (subtask.done) Color(0xFF99999D) else TaskColors.Ink,
                                    textDecoration = if (subtask.done) TextDecoration.LineThrough else TextDecoration.None)
                                IconButton({ editIndex = index; editTitle = subtask.title }, Modifier.size(38.dp), enabled = !busy) {
                                    Icon(TaskIcons.Edit, "Sửa việc nhỏ ${subtask.title}", Modifier.size(18.dp), tint = TaskColors.Grey)
                                }
                                IconButton({ onSave(task.copy(subtasks = task.subtasks.filterIndexed { i, _ -> i != index }), {}) },
                                    Modifier.size(36.dp), enabled = !busy) {
                                    Icon(TaskIcons.Delete, "Xóa việc nhỏ ${subtask.title}", Modifier.size(18.dp), tint = TaskColors.Red)
                                }
                            }
                            HorizontalDivider(color = TaskColors.Line)
                        }
                        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 15.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(TaskIcons.Add, null, Modifier.size(21.dp), tint = TaskColors.Blue)
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(newSubtask, { newSubtask = it.take(200) }, Modifier.weight(1f), enabled = !busy,
                                singleLine = true, textStyle = TextStyle(fontSize = 16.sp, color = TaskColors.Ink),
                                cursorBrush = SolidColor(TaskColors.Blue), decorationBox = { inner ->
                                    if (newSubtask.isEmpty()) Text("Thêm việc nhỏ", color = Color(0xFF99999D))
                                    inner()
                                })
                            TextButton({
                                val value = newSubtask.trim()
                                if (value.isNotEmpty()) onSave(task.copy(subtasks = task.subtasks + TodoSubtask(value)), { newSubtask = "" })
                            }, enabled = newSubtask.isNotBlank() && !busy, contentPadding = PaddingValues(start = 10.dp)) {
                                Text("Thêm", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    additionalContent(task)
                    val error = state.detailErrors[id] ?: state.error
                    if (error != null) TaskStatus(state.copy(loading = false, error = error), onRetry)
                    Spacer(Modifier.height(24.dp))
                }
                Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button({ onSave(task.copy(isCompleted = !task.isCompleted), {}) }, enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaskColors.Blue)) {
                        if (busy) CircularProgressIndicator(Modifier.size(21.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text(if (task.isCompleted) "Đánh dấu chưa hoàn thành" else "Đánh dấu hoàn thành", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onDelete, enabled = !busy, modifier = Modifier.padding(top = 6.dp)) {
                        Text("Xóa công việc", fontSize = 16.sp, color = TaskColors.Red)
                    }
                }
                editIndex?.let { index ->
                    AlertDialog(onDismissRequest = { if (!busy) editIndex = null }, title = { Text("Sửa việc nhỏ") },
                        text = {
                            Column {
                                OutlinedTextField(editTitle, { editTitle = it.take(200) }, label = { Text("Tên việc nhỏ") }, enabled = !busy)
                                state.error?.let { Text(it, color = TaskColors.Red, fontSize = 13.sp) }
                            }
                        }, confirmButton = {
                            TextButton({
                                onSave(task.copy(subtasks = task.subtasks.mapIndexed { i, item ->
                                    if (i == index) item.copy(title = editTitle.trim()) else item
                                }), { editIndex = null })
                            }, enabled = !busy && editTitle.isNotBlank()) { Text("Lưu") }
                        }, dismissButton = { TextButton({ editIndex = null }, enabled = !busy) { Text("Hủy") } })
                }
            }
        }
    }
}

@Composable
private fun DetailInfo(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = TaskColors.Grey)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), color = TaskColors.Grey, fontSize = 16.sp)
        Text(value, fontSize = 16.sp)
    }
}
