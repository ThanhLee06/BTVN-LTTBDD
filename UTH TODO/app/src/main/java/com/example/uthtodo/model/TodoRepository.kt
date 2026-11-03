package com.example.uthtodo.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.uthtodo.services.AnalyticsService
import com.example.uthtodo.services.CrashlyticsService
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.services.PresenceService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.Calendar

object TodoRepository {
    val currentUser = mutableStateOf(UserProfile())
    val tasks = mutableStateListOf<TodoTask>()
    val isLoading = mutableStateOf(false)
    val isFromCache = mutableStateOf(false)
    val hasPendingWrites = mutableStateOf(false)
    val syncError = mutableStateOf<String?>(null)
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var listener: ListenerRegistration? = null
    private var firstLoadTrace: Trace? = null
    private var boundUid: String? = null

    fun updateWithFirebaseUser(user: FirebaseUser?) {
        currentUser.value = if (user == null) UserProfile() else UserProfile(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: if (user.isAnonymous) "Khách" else user.email?.substringBefore('@') ?: "Người dùng",
            email = user.email,
            isAnonymous = user.isAnonymous,
            isGoogleLinked = user.providerData.any { it.providerId == "google.com" },
            photoUrl = user.photoUrl?.toString()
        )
        if (boundUid == user?.uid) return
        listener?.remove()
        firstLoadTrace?.stop()
        firstLoadTrace = null
        PresenceService.stop()
        tasks.clear()
        syncError.value = null
        hasPendingWrites.value = false
        boundUid = user?.uid
        NotificationService.bindUser(user?.uid)
        CrashlyticsService.setUserId(user?.uid.orEmpty())
        AnalyticsService.setUser(user)
        if (user == null) {
            isLoading.value = false
            return
        }
        PresenceService.start(user.uid)
        NotificationService.registerDevice()
        listen(user.uid)
    }

    fun retrySync() {
        boundUid?.let { uid ->
            listener?.remove()
            firstLoadTrace?.stop()
            firstLoadTrace = null
            listen(uid)
            NotificationService.registerDevice()
        }
    }

    private fun listen(uid: String) {
        isLoading.value = true
        syncError.value = null
        firstLoadTrace = FirebasePerformance.getInstance().newTrace("tasks_initial_sync").also { it.start() }
        listener = db.collection("users").document(uid).collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (boundUid != uid) return@addSnapshotListener
                if (error != null) {
                    isLoading.value = false
                    syncError.value = "Chưa đồng bộ được công việc. Kiểm tra kết nối rồi thử lại."
                    firstLoadTrace?.putAttribute("result", "error")
                    firstLoadTrace?.stop()
                    firstLoadTrace = null
                    CrashlyticsService.logError("firestore_listen", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val remote = snapshot.documents.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        TodoTask(doc.id, title, doc.getString("category") ?: "Cuộc sống",
                            doc.getString("time") ?: "08:00", doc.getBoolean("isCompleted") ?: false,
                            doc.getLong("createdAt") ?: 0, doc.getLong("completedAt"))
                    }
                    tasks.clear()
                    tasks.addAll(remote)
                    isLoading.value = false
                    isFromCache.value = snapshot.metadata.isFromCache
                    hasPendingWrites.value = snapshot.metadata.hasPendingWrites()
                    if (!snapshot.metadata.isFromCache) syncError.value = null
                    // Measure the first server result, not just the empty local cache.
                    if (!snapshot.metadata.isFromCache) {
                        firstLoadTrace?.putMetric("task_count", remote.size.toLong())
                        firstLoadTrace?.putAttribute("result", "success")
                        firstLoadTrace?.stop()
                        firstLoadTrace = null
                    }
                }
            }
    }

    fun addTask(title: String, category: String, time: String): Boolean {
        val uid = boundUid ?: return false
        if (!isValidTask(title, category, time)) return false
        val ref = db.collection("users").document(uid).collection("tasks").document()
        val task = TodoTask(ref.id, title.trim(), category, time, createdAt = System.currentTimeMillis())
        ref.set(task.toFields()).addOnFailureListener { failure(uid, "task_create", it) }
        AnalyticsService.event("task_create", category)
        return true // Firestore queues this write offline; metadata exposes pending writes.
    }

    fun editTask(task: TodoTask, title: String, category: String, time: String): Boolean {
        val uid = boundUid ?: return false
        if (!isValidTask(title, category, time)) return false
        db.collection("users").document(uid).collection("tasks").document(task.id)
            .set(task.copy(title = title.trim(), category = category, time = time).toFields())
            .addOnFailureListener { failure(uid, "task_edit", it) }
        AnalyticsService.event("task_edit", category)
        return true
    }

    fun toggleTask(taskId: String) {
        val uid = boundUid ?: return
        val task = tasks.find { it.id == taskId } ?: return
        db.collection("users").document(uid).collection("tasks").document(taskId)
            .update(mapOf("isCompleted" to !task.isCompleted,
                "completedAt" to if (task.isCompleted) null else System.currentTimeMillis()))
            .addOnFailureListener { failure(uid, "task_toggle", it) }
        AnalyticsService.event("task_toggle", task.category, !task.isCompleted)
    }

    fun deleteTask(taskId: String) {
        val uid = boundUid ?: return
        db.collection("users").document(uid).collection("tasks").document(taskId)
            .delete().addOnFailureListener { failure(uid, "task_delete", it) }
        AnalyticsService.event("task_delete")
    }

    private fun failure(uid: String, operation: String, error: Exception) {
        if (boundUid == uid) syncError.value = "Không lưu được thay đổi. Vui lòng thử lại."
        CrashlyticsService.logError(operation, error)
    }

    private fun TodoTask.toFields(): Map<String, Any?> = mapOf(
        "title" to title, "category" to category, "time" to time,
        "isCompleted" to isCompleted, "createdAt" to createdAt, "completedAt" to completedAt
    )

    fun isValidTask(title: String, category: String, time: String): Boolean =
        title.trim().length in 1..200 && category in listOf("Học tập", "Cuộc sống") &&
            Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)

    fun getCompletedCount(): Int = tasks.count { it.isCompleted }
    fun getTotalCount(): Int = tasks.size
    fun getProgressPercent(): Int = if (tasks.isEmpty()) 0 else getCompletedCount() * 100 / tasks.size
    fun getCategoryRemainingCount(category: String): Int = tasks.count { it.category == category && !it.isCompleted }

    val weeklyStats: List<WeeklyStat>
        get() {
            val today = Calendar.getInstance()
            val monday = (today.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
            }
            val counts = (0..6).map { index ->
                val start = (monday.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, index) }
                val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                tasks.count { it.isCompleted && it.completedAt != null && it.completedAt >= start.timeInMillis && it.completedAt < end.timeInMillis }
            }
            return counts.mapIndexed { index, count -> WeeklyStat(
                if (index == 6) "CN" else "T${index + 2}", count,
                counts.maxOrNull()?.coerceAtLeast(1) ?: 1,
                index == (today.get(Calendar.DAY_OF_WEEK) + 5) % 7
            ) }
        }
}
