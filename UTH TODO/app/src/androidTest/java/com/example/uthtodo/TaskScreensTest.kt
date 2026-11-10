package com.example.uthtodo

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.data.TasksState
import com.example.uthtodo.model.TodoSubtask
import com.example.uthtodo.model.TodoTask
import com.example.uthtodo.model.UserProfile
import com.example.uthtodo.screens.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TaskScreensTest {
    @get:Rule val compose = createComposeRule()
    private val clock = TaskClock("2026-10-20")
    private val fixture = listOf(
        TodoTask("4", "Tập thể dục 30 phút", "Cuộc sống", "06:30", true, date = "2026-10-20"),
        TodoTask("1", "Nộp bài tập Lập trình di động", "Học tập", "09:00", true, date = "2026-10-20", priority = "high"),
        TodoTask("2", "Ôn chương 3 – Cấu trúc dữ liệu", "Học tập", "14:00", date = "2026-10-20", remind = true,
            note = "Tập trung phần cây nhị phân và bảng băm.",
            subtasks = listOf(TodoSubtask("Đọc lại slide bài giảng", true), TodoSubtask("Làm 10 bài tập cuối chương"))),
        TodoTask("3", "Đi chợ mua đồ cho tuần", "Cuộc sống", "17:30", date = "2026-10-20", priority = "low"),
        TodoTask("6", "Gọi điện về nhà", "Cuộc sống", "21:00", date = "2026-10-21", priority = "low"),
        TodoTask("5", "Họp nhóm đồ án", "Học tập", "20:00", date = "2026-10-25", priority = "high")
    )

    @Test fun homeReferenceAndTaskNavigation() {
        var opened = ""
        compose.setContent {
            HomeScreen(TasksState(fixture, loaded = true), clock, UserProfile(), {}, {}, { opened = it }, {}, {}, {}, {})
        }
        compose.onNodeWithText("2/4 việc").assertIsDisplayed()
        compose.onNodeWithText("50%").assertIsDisplayed()
        compose.onAllNodesWithText("2 việc còn lại").assertCountEquals(2)
        screenshot("home")
        compose.onNodeWithText("Ôn chương 3 – Cấu trúc dữ liệu").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("2", opened) }
    }

    @Test fun listReferenceAndFilterKeepsGlobalPendingCount() {
        compose.setContent { TaskListScreen(TasksState(fixture, loaded = true), clock, "all", {}, {}, {}, {}, {}) }
        compose.onNodeWithText("4 việc chưa xong").assertIsDisplayed()
        compose.onNodeWithText("Gấp").assertIsDisplayed()
        screenshot("list")
        compose.onAllNodesWithText("Học tập")[0].performClick()
        compose.onNodeWithText("4 việc chưa xong").assertIsDisplayed()
        compose.onNodeWithText("Đi chợ mua đồ cho tuần").assertDoesNotExist()
        compose.onNode(hasSetTextAction()).performTextInput("không có kết quả")
        compose.onNodeWithText("Không tìm thấy công việc nào.").assertIsDisplayed()
        compose.onNodeWithText("4 việc chưa xong").assertIsDisplayed()
    }

    @Test fun uncheckingCompletedTaskMovesItToPending() {
        val state = mutableStateOf(TasksState(fixture, loaded = true))
        compose.setContent {
            TaskListScreen(state.value, clock, "all", {}, {}, {}, { id ->
                state.value = state.value.copy(tasks = state.value.tasks.map {
                    if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
                })
            }, {})
        }
        compose.onNodeWithContentDescription("Hoàn thành Tập thể dục 30 phút").performScrollTo().performClick()
        compose.onNodeWithText("5 việc chưa xong").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Tập thể dục 30 phút").assertExists()
    }

    @Test fun detailReferenceAndSubtaskCompletion() {
        val state = mutableStateOf(TasksState(fixture, loaded = true))
        compose.setContent {
            TaskDetailScreen("2", "list", state.value, clock, {}, {}, {}, { updated, success ->
                state.value = state.value.copy(tasks = state.value.tasks.map { if (it.id == updated.id) updated else it })
                success()
            }, {})
        }
        compose.onNodeWithText("1/2").assertIsDisplayed()
        compose.onNodeWithText("Hôm nay, 14:00").assertIsDisplayed()
        screenshot("detail")
        compose.onNodeWithContentDescription("Hoàn thành việc nhỏ Làm 10 bài tập cuối chương").performScrollTo().performClick()
        compose.onNodeWithText("2/2").assertIsDisplayed()
        compose.onNodeWithText("Đánh dấu hoàn thành").performClick()
        compose.onNodeWithText("Đánh dấu chưa hoàn thành").assertIsDisplayed()
    }

    @Test fun subtaskCanBeAddedEditedAndRemoved() {
        val state = mutableStateOf(TasksState(fixture, loaded = true))
        compose.setContent {
            TaskDetailScreen("2", "list", state.value, clock, {}, {}, {}, { updated, success ->
                state.value = state.value.copy(tasks = state.value.tasks.map { if (it.id == updated.id) updated else it })
                success()
            }, {})
        }
        compose.onNode(hasSetTextAction()).performTextInput("Việc nhỏ thử")
        compose.onNodeWithText("Thêm").performScrollTo().performClick()
        screenshot("subtask-input-check")
        compose.onNodeWithText("1/3").assertExists()
        compose.onNodeWithContentDescription("Xóa việc nhỏ Việc nhỏ thử").performScrollTo().performClick()
        compose.onNodeWithText("1/2").assertExists()
        compose.onNodeWithContentDescription("Sửa việc nhỏ Đọc lại slide bài giảng").performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("Đọc lại slide bài giảng")).performTextReplacement("Đọc chương mới")
        compose.onNodeWithText("Lưu").performClick()
        compose.onNodeWithText("Đọc chương mới").assertExists()
    }

    @Test fun listShowsServerErrorAndRetry() {
        var retries = 0
        compose.setContent { TaskListScreen(TasksState(error = "Lỗi mẫu từ máy chủ"), clock, "all", {}, {}, {}, {}, { retries++ }) }
        compose.onNodeWithText("Lỗi mẫu từ máy chủ").assertIsDisplayed()
        compose.onNodeWithText("Thử lại").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = requireNotNull(context.getExternalFilesDir("screenshots")).apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
