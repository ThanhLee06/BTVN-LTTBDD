package com.example.uthtodo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthtodo.features.*
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FeatureStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val scheduler = object : ReminderScheduler {
        override fun cancel(owner: String, id: String) {}
        override fun schedule(owner: String, data: LocalTaskData) {}
    }
    @Test fun metadataSurvivesReopenAndIsolatedBetweenAccounts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FeatureDatabase::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            val task = TodoTask("2", "Study", "Học tập", "14:00", date = "2026-11-11")
            val store = FeatureStore(context, db.records(), scheduler, scope)
            store.bind("test_a"); store.restore(listOf(task))
            val point = TaskLocation(10.77, 106.70, 500f, 100, false, "Trường")
            store.setExtras(task, point, emptyList())
            val restored = FeatureStore(context, db.records(), scheduler, scope)
            restored.bind("test_a")
            assertEquals(task, restored.cached().single())
            assertEquals(point, restored.find("test_a", "2")!!.location)
            restored.bind("test_b"); restored.restore(listOf(task))
            assertNull(restored.find("test_b", "2")!!.location)
            assertEquals(point, restored.find("test_a", "2")!!.location)
        } finally { scope.cancel(); db.close() }
    }
    @Test fun canceledReminderAndDeletedTaskDoNotReturnFromMock() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FeatureDatabase::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            val store = FeatureStore(context, db.records(), scheduler, scope)
            val task = TodoTask("2", "Study", "Học tập", "14:00", date = "2026-12-11", remind = true)
            store.bind("test_a"); store.restore(listOf(task))
            store.saved(task.copy(isCompleted = true)); store.restore(listOf(task))
            assertFalse(store.find("test_a", "2")!!.reminderRequested)
            assertTrue(store.cached().single().isCompleted)
            store.deleted("2")
            assertTrue(store.restore(listOf(task)).isEmpty())
        } finally { scope.cancel(); db.close() }
    }
    @Test fun importedPhotoIsPrivateResizedAndRemovable() = runBlocking {
        val original = File(FeatureStore.photoDirectory(context), "test-import-original.jpg")
        val image = Bitmap.createBitmap(2000, 1200, Bitmap.Config.ARGB_8888)
        image.eraseColor(android.graphics.Color.BLUE)
        original.outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, 90, it) }; image.recycle()
        var result: TaskPhoto? = null
        try {
            result = PhotoFiles.import(context, Uri.fromFile(original))
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(result.path, options)
            assertEquals(1600, options.outWidth)
            assertEquals(960, options.outHeight)
            assertEquals(FeatureStore.photoDirectory(context).canonicalPath, File(result.path).parentFile!!.canonicalPath)
        } finally { original.delete(); result?.let { File(it.path).delete() } }
    }
}
