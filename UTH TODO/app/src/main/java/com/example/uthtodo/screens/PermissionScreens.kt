package com.example.uthtodo.screens

import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.location.LocationManagerCompat
import com.example.uthtodo.components.TaskColors
import com.example.uthtodo.components.TaskStyle
import com.example.uthtodo.features.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

private fun permissionIcon(kind: PermissionKind) = when (kind) {
    PermissionKind.LOCATION -> Icons.Outlined.LocationOn
    PermissionKind.NOTIFICATIONS -> Icons.Outlined.NotificationsNone
    PermissionKind.CAMERA -> Icons.Outlined.CameraAlt
}

@Composable
fun PermissionIntroOverlay(controller: PermissionController) {
    val intro by FeatureGraph.preferences.state.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val state = intro ?: return
    if (state.version >= 1) return
    fun finish() { scope.launch { FeatureGraph.preferences.finish() } }
    val kind = PermissionKind.entries.getOrNull(state.step)
    if (kind == null && state.step >= PermissionKind.entries.size) { LaunchedEffect(Unit) { FeatureGraph.preferences.finish() }; return }
    fun next() { scope.launch { FeatureGraph.preferences.step(state.step + 1) } }
    Dialog(onDismissRequest = ::finish) {
        TaskStyle {
            Surface(shape = RoundedCornerShape(26.dp), color = Color.White) {
                Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(100.dp).background(TaskColors.StudyBg, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(kind?.let(::permissionIcon) ?: Icons.Outlined.VerifiedUser, null, Modifier.size(54.dp), tint = TaskColors.Blue)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(kind?.title ?: "Thiết lập quyền ứng dụng", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Text(kind?.explanation ?: "Thiết lập vị trí, thông báo và camera để dùng thêm các tiện ích cho công việc. Bạn có thể để sau.",
                        fontSize = 15.sp, lineHeight = 22.sp, color = TaskColors.Grey, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(26.dp))
                    Button(onClick = {
                        if (kind == null) scope.launch { FeatureGraph.preferences.step(0) }
                        else controller.request(kind, explain = false, onGranted = ::next)
                    }, Modifier.fillMaxWidth().height(48.dp)) {
                        Text(if (kind == null) "Thiết lập" else if (controller.snapshot.granted(kind)) "Đã cho phép · Tiếp tục" else "Cho phép")
                    }
                    OutlinedButton(onClick = { if (kind == null) finish() else next() }, Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Để sau") }
                    if (kind != null) TextButton(onClick = ::finish) { Text("Bỏ qua tất cả") }
                }
            }
        }
    }
}

@Composable
fun AppPermissionsScreen(controller: PermissionController, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val features by FeatureGraph.store.state.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    TaskStyle {
        Column(Modifier.fillMaxSize().background(TaskColors.Background).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TextButton(onBack) { Text("‹ Cài đặt") }
            Text("Quyền ứng dụng", fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("Bạn quyết định những tiện ích UTH Todo có thể sử dụng.", color = TaskColors.Grey)
            PermissionKind.entries.forEach { kind ->
                val granted = controller.snapshot.granted(kind)
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(permissionIcon(kind), null, tint = TaskColors.Blue)
                            Spacer(Modifier.width(12.dp)); Text(kind.title, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
                        }
                        val status = when (kind) {
                            PermissionKind.LOCATION -> when { controller.snapshot.fine -> "Đã cho phép vị trí chính xác"; controller.snapshot.coarse -> "Đã cho phép vị trí gần đúng"; else -> "Chưa cho phép" }
                            PermissionKind.CAMERA -> when { !controller.snapshot.hasCamera -> "Thiết bị không có camera"; granted -> "Đã cho phép"; else -> "Chưa cho phép" }
                            PermissionKind.NOTIFICATIONS -> when {
                                !controller.snapshot.notificationRuntime -> "Chưa cấp quyền thông báo"
                                !controller.snapshot.appNotifications -> "Thông báo ứng dụng đang tắt"
                                !controller.snapshot.reminderChannel -> "Kênh Nhắc việc đang tắt"
                                else -> "Đã bật thông báo nhắc việc"
                            }
                        }
                        Text(status, color = if (granted) TaskColors.Blue else TaskColors.Grey)
                        Text(kind.explanation, fontSize = 14.sp, color = TaskColors.Grey)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!granted) OutlinedButton({ controller.request(kind) }, enabled = kind != PermissionKind.CAMERA || controller.snapshot.hasCamera) { Text("Cấp quyền") }
                            TextButton({ controller.openSettings(kind) }) { Text("Mở Cài đặt") }
                        }
                        if (kind == PermissionKind.LOCATION) {
                            val enabled = LocationManagerCompat.isLocationEnabled(context.getSystemService(LocationManager::class.java))
                            Text(if (enabled) "Dịch vụ vị trí thiết bị đang bật" else "Dịch vụ vị trí thiết bị đang tắt", fontSize = 13.sp)
                            if (!enabled) TextButton({ runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) } }) { Text("Bật dịch vụ vị trí") }
                        }
                        if (kind == PermissionKind.NOTIFICATIONS) OutlinedButton({
                            controller.request(kind) {
                                val shown = FeatureGraph.scheduler.test()
                                Toast.makeText(context, if (shown) "Đã gửi thông báo thử trên thiết bị" else "Thông báo đang bị chặn", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text("Thử thông báo") }
                    }
                }
            }
            Text("Nhắc việc được đặt trước hạn 15 phút theo giờ thiết bị. Android có thể trì hoãn khi tiết kiệm pin. Khi buộc dừng app, hãy mở lại để khôi phục lịch.",
                fontSize = 13.sp, color = TaskColors.Grey)
            OutlinedButton({ scope.launch { FeatureGraph.preferences.replay(); withContext(Dispatchers.Main.immediate) { onBack() } } }) { Text("Xem lại giới thiệu quyền") }
            if (features.owner != null) TextButton({ confirmClear = true }) { Text("Xóa ảnh, địa điểm và tắt lịch nhắc", color = TaskColors.Red) }
            features.error?.let { Text(it, color = TaskColors.Red) }
        }
        if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Xóa dữ liệu bổ sung?") },
            text = { Text("Xóa ảnh và địa điểm lưu trên thiết bị của tài khoản này, đồng thời tắt các lịch nhắc. Công việc vẫn được giữ.") },
            confirmButton = { TextButton({ scope.launch { FeatureGraph.store.guard { FeatureGraph.store.clearCurrent() }; confirmClear = false } }) { Text("Xóa") } },
            dismissButton = { TextButton({ confirmClear = false }) { Text("Hủy") } })
    }
}
