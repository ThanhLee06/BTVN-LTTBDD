package com.example.uthtodo

import android.app.NotificationManager
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import com.example.uthtodo.screens.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PermissionFeaturesTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val device get() = UiDevice.getInstance(instrumentation)

    @Test fun a_introCanBeSkippedWithoutGrantingPermissions() {
        runBlocking { FeatureGraph.preferences.replay() }
        compose.setContent { val controller = rememberPermissionController(); Box(Modifier.fillMaxSize()) { PermissionIntroOverlay(controller) } }
        compose.onNodeWithText("Thiết lập").performClick()
        compose.onNodeWithText("Vị trí").assertIsDisplayed(); screenshot("intro-location")
        compose.onNodeWithText("Để sau").performClick()
        compose.onNodeWithText("Thông báo").assertIsDisplayed(); screenshot("intro-notifications")
        compose.onNodeWithText("Để sau").performClick()
        compose.onNodeWithText("Camera").assertIsDisplayed(); screenshot("intro-camera")
        compose.onNodeWithText("Bỏ qua tất cả").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Bỏ qua tất cả").fetchSemanticsNodes().isEmpty() }
        assertEquals(1, runBlocking { FeatureGraph.preferences.state.first().version })
    }

    @Test fun b_notificationPermissionUsesSystemDialog() {
        var granted = false
        compose.setContent {
            val controller = rememberPermissionController()
            Button({ controller.request(PermissionKind.NOTIFICATIONS) { granted = true } }) { Text("Yêu cầu thông báo") }
        }
        compose.onNodeWithText("Yêu cầu thông báo").performClick()
        if (!PermissionSnapshot.read(context).granted(PermissionKind.NOTIFICATIONS)) {
            compose.onNodeWithText("Cho phép").performClick(); allowSystem("notification-permission")
        }
        compose.waitUntil(10_000) { granted }
        assertTrue(PermissionSnapshot.read(context).granted(PermissionKind.NOTIFICATIONS))
        assertTrue(FeatureGraph.scheduler.test())
        compose.waitUntil(5_000) { context.getSystemService(NotificationManager::class.java).activeNotifications.any { it.id == 2900 } }
    }

    @Test fun c_locationPermissionAndCurrentLocation() {
        var granted = false
        compose.setContent {
            val controller = rememberPermissionController()
            Button({ controller.request(PermissionKind.LOCATION) { granted = true } }) { Text("Yêu cầu vị trí") }
        }
        compose.onNodeWithText("Yêu cầu vị trí").performClick()
        if (!PermissionSnapshot.read(context).granted(PermissionKind.LOCATION)) {
            compose.onNodeWithText("Cho phép").performClick()
            device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_location_accuracy_radio_coarse")), 2_000)?.click()
            allowSystem("location-permission")
        }
        compose.waitUntil(10_000) { granted }
        val point = runBlocking { TaskLocationService(context).current() }
        assertTrue(point.latitude in -90.0..90.0); assertTrue(point.longitude in -180.0..180.0)
        assertTrue("Emulator must be supplied a location before this test", point.latitude in 10.0..11.0)
    }

    @Test fun d_cameraPermissionPreviewAndRealCapture() {
        var photo: TaskPhoto? = null
        compose.setContent { CameraScreen(rememberPermissionController(), {}, { photo = it }) }
        if (!PermissionSnapshot.read(context).camera) {
            compose.onNodeWithText("Cấp quyền Camera").performClick()
            compose.onNodeWithText("Cho phép").performClick(); allowSystem("camera-permission")
        }
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("Chụp ảnh").fetchSemanticsNodes().any { !it.config.contains(SemanticsProperties.Disabled) }
        }
        screenshot("camera-preview")
        compose.onNodeWithText("Chụp ảnh").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Dùng ảnh").fetchSemanticsNodes().isNotEmpty() }
        screenshot("camera-confirm")
        compose.onNodeWithText("Dùng ảnh").performClick()
        compose.waitUntil(15_000) { photo != null }
        assertTrue(File(photo!!.path).length() > 0)
        FeatureGraph.store.safeDelete(photo!!.path)
    }

    private fun allowSystem(name: String) {
        val allowed = device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_allow_foreground_only_button")), 2_000)
            ?: device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_allow_button")), 3_000)
        assertNotNull("Android permission dialog must appear", allowed)
        screenshot(name)
        allowed!!.click()
    }
    private fun screenshot(name: String) {
        val file = File(requireNotNull(context.getExternalFilesDir("permissions-screenshots")).apply { mkdirs() }, "$name.png")
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        }
    }
}
