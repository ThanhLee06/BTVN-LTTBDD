package com.example.uthtodo.features

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.permissionDataStore by preferencesDataStore("permission_intro")
data class PermissionIntro(val version: Int = 0, val step: Int = -1, val asked: Set<String> = emptySet())
class PermissionPreferences(context: Context) {
    private val store = context.applicationContext.permissionDataStore
    val state = store.data.catch { emit(emptyPreferences()) }.map { p -> PermissionIntro(
        p[intPreferencesKey("version")] ?: 0, p[intPreferencesKey("step")] ?: -1,
        p[stringSetPreferencesKey("asked")] ?: emptySet()) }
    suspend fun step(step: Int) { store.edit { it[intPreferencesKey("step")] = step } }
    suspend fun finish() { store.edit { it[intPreferencesKey("version")] = 1; it[intPreferencesKey("step")] = -1 } }
    suspend fun replay() { store.edit { it[intPreferencesKey("version")] = 0; it[intPreferencesKey("step")] = -1 } }
    suspend fun asked(kind: PermissionKind) { store.edit {
        val key = stringSetPreferencesKey("asked"); it[key] = (it[key] ?: emptySet()) + kind.name
    } }
}

enum class PermissionKind(val title: String, val explanation: String) {
    LOCATION("Vị trí", "Gắn địa điểm hiện tại vào công việc khi bạn yêu cầu. Vị trí gần đúng vẫn sử dụng được."),
    NOTIFICATIONS("Thông báo", "Nhận thông báo nhắc những công việc bạn đã bật nhắc nhở."),
    CAMERA("Camera", "Chụp ảnh tài liệu hoặc kết quả công việc để đính kèm trên thiết bị.");
    fun permissions(): Array<String> = when (this) {
        LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        CAMERA -> arrayOf(Manifest.permission.CAMERA)
        NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray()
    }
}

data class PermissionSnapshot(val coarse: Boolean, val fine: Boolean, val camera: Boolean,
    val notificationRuntime: Boolean, val appNotifications: Boolean, val reminderChannel: Boolean, val hasCamera: Boolean) {
    fun granted(kind: PermissionKind) = when (kind) {
        PermissionKind.LOCATION -> coarse || fine
        PermissionKind.CAMERA -> camera && hasCamera
        PermissionKind.NOTIFICATIONS -> notificationRuntime && appNotifications && reminderChannel
    }
    companion object {
        fun read(context: Context): PermissionSnapshot {
            fun has(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            val manager = context.getSystemService(NotificationManager::class.java)
            return PermissionSnapshot(has(Manifest.permission.ACCESS_COARSE_LOCATION), has(Manifest.permission.ACCESS_FINE_LOCATION),
                has(Manifest.permission.CAMERA), Build.VERSION.SDK_INT < 33 || has(Manifest.permission.POST_NOTIFICATIONS),
                NotificationManagerCompat.from(context).areNotificationsEnabled(), Build.VERSION.SDK_INT < 26 ||
                    manager.getNotificationChannel(DeviceReminderScheduler.CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE,
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        }
    }
}

fun Context.activity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.activity(); else -> null }

@Stable
class PermissionController(private val context: Context) {
    var snapshot by mutableStateOf(PermissionSnapshot.read(context)); private set
    var explanation by mutableStateOf<PermissionKind?>(null)
    var settingsRequired by mutableStateOf(false)
    private var pending: PermissionKind? = null
    private var success: (() -> Unit)? = null
    internal var launch: (Array<String>) -> Unit = {}
    fun refresh() { snapshot = PermissionSnapshot.read(context) }
    fun request(kind: PermissionKind, explain: Boolean = true, onGranted: () -> Unit = {}) {
        refresh()
        if (snapshot.granted(kind)) { onGranted(); return }
        if (kind == PermissionKind.CAMERA && !snapshot.hasCamera) {
            Toast.makeText(context, "Thiết bị không có camera. Bạn có thể chọn ảnh từ thư viện.", Toast.LENGTH_LONG).show(); return
        }
        pending = kind; success = onGranted
        FeatureGraph.scope.launch {
            val history = FeatureGraph.preferences.state.first()
            val activity = context.activity()
            val rationale = kind.permissions().any { activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
            settingsRequired = (kind == PermissionKind.NOTIFICATIONS && snapshot.notificationRuntime) ||
                (kind.name in history.asked && !rationale)
            if (explain || settingsRequired || rationale) explanation = kind else proceed()
        }
    }
    fun proceed() {
        val kind = pending ?: return
        explanation = null
        if (settingsRequired) { openSettings(kind); cancel(); return }
        FeatureGraph.scope.launch {
            FeatureGraph.preferences.asked(kind)
            val permissions = kind.permissions()
            if (permissions.isEmpty()) { openSettings(kind); cancel() } else launch(permissions)
        }
    }
    internal fun result() {
        refresh()
        if (pending?.let(snapshot::granted) == true) success?.invoke()
        else Toast.makeText(context, "Bạn có thể cấp quyền sau trong Cài đặt → Quyền ứng dụng.", Toast.LENGTH_SHORT).show()
        cancel()
        FeatureGraph.scope.launch { FeatureGraph.store.guard { FeatureGraph.store.reschedule() } }
    }
    fun cancel() { pending = null; success = null; explanation = null }
    fun openSettings(kind: PermissionKind) {
        val intent = if (kind == PermissionKind.NOTIFICATIONS && Build.VERSION.SDK_INT >= 26) {
            if (snapshot.appNotifications && !snapshot.reminderChannel) Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).putExtra(Settings.EXTRA_CHANNEL_ID, DeviceReminderScheduler.CHANNEL)
            else Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        runCatching { context.startActivity(intent) }.onFailure {
            Toast.makeText(context, "Hãy mở Cài đặt hệ thống → Ứng dụng → UTH Todo.", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun rememberPermissionController(): PermissionController {
    val context = LocalContext.current
    val controller = remember(context) { PermissionController(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { controller.result() }
    controller.launch = { launcher.launch(it) }
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) {
            controller.refresh()
            FeatureGraph.scope.launch { FeatureGraph.store.guard { FeatureGraph.store.reschedule() } }
        } }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); controller.cancel() }
    }
    controller.explanation?.let { kind ->
        AlertDialog(onDismissRequest = controller::cancel, title = { Text(kind.title) },
            text = { Text(if (controller.settingsRequired) "Mở Cài đặt hệ thống để cấp hoặc bật lại quyền ${kind.title.lowercase()}." else kind.explanation) },
            confirmButton = { TextButton(onClick = controller::proceed) { Text(if (controller.settingsRequired) "Mở Cài đặt" else "Cho phép") } },
            dismissButton = { TextButton(onClick = controller::cancel) { Text("Để sau") } })
    }
    return controller
}
