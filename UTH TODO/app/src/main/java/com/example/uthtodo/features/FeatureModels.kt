package com.example.uthtodo.features

import com.example.uthtodo.model.TodoTask
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class TaskLocation(val latitude: Double, val longitude: Double, val accuracyMeters: Float,
    val capturedAt: Long, val precise: Boolean, val label: String = "")
data class TaskPhoto(val id: String, val path: String, val createdAt: Long)
data class LocalTaskData(val task: TodoTask, val identity: String,
    val location: TaskLocation? = null, val photos: List<TaskPhoto> = emptyList(),
    val changed: Boolean = false, val deleted: Boolean = false,
    val reminderRequested: Boolean = false, val reminderDue: Long = 0,
    val version: Long = 0, val deliveredVersion: Long = -1)
data class FeatureState(val owner: String? = null, val tasks: Map<String, LocalTaskData> = emptyMap(),
    val error: String? = null, val busy: Boolean = false)
data class ReminderDestination(val owner: String, val taskId: String, val identity: String)

object ReminderRules {
    fun due(task: TodoTask, zone: TimeZone = TimeZone.getDefault()): Long = runCatching {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply { isLenient = false; timeZone = zone }
        formatter.parse("${task.date} ${task.time}")!!.time
    }.getOrDefault(0L)
    fun trigger(task: TodoTask, now: Long, explicit: Boolean, zone: TimeZone = TimeZone.getDefault()): Long {
        val due = due(task, zone)
        if (!task.remind || task.isCompleted || due <= now) return 0
        val wanted = due - 15 * 60_000L
        return if (wanted > now) wanted else if (explicit) now + 1_000L else 0
    }
}
