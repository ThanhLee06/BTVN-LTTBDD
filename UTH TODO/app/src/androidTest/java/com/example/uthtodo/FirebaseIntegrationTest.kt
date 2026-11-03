package com.example.uthtodo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Intent
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Tests a temporary document in the signed-in account; never changes existing tasks. */
@RunWith(AndroidJUnit4::class)
class FirebaseIntegrationTest {
    /** Reads only notifications owned by this app, never the system-wide notification dump. */
    @Test fun remoteBackgroundNotificationReachedAndroid() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        assertTrue("Firebase Console background notification must reach Android", manager.activeNotifications.any {
            it.notification.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() == "UTH Todo — FCM chạy nền"
        })
    }

    @Test fun appCheckIssuesValidToken() = runBlocking {
        withTimeout(30000) {
            val result = com.google.firebase.appcheck.FirebaseAppCheck.getInstance().getAppCheckToken(true).await()
            assertTrue("App Check must issue a nonempty token", result.token.isNotBlank())
            assertTrue("App Check token must not be expired", result.expireTimeMillis > System.currentTimeMillis())
        }
    }

    @Test fun configuredServicesReachBackend() = runBlocking {
        withTimeout(60000) {
            val uid = requireNotNull(FirebaseAuth.getInstance().currentUser).uid
            withTimeout(30000) {
                val presenceRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("presence").child(uid)
                while (presenceRef.get().await().children.none {
                    it.child("online").getValue(Boolean::class.java) == true
                }) kotlinx.coroutines.delay(500)
            }
            val config = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
            config.fetchAndActivate().await()
            assertEquals(com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE,
                config.getValue("banner_message").source)
            assertTrue(config.getString("banner_message").contains("UTH Todo"))
            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            val installation = com.google.firebase.installations.FirebaseInstallations.getInstance().id.await()
            val device = FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("devices").document(installation).get(Source.SERVER).await()
            assertEquals("FCM token must be registered for this account", token, device.getString("token"))
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.runOnMainSync {
                com.example.uthtodo.services.NotificationService.show("Thông báo thử nghiệm", "Kiểm tra BT2", test = true)
                com.example.uthtodo.services.CrashlyticsService.sendDemoReport()
            }
            val manager = instrumentation.targetContext.getSystemService(android.app.NotificationManager::class.java)
            assertTrue("Local notification must reach Android", manager.activeNotifications.any { it.id == 1001 })
        }
    }

    @Test fun mainActivityLaunches() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit {
                instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            try { future.get(25, TimeUnit.SECONDS) }
            catch (error: Exception) {
                throw AssertionError("Main thread during startup:\n" + Looper.getMainLooper().thread.stackTrace.joinToString("\n"), error)
            }
        } finally { executor.shutdownNow() }
    }

    @Test fun ownTaskCrudAndCrossUserIsolation() = runBlocking {
        withTimeout(60000) {
            val user = requireNotNull(FirebaseAuth.getInstance().currentUser) { "Sign in on this emulator before running." }
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("users").document(user.uid).collection("tasks")
                .document("integration_" + UUID.randomUUID().toString())
            try {
                ref.set(mapOf("title" to "Kiểm tra đồng bộ BT2", "category" to "Học tập", "time" to "15:00",
                    "isCompleted" to false, "createdAt" to System.currentTimeMillis(), "completedAt" to null)).await()
                assertEquals("Kiểm tra đồng bộ BT2", ref.get(Source.SERVER).await().getString("title"))
                ref.update(mapOf("isCompleted" to true, "completedAt" to System.currentTimeMillis())).await()
                assertEquals(true, ref.get(Source.SERVER).await().getBoolean("isCompleted"))
                ref.delete().await()
                assertFalse(ref.get(Source.SERVER).await().exists())
                try {
                    db.collection("users").document("other_user_" + UUID.randomUUID())
                        .collection("tasks").document("private").get(Source.SERVER).await()
                    fail("Cross-user access must be denied")
                } catch (error: FirebaseFirestoreException) {
                    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, error.code)
                }
                try {
                    ref.set(mapOf("title" to "Invalid schema")).await()
                    fail("Invalid task schema must be denied")
                } catch (error: FirebaseFirestoreException) {
                    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, error.code)
                }
            } finally { ref.delete().await() }
        }
    }
}
