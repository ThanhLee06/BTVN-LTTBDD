package com.example.uthtodo

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthtodo.features.*
import com.example.uthtodo.data.TaskGraph
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FullFeatureFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun attachLocationAndCameraPhotoToExistingTask() {
        compose.waitUntil(20_000) { TaskGraph.repository.state.value.loaded }
        compose.onNodeWithText("Ôn chương 3 – Cấu trúc dữ liệu").performScrollTo().performClick()
        compose.onNodeWithText("Gắn vị trí hiện tại").performScrollTo().performClick()
        compose.waitUntil(20_000) { FeatureGraph.store.state.value.tasks["2"]?.location != null }
        compose.onNodeWithText("Mở bản đồ").assertExists()
        val before = FeatureGraph.store.state.value.tasks["2"]?.photos.orEmpty().size
        if (before < 5) {
            compose.onNodeWithText("Chụp ảnh").performScrollTo().performClick()
            compose.waitUntil(20_000) { compose.onAllNodesWithText("Chụp ảnh").fetchSemanticsNodes().any { !it.config.contains(SemanticsProperties.Disabled) } }
            compose.onNodeWithText("Chụp ảnh").performClick()
            compose.waitUntil(20_000) { compose.onAllNodesWithText("Dùng ảnh").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Dùng ảnh").performClick()
            compose.waitUntil(15_000) { FeatureGraph.store.state.value.tasks["2"]?.photos.orEmpty().size > before }
        }
        compose.onNodeWithText("Ảnh đính kèm · ${FeatureGraph.store.state.value.tasks["2"]!!.photos.size}/5").performScrollTo()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = requireNotNull(instrumentation.targetContext.getExternalFilesDir("permissions-screenshots")).apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(folder, "task-location-photo.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        }
        val owner = requireNotNull(FeatureGraph.store.owner)
        val saved = runBlocking { FeatureGraph.store.find(owner, "2") }!!
        assertNotNull(saved.location)
        assertTrue(saved.photos.isNotEmpty())
        assertTrue(File(saved.photos.last().path).exists())
    }
}
