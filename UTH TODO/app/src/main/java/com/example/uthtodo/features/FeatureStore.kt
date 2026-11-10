package com.example.uthtodo.features

import android.content.Context
import com.example.uthtodo.data.TaskPersistence
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

interface ReminderScheduler {
    fun cancel(owner: String, id: String)
    fun schedule(owner: String, data: LocalTaskData)
}

/** Room is the authority for private metadata and acknowledged mock writes, scoped by account. */
class FeatureStore(context: Context, private val dao: FeatureDao,
    private val scheduler: ReminderScheduler, private val scope: CoroutineScope) : TaskPersistence {
    private val context = context.applicationContext
    private val lock = Mutex()
    private val mutableState = MutableStateFlow(FeatureState())
    val state = mutableState.asStateFlow()
    @Volatile var owner: String? = null
        private set

    fun bind(uid: String?) {
        val previous = owner
        owner = uid
        mutableState.value = FeatureState(owner = uid)
        scope.launch {
            guard {
                lock.withLock {
                    if (previous != null && previous != uid) dao.all(previous).forEach { scheduler.cancel(previous, it.taskId) }
                    if (uid != null && uid == owner) {
                        val entries = read(uid)
                        publish(uid, entries)
                        entries.values.forEach { scheduler.schedule(uid, it) }
                    }
                }
                cleanDrafts()
            }
        }
    }

    override suspend fun restore(remote: List<TodoTask>): List<TodoTask> {
        val uid = owner ?: return remote
        return lock.withLock {
            val local = read(uid).toMutableMap()
            remote.forEach { task ->
                val old = local[task.id]
                val data = old?.takeIf { it.changed || it.deleted }
                    ?: (old ?: LocalTaskData(task, UUID.randomUUID().toString(), reminderRequested = task.remind,
                        reminderDue = ReminderRules.trigger(task, System.currentTimeMillis(), false))).copy(task = task)
                local[task.id] = data
                dao.put(FeatureRecord(uid, task.id, FeatureCodec.encode(data)))
            }
            publish(uid, local)
            if (uid == owner) local.values.forEach { scheduler.schedule(uid, it) }
            val result = remote.mapNotNull { task -> local[task.id]?.takeUnless { it.deleted }?.task }.toMutableList()
            local.values.filter { it.changed && !it.deleted && result.none { item -> item.id == it.task.id } }.forEach { result.add(it.task) }
            result
        }
    }

    override suspend fun cached(): List<TodoTask> = owner?.let { uid ->
        lock.withLock { read(uid).also { publish(uid, it) }.values.filterNot { it.deleted }.map { it.task } }
    } ?: emptyList()

    override suspend fun saved(task: TodoTask) {
        val uid = owner ?: return
        lock.withLock {
            val old = dao.find(uid, task.id)?.let { FeatureCodec.decode(it.payload) }
            val scheduleChanged = old == null || old.task.date != task.date || old.task.time != task.time ||
                old.task.remind != task.remind || old.task.isCompleted != task.isCompleted
            val next = (old ?: LocalTaskData(task, UUID.randomUUID().toString())).copy(task = task, changed = true, deleted = false,
                reminderRequested = if (scheduleChanged) task.remind && !task.isCompleted else old!!.reminderRequested,
                reminderDue = if (scheduleChanged) ReminderRules.trigger(task, System.currentTimeMillis(), true) else old!!.reminderDue,
                version = (old?.version ?: 0) + if (scheduleChanged) 1 else 0)
            put(uid, next)
        }
    }

    override suspend fun deleted(id: String) {
        val uid = owner ?: return
        lock.withLock {
            dao.find(uid, id)?.let { row ->
                val old = FeatureCodec.decode(row.payload)
                put(uid, old.copy(deleted = true, photos = emptyList(), location = null, reminderRequested = false, version = old.version + 1))
                old.photos.forEach { safeDelete(it.path) }
            }
            scheduler.cancel(uid, id)
        }
    }

    suspend fun setExtras(task: TodoTask, location: TaskLocation?, photos: List<TaskPhoto>) = change(task) { data ->
        require(photos.size <= 5) { "Mỗi công việc tối đa 5 ảnh." }
        val removed = data.photos.filterNot { photo -> photos.any { it.id == photo.id } }
        data.copy(location = location, photos = photos) to { removed.forEach { safeDelete(it.path) } }
    }

    suspend fun change(task: TodoTask, transform: (LocalTaskData) -> Pair<LocalTaskData, () -> Unit>) {
        val uid = owner ?: error("Hãy đăng nhập để lưu dữ liệu công việc.")
        lock.withLock {
            val old = dao.find(uid, task.id)?.let { FeatureCodec.decode(it.payload) }
                ?: LocalTaskData(task, UUID.randomUUID().toString())
            val (next, cleanup) = transform(old)
            check(uid == owner) { "Tài khoản đã thay đổi. Vui lòng mở lại công việc." }
            put(uid, next)
            withContext(Dispatchers.IO) { cleanup() }
        }
    }

    suspend fun find(uid: String, id: String): LocalTaskData? = dao.find(uid, id)?.let { FeatureCodec.decode(it.payload) }

    suspend fun reschedule() {
        val uid = owner ?: return
        lock.withLock {
            val records = read(uid).mapValues { (_, data) ->
                val due = if (data.reminderRequested && data.deliveredVersion != data.version)
                    ReminderRules.trigger(data.task, System.currentTimeMillis(), true) else 0
                data.copy(reminderDue = due, version = data.version + if (due != data.reminderDue && due != 0L) 1 else 0)
            }
            records.values.forEach { put(uid, it) }
            publish(uid, records)
        }
    }

    suspend fun deliver(uid: String, id: String, identity: String, version: Long, show: (LocalTaskData) -> Boolean) {
        lock.withLock {
            if (owner != uid) return
            val data = find(uid, id) ?: return
            if (data.identity != identity || data.version != version || data.deliveredVersion == version ||
                data.deleted || !data.reminderRequested || data.task.isCompleted || ReminderRules.due(data.task) <= System.currentTimeMillis()) return
            if (show(data)) put(uid, data.copy(deliveredVersion = version))
        }
    }

    suspend fun clearCurrent() {
        val uid = owner ?: return
        lock.withLock {
            val rows = read(uid)
            rows.values.forEach { scheduler.cancel(uid, it.task.id) }
            rows.values.forEach { data ->
                val cleared = data.copy(photos = emptyList(), location = null, reminderRequested = false, reminderDue = 0, version = data.version + 1)
                dao.put(FeatureRecord(uid, data.task.id, FeatureCodec.encode(cleared)))
            }
            withContext(Dispatchers.IO) { rows.values.flatMap { it.photos }.forEach { safeDelete(it.path) } }
            publish(uid, read(uid))
        }
    }

    suspend fun cleanDrafts() = withContext(Dispatchers.IO) {
        val used = dao.allAccounts().flatMap { runCatching { FeatureCodec.decode(it.payload).photos }.getOrDefault(emptyList()) }
            .map { File(it.path).canonicalPath }.toSet()
        photoDirectory(context).listFiles()?.filter { it.canonicalPath !in used && it.lastModified() < System.currentTimeMillis() - 86_400_000L }
            ?.forEach { safeDelete(it.path) }
    }

    fun dismissError() { mutableState.update { it.copy(error = null) } }
    suspend fun guard(block: suspend () -> Unit): Boolean {
        val startingOwner = owner
        mutableState.update { it.copy(busy = true, error = null) }
        return try { block(); true }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (startingOwner == owner) mutableState.update { it.copy(error = e.message?.takeIf { e is IllegalArgumentException || e is IllegalStateException }
                ?: "Chưa lưu được dữ liệu trên thiết bị. Kiểm tra bộ nhớ rồi thử lại.") }; false
        } finally { if (startingOwner == owner) mutableState.update { it.copy(busy = false) } }
    }

    private suspend fun read(uid: String) = dao.all(uid).associate { it.taskId to FeatureCodec.decode(it.payload) }
    private suspend fun put(uid: String, data: LocalTaskData) {
        check(uid == owner) { "Tài khoản đã thay đổi." }
        dao.put(FeatureRecord(uid, data.task.id, FeatureCodec.encode(data)))
        publish(uid, read(uid))
        scheduler.schedule(uid, data)
    }
    private fun publish(uid: String, records: Map<String, LocalTaskData>) {
        if (uid == owner) mutableState.update { it.copy(owner = uid, tasks = records) }
    }
    fun safeDelete(path: String) {
        val root = photoDirectory(context).canonicalFile
        val target = File(path).canonicalFile
        if (target.parentFile == root) target.delete()
    }
    companion object { fun photoDirectory(context: Context) = File(context.filesDir, "uth_photos").apply { mkdirs() } }
}
