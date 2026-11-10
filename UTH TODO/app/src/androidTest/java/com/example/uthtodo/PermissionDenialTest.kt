package com.example.uthtodo

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.uthtodo.features.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Run after resetting only the intro preferences and revoking CAMERA on the test emulator. */
@RunWith(AndroidJUnit4::class)
class PermissionDenialTest {
    @get:Rule val compose = createComposeRule()
    @Test fun refusingCameraTwiceOffersSettingsAndKeepsAppUsable() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var protectedCalls = 0
        var unrelatedClicks = 0
        compose.setContent {
            val controller = rememberPermissionController()
            Column {
                Button({ controller.request(PermissionKind.CAMERA) { protectedCalls++ } }) { Text("Dùng camera") }
                Button({ unrelatedClicks++ }) { Text("Xem công việc") }
            }
        }
        repeat(2) {
            compose.onNodeWithText("Dùng camera").performClick()
            compose.onNodeWithText("Cho phép").performClick()
            val deny = device.wait(Until.findObject(By.res("com.android.permissioncontroller", "permission_deny_button")), 5_000)
            assertNotNull("Android denial choice must be shown", deny)
            deny!!.click()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Dùng camera").performClick()
        compose.onNodeWithText("Mở Cài đặt").assertIsDisplayed()
        compose.onNodeWithText("Để sau").performClick()
        compose.onNodeWithText("Xem công việc").performClick()
        compose.runOnIdle { assertEquals(0, protectedCalls); assertEquals(1, unrelatedClicks) }
    }
}
