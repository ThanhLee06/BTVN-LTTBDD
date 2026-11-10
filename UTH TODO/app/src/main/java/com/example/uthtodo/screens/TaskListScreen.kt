package com.example.uthtodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.components.*
import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.data.TaskRules
import com.example.uthtodo.data.TasksState

@Composable
fun TaskListScreen(state: TasksState, clock: TaskClock, initialCategory: String,
    onTab: (String) -> Unit, onAdd: () -> Unit, onTask: (String) -> Unit, onToggle: (String) -> Unit, onRetry: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    val visible = TaskRules.filter(state.tasks, query, category)
    val pending = visible.filterNot { it.isCompleted }
    val completed = visible.filter { it.isCompleted }
    val scroll = rememberLazyListState()
    val topPadding = taskTopPadding()
    TaskPage("list", onTab, onAdd) {
        LazyColumn(Modifier.fillMaxSize(), state = scroll,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = topPadding, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Danh sách", Modifier.weight(1f), fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                    Text("${state.tasks.count { !it.isCompleted }} việc chưa xong", fontSize = 14.sp, color = TaskColors.Grey)
                }
                Spacer(Modifier.height(18.dp))
                BasicTextField(query, { query = it }, Modifier.semantics { contentDescription = "Tìm công việc" }.fillMaxWidth().height(44.dp)
                    .clip(RoundedCornerShape(13.dp)).background(TaskColors.Line), singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = TaskColors.Ink), cursorBrush = SolidColor(TaskColors.Blue),
                    decorationBox = { inner ->
                        Row(Modifier.fillMaxSize().padding(start = 13.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(TaskIcons.Search, null, tint = TaskColors.Grey, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Box(Modifier.weight(1f)) {
                                if (query.isEmpty()) Text("Tìm công việc", color = Color(0xFF99999D))
                                inner()
                            }
                            if (query.isNotEmpty()) IconButton({ query = "" }, Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Close, "Xóa tìm kiếm", Modifier.size(18.dp), tint = TaskColors.Grey)
                            }
                        }
                    })
                Spacer(Modifier.height(16.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "Tất cả", "Học tập" to "Học tập", "Cuộc sống" to "Cuộc sống").forEach { (value, label) ->
                        val selected = category == value
                        Text(label, Modifier.clip(RoundedCornerShape(50)).background(if (selected) TaskColors.Ink else Color.White)
                            .border(1.dp, if (selected) TaskColors.Ink else Color(0xFFE0E0E5), RoundedCornerShape(50))
                            .selectable(selected = selected, role = Role.Tab, onClick = { category = value }).padding(horizontal = 16.dp, vertical = 9.dp),
                            color = if (selected) Color.White else TaskColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
                    }
                }
                TaskStatus(state, onRetry)
                if (pending.isNotEmpty()) {
                    Spacer(Modifier.height(17.dp))
                    SectionLabel("CẦN LÀM")
                    Spacer(Modifier.height(5.dp))
                }
                if (visible.isEmpty() && !state.loading && state.error == null) {
                    Text("Không tìm thấy công việc nào.", Modifier.padding(vertical = 24.dp), color = TaskColors.Grey)
                }
            }
            items(pending, key = { it.id }) { task ->
                TaskItemRow(task, { onToggle(task.id) }, { onTask(task.id) }, listMode = true, clock = clock, busy = task.id in state.busy)
            }
            if (completed.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionLabel("ĐÃ XONG")
                    Spacer(Modifier.height(5.dp))
                }
                items(completed, key = { it.id }) { task ->
                    TaskItemRow(task, { onToggle(task.id) }, { onTask(task.id) }, listMode = true, clock = clock, busy = task.id in state.busy)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(label, color = TaskColors.Grey, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
}
