package com.example.uthtodo.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.uthtodo.components.TaskColors
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun ExtrasFields(location: TaskLocation?, photos: List<TaskPhoto>, controller: PermissionController,
    onLocation: (TaskLocation?) -> Unit, onCamera: () -> Unit, onPick: () -> Unit,
    onDelete: (TaskPhoto) -> Unit, onView: (TaskPhoto) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var locating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var label by remember(location?.capturedAt, location?.label) { mutableStateOf(location?.label.orEmpty()) }
    Text("Địa điểm", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
    if (location != null) {
        Text(location.label.ifBlank { "Vị trí đã gắn" }, fontSize = 14.sp)
        Text(String.format(Locale.ROOT, "%.5f, %.5f · %s · ±%.0f m", location.latitude, location.longitude,
            if (location.precise) "Chính xác" else "Gần đúng", location.accuracyMeters), fontSize = 12.sp, color = TaskColors.Grey)
        OutlinedTextField(label, { label = it.take(100) }, label = { Text("Tên gợi nhớ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row {
            TextButton({ onLocation(location.copy(label = label.trim())) }) { Text("Lưu tên") }
            TextButton({
                val geo = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}")
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, geo)) }.onFailure {
                    error = "Không có ứng dụng bản đồ. Bạn có thể dùng nút Sao chép tọa độ."
                }
            }) { Text("Mở bản đồ") }
            TextButton({ onLocation(null) }) { Text("Xóa") }
        }
        TextButton({ context.getSystemService(android.content.ClipboardManager::class.java)
            .setPrimaryClip(android.content.ClipData.newPlainText("Tọa độ", "${location.latitude}, ${location.longitude}")) }) { Text("Sao chép tọa độ") }
    }
    OutlinedButton({
        controller.request(PermissionKind.LOCATION) {
            locating = true; error = null
            scope.launch {
                try { onLocation(TaskLocationService(context).current()) }
                catch (e: TimeoutCancellationException) { error = "Lấy vị trí quá lâu. Kiểm tra GPS rồi thử lại." }
                catch (e: CancellationException) { throw e }
                catch (e: SecurityException) { controller.refresh(); error = "Quyền vị trí đã bị thu hồi." }
                catch (e: Exception) { error = e.message?.takeIf { e is IllegalStateException } ?: "Chưa lấy được vị trí. Kiểm tra dịch vụ vị trí rồi thử lại." }
                finally { locating = false }
            }
        }
    }, enabled = !locating) { Text(if (locating) "Đang lấy vị trí…" else "Gắn vị trí hiện tại") }
    error?.let {
        Text(it, color = TaskColors.Red, fontSize = 13.sp)
        TextButton({ runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) } }) { Text("Mở cài đặt vị trí") }
    }
    Text("Ảnh đính kèm · ${photos.size}/5", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items(photos, key = { it.id }) { photo ->
            Column {
                AsyncImage(File(photo.path), "Mở ảnh đính kèm", Modifier.size(84.dp).clickable { onView(photo) })
                TextButton({ onDelete(photo) }) { Text("Xóa ảnh", color = TaskColors.Red, fontSize = 12.sp) }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton({ controller.request(PermissionKind.CAMERA, onGranted = onCamera) }, enabled = photos.size < 5) { Text("Chụp ảnh") }
        TextButton(onPick, enabled = photos.size < 5) { Text("Chọn ảnh") }
    }
}

@Composable
fun TaskExtrasPanel(task: TodoTask, controller: PermissionController, onCamera: () -> Unit,
    onPick: () -> Unit, onView: (TaskPhoto) -> Unit) {
    val state by FeatureGraph.store.state.collectAsState()
    val data = state.tasks[task.id]
    val scope = rememberCoroutineScope()
    fun extras(location: TaskLocation?, photos: List<TaskPhoto>) {
        scope.launch { FeatureGraph.store.guard { FeatureGraph.store.setExtras(task, location, photos) } }
    }
    HorizontalDivider(Modifier.padding(top = 20.dp))
    ExtrasFields(data?.location, data?.photos.orEmpty(), controller,
        { extras(it, data?.photos.orEmpty()) }, onCamera, onPick,
        { photo -> extras(data?.location, data?.photos.orEmpty().filterNot { it.id == photo.id }) }, onView)
    if (task.remind) {
        Text(when {
            !controller.snapshot.granted(PermissionKind.NOTIFICATIONS) -> "Nhắc việc chưa hoạt động: thông báo đang bị chặn."
            ReminderRules.due(task) <= System.currentTimeMillis() -> "Đã quá hạn; không lập lịch nhắc cho việc cũ."
            task.isCompleted -> "Công việc đã hoàn thành; lịch nhắc đã tắt."
            data?.reminderRequested == true && data.deliveredVersion == data.version -> "Đã gửi nhắc việc."
            data?.reminderRequested == true -> "Nhắc trước 15 phút theo giờ thiết bị. Android có thể trì hoãn khi tiết kiệm pin."
            else -> "Lịch nhắc đang tắt. Bạn có thể đặt lại lịch."
        }, fontSize = 12.sp, color = TaskColors.Grey)
        if (!controller.snapshot.granted(PermissionKind.NOTIFICATIONS)) TextButton({ controller.request(PermissionKind.NOTIFICATIONS) }) { Text("Bật thông báo nhắc việc") }
        if (!task.isCompleted && ReminderRules.due(task) > System.currentTimeMillis()) TextButton({
            controller.request(PermissionKind.NOTIFICATIONS) {
                scope.launch { FeatureGraph.store.guard {
                    FeatureGraph.store.change(task) { old ->
                        old.copy(reminderRequested = true, reminderDue = ReminderRules.trigger(task, System.currentTimeMillis(), true), version = old.version + 1) to {}
                    }
                } }
            }
        }) { Text("Đặt lại lịch nhắc") }
    }
    state.error?.let { Text(it, color = TaskColors.Red, fontSize = 13.sp) }
}
