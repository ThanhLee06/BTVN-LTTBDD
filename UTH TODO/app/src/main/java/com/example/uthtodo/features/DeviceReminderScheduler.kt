package com.example.uthtodo.features

import android.app.*
import android.content.*
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.uthtodo.MainActivity
import com.example.uthtodo.R
import com.example.uthtodo.services.NotificationService
import kotlinx.coroutines.launch

class DeviceReminderScheduler(context: Context) : ReminderScheduler {
    private val context = context.applicationContext
    companion object { const val CHANNEL = "uth_task_reminders" }
    init {
        if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Nhắc việc", NotificationManager.IMPORTANCE_DEFAULT))
    }
    private fun intent(owner: String, id: String) = Intent(context, ReminderReceiver::class.java)
        .setData(Uri.parse("uthtodo://alarm/${Uri.encode(owner)}/${Uri.encode(id)}"))
    override fun cancel(owner: String, id: String) {
        val pending = PendingIntent.getBroadcast(context, 0, intent(owner, id), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pending != null) { context.getSystemService(AlarmManager::class.java).cancel(pending); pending.cancel() }
        context.getSystemService(NotificationManager::class.java).cancel("$owner/$id", 2001)
    }
    override fun schedule(owner: String, data: LocalTaskData) {
        // Updating the alarm must not dismiss an already-delivered notification.
        val old = PendingIntent.getBroadcast(context, 0, intent(owner, data.task.id), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (old != null) context.getSystemService(AlarmManager::class.java).cancel(old)
        if (data.deleted || data.task.isCompleted || !data.reminderRequested) {
            context.getSystemService(NotificationManager::class.java).cancel("$owner/${data.task.id}", 2001)
        }
        if (data.deleted || data.task.isCompleted || !data.reminderRequested || data.deliveredVersion == data.version ||
            data.reminderDue == 0L || ReminderRules.due(data.task) <= System.currentTimeMillis() ||
            !PermissionSnapshot.read(context).granted(PermissionKind.NOTIFICATIONS)) return
        val request = intent(owner, data.task.id).putExtra("owner", owner).putExtra("id", data.task.id)
            .putExtra("identity", data.identity).putExtra("version", data.version)
        val pending = PendingIntent.getBroadcast(context, 0, request, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            data.reminderDue.coerceAtLeast(System.currentTimeMillis() + 500), pending)
    }
    fun show(owner: String, data: LocalTaskData): Boolean {
        if (!PermissionSnapshot.read(context).granted(PermissionKind.NOTIFICATIONS)) return false
        val launch = Intent(context, MainActivity::class.java).setData(Uri.parse("uthtodo://task/${Uri.encode(owner)}/${data.task.id}"))
            .putExtra("reminder_owner", owner).putExtra("reminder_task", data.task.id).putExtra("reminder_identity", data.identity)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(NotificationManager::class.java).notify("$owner/${data.task.id}", 2001,
            NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_stat_todo)
                .setContentTitle(data.task.title).setContentText("Hạn chót: ${data.task.date}, ${data.task.time}")
                .setContentIntent(pending).setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
        NotificationService.rememberNotice(data.task.title, "Hạn chót: ${data.task.date}, ${data.task.time}")
        return true
    }
    fun test(): Boolean {
        if (!PermissionSnapshot.read(context).granted(PermissionKind.NOTIFICATIONS)) return false
        val launch = Intent(context, MainActivity::class.java).putExtra("open_notifications", true)
        val pending = PendingIntent.getActivity(context, 2900, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        context.getSystemService(NotificationManager::class.java).notify(2900,
            NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_stat_todo).setContentTitle("UTH Todo — Thử nhắc việc")
                .setContentText("Thông báo trên thiết bị đang hoạt động.").setContentIntent(pending).setAutoCancel(true).build())
        return true
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val owner = intent.getStringExtra("owner") ?: return
        val id = intent.getStringExtra("id") ?: return
        val identity = intent.getStringExtra("identity") ?: return
        val pending = goAsync()
        FeatureGraph.scope.launch {
            try { FeatureGraph.store.guard {
                FeatureGraph.store.deliver(owner, id, identity, intent.getLongExtra("version", -1)) { FeatureGraph.scheduler.show(owner, it) }
            } } finally { pending.finish() }
        }
    }
}

class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pending = goAsync()
        FeatureGraph.scope.launch {
            try {
                if (FeatureGraph.store.guard { FeatureGraph.store.reschedule() }) {
                    context.applicationContext.getSharedPreferences("uth_reminder_recovery", Context.MODE_PRIVATE).edit()
                        .putString("last_action", intent.action).putLong("at", System.currentTimeMillis()).apply()
                }
            } finally { pending.finish() }
        }
    }
}
