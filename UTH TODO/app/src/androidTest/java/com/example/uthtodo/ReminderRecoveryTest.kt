package com.example.uthtodo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

/** Run prepare, reboot the emulator, then run verify; never changes a task on the server. */
@RunWith(AndroidJUnit4::class)
class ReminderRecoveryTest {
    private val id = "99887766554433"
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun prepareReminderBeforeReboot() = runBlocking {
        val later = Date(System.currentTimeMillis() + 30 * 60_000)
        val task = TodoTask(id, "Kiểm tra khôi phục lịch", "Học tập", SimpleDateFormat("HH:mm", Locale.ROOT).format(later),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(later), remind = true)
        context.getSharedPreferences("uth_reminder_recovery", Context.MODE_PRIVATE).edit().clear().commit()
        FeatureGraph.store.saved(task)
        assertTrue(FeatureGraph.store.find(requireNotNull(FeatureGraph.store.owner), id)!!.reminderRequested)
    }
    @Test fun verifyReminderAfterReboot() = runBlocking {
        val owner = requireNotNull(FeatureGraph.store.owner)
        try {
            val marker = context.getSharedPreferences("uth_reminder_recovery", Context.MODE_PRIVATE)
            withTimeout(30_000) {
                while (marker.getString("last_action", null) != Intent.ACTION_BOOT_COMPLETED) delay(300)
            }
            val restored = FeatureGraph.store.find(owner, id)!!
            assertTrue(restored.reminderRequested)
            val intent = Intent(context, ReminderReceiver::class.java).setData(Uri.parse("uthtodo://alarm/${Uri.encode(owner)}/$id"))
            assertNotNull(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE))
        } finally { FeatureGraph.store.deleted(id) }
    }
}
