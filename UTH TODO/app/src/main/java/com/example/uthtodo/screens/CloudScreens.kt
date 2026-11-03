package com.example.uthtodo.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.uthtodo.BuildConfig
import com.example.uthtodo.model.TodoRepository
import com.example.uthtodo.services.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)
        .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("‹ Quay lại") }
        Text("Thông báo", style = MaterialTheme.typography.headlineMedium)
        if (NotificationService.notices.isEmpty()) {
            Text("Chưa có thông báo được lưu trên thiết bị. Các thông báo nhận khi đang mở app sẽ xuất hiện ở đây.")
        }
        NotificationService.notices.forEach { notice ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(notice.title, style = MaterialTheme.typography.titleMedium)
                    Text(notice.body)
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(Date(notice.receivedAt)),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onProfile: () -> Unit) {
    val context = LocalContext.current
    var appCheckStatus by remember { mutableStateOf("") }
    var notificationsAllowed by remember {
        mutableStateOf(Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsAllowed = it
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)
        .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("‹ Quay lại") }
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick = onProfile) { Text("Thông tin cá nhân") }
        Text("Đồng bộ công việc", style = MaterialTheme.typography.titleMedium)
        Text(PresenceService.status.value)
        Text(TodoRepository.syncError.value ?: when {
            TodoRepository.hasPendingWrites.value -> "Có thay đổi đang chờ đồng bộ"
            TodoRepository.isFromCache.value -> "Đang dùng dữ liệu đã lưu trên thiết bị"
            else -> "Công việc được đồng bộ theo tài khoản"
        })
        OutlinedButton(onClick = TodoRepository::retrySync) { Text("Đồng bộ lại") }
        HorizontalDivider()
        Text("Thông báo", style = MaterialTheme.typography.titleMedium)
        Text(if (notificationsAllowed) "Đã cho phép thông báo" else "Chưa cho phép thông báo")
        if (!notificationsAllowed && Build.VERSION.SDK_INT >= 33) {
            OutlinedButton(onClick = { permission.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Cho phép thông báo") }
        }
        Text(NotificationService.tokenStatus.value, style = MaterialTheme.typography.bodySmall)
        HorizontalDivider()
        Text(RemoteConfigService.status.value)
        OutlinedButton(onClick = RemoteConfigService::refresh) { Text("Kiểm tra cập nhật giao diện") }
        if (BuildConfig.DEBUG) {
            HorizontalDivider()
            Text("Công cụ kiểm thử (bản debug)", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = {
                appCheckStatus = "Đang xác minh App Check"
                com.google.firebase.appcheck.FirebaseAppCheck.getInstance().getAppCheckToken(true)
                    .addOnSuccessListener { appCheckStatus = "App Check đã cấp token hợp lệ cho thiết bị thử nghiệm" }
                    .addOnFailureListener { appCheckStatus = "Chưa xác minh được App Check. Kiểm tra đăng ký thiết bị trong Firebase Console." }
            }) { Text("Kiểm tra App Check") }
            if (appCheckStatus.isNotBlank()) Text(appCheckStatus, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token -> copyDebugIdentifier(context, "FCM test token", token) }
                    .addOnFailureListener { Toast.makeText(context, "Chưa lấy được mã thiết bị", Toast.LENGTH_SHORT).show() }
            }) { Text("Sao chép token FCM thử nghiệm") }
            OutlinedButton(onClick = {
                com.google.firebase.installations.FirebaseInstallations.getInstance().id
                    .addOnSuccessListener { id -> copyDebugIdentifier(context, "Firebase installation ID", id) }
                    .addOnFailureListener { Toast.makeText(context, "Chưa lấy được mã cài đặt", Toast.LENGTH_SHORT).show() }
            }) { Text("Sao chép Installation ID thử nghiệm") }
            OutlinedButton(onClick = {
                NotificationService.show("Thông báo thử nghiệm", "Bạn đã bật thông báo cho UTH Todo.", test = true)
                Toast.makeText(context, "Đã tạo thông báo cục bộ thử nghiệm", Toast.LENGTH_SHORT).show()
            }) { Text("Thử hiển thị thông báo") }
            OutlinedButton(onClick = {
                CrashlyticsService.sendDemoReport()
                Toast.makeText(context, "Đã ghi báo cáo lỗi thử nghiệm, app vẫn hoạt động", Toast.LENGTH_LONG).show()
            }) { Text("Ghi báo cáo lỗi non-fatal") }
        }
    }
}

private fun copyDebugIdentifier(context: android.content.Context, label: String, value: String) {
    val clip = android.content.ClipData.newPlainText(label, value)
    if (Build.VERSION.SDK_INT >= 33) {
        clip.description.extras = android.os.PersistableBundle().apply {
            putBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    context.getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(clip)
    Toast.makeText(context, "Đã sao chép mã thử nghiệm; chỉ dán vào Firebase Console của bạn", Toast.LENGTH_LONG).show()
}
