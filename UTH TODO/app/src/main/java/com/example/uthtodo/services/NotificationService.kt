package com.example.uthtodo.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.uthtodo.MainActivity
import com.example.uthtodo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

data class TodoNotice(val title: String, val body: String, val receivedAt: Long)

object NotificationService {
    private const val CHANNEL = "uth_todo_updates"
    private lateinit var context: Context
    private var boundUid: String? = null
    val notices = mutableStateListOf<TodoNotice>()
    val openInbox = mutableStateOf(false)
    val tokenStatus = mutableStateOf("Đang đăng ký thiết bị")
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Cập nhật công việc", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }
    fun bindUser(uid: String?) {
        boundUid = uid
        notices.clear()
        if (uid == null) return
        val raw = context.getSharedPreferences("uth_notices", Context.MODE_PRIVATE).getString(uid, "[]")
        runCatching {
            val data = JSONArray(raw)
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                notices.add(TodoNotice(item.getString("title"), item.getString("body"), item.getLong("at")))
            }
        }
    }
    fun registerDevice() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseInstallations.getInstance().id.addOnSuccessListener { installation ->
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (FirebaseAuth.getInstance().currentUser?.uid != uid) return@addOnSuccessListener
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("devices").document(installation)
                    .set(mapOf("token" to token, "platform" to "android", "updatedAt" to System.currentTimeMillis()))
                    .addOnCompleteListener { tokenStatus.value = if (it.isSuccessful) "Đã đăng ký nhận thông báo" else "Chưa lưu được thiết bị" }
            }.addOnFailureListener { tokenStatus.value = "Chưa lấy được mã thiết bị" }
        }
    }
    suspend fun unregisterDevice() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val installation = FirebaseInstallations.getInstance().id.await()
        FirebaseFirestore.getInstance().collection("users").document(uid).collection("devices")
            .document(installation).delete().await()
    }
    fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("open_notifications", false) == true || intent?.hasExtra("google.message_id") == true) {
            if (intent.hasExtra("notice_title")) {
                rememberNotice(intent.getStringExtra("notice_title") ?: "Cập nhật công việc", intent.getStringExtra("notice_body").orEmpty())
            }
            openInbox.value = true
        }
    }
    fun rememberNotice(title: String, body: String) {
        val uid = boundUid ?: return
        notices.add(0, TodoNotice(title, body, System.currentTimeMillis()))
        while (notices.size > 50) notices.removeAt(notices.lastIndex)
        val data = JSONArray()
        notices.forEach { data.put(JSONObject().put("title", it.title).put("body", it.body).put("at", it.receivedAt)) }
        context.getSharedPreferences("uth_notices", Context.MODE_PRIVATE).edit().putString(uid, data.toString()).apply()
    }
    fun show(title: String, body: String, test: Boolean = false) {
        if (FirebaseAuth.getInstance().currentUser == null) return
        rememberNotice(title, body)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(context, MainActivity::class.java).putExtra("open_notifications", true)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_todo).setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending).setAutoCancel(true).build()
        context.getSystemService(NotificationManager::class.java).notify(if (test) 1001 else System.currentTimeMillis().toInt(), notification)
    }
}

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { NotificationService.registerDevice() }
    override fun onMessageReceived(message: RemoteMessage) {
        NotificationService.show(message.notification?.title ?: message.data["title"] ?: "Cập nhật công việc",
            message.notification?.body ?: message.data["body"].orEmpty())
    }
}
