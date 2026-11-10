package com.example.uthtodo

import android.app.NotificationManager
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class NotificationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun alarmPostsAndOpensCorrectLocalTask() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val owner = requireNotNull(FeatureGraph.store.owner)
        val later = Date(System.currentTimeMillis() + 10 * 60_000)
        val task = TodoTask(System.currentTimeMillis().toString(), "Kiểm thử nhắc việc trên thiết bị", "Học tập",
            SimpleDateFormat("HH:mm", Locale.ROOT).format(later), date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(later), remind = true)
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            manager.cancel(2900)
            runBlocking { FeatureGraph.store.saved(task) }
            compose.waitUntil(60_000) { manager.activeNotifications.any { it.tag == "$owner/${task.id}" } }
            device.openNotification()
            val notice = device.wait(Until.findObject(By.text(task.title)), 10_000)
            assertNotNull("Scheduled notification must appear in system shade", notice)
            val folder = requireNotNull(context.getExternalFilesDir("permissions-screenshots")).apply { mkdirs() }
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                val bounds = notice!!.visibleBounds
                val crop = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
                File(folder, "scheduled-notification.png").outputStream().use { crop.compress(Bitmap.CompressFormat.PNG, 100, it) }
                crop.recycle(); bitmap.recycle()
            }
            notice!!.click()
            compose.waitUntil(20_000) { compose.onAllNodesWithText("Ghi chú").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Ghi chú").assertIsDisplayed()
            compose.onNodeWithText(task.title).assertIsDisplayed()
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(folder, "notification-task.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
            }
        } finally { runBlocking { FeatureGraph.store.deleted(task.id) } }
    }
}
