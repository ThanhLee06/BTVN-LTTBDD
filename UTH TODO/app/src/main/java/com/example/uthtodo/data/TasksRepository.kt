package com.example.uthtodo.data

import com.example.uthtodo.BuildConfig
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TasksState(
    val tasks: List<TodoTask> = emptyList(),
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val detailLoading: Set<String> = emptySet(),
    val busy: Set<String> = emptySet(),
    val error: String? = null,
    val detailErrors: Map<String, String> = emptyMap(),
    val lastSavedId: String? = null
)

/** The teaching mock returns static mutation samples. Keep acknowledged writes for this session. */
class TasksRepository(private val api: TaskApi, private val persistence: TaskPersistence? = null) {
    private val mutableState = MutableStateFlow(TasksState())
    val state = mutableState.asStateFlow()
    private val changed = mutableMapOf<String, TodoTask>()
    private val deleted = mutableSetOf<String>()
    private var generation = 0

    fun clear() {
        generation++
        changed.clear(); deleted.clear(); mutableState.value = TasksState()
    }

    fun dismissError() { mutableState.update { it.copy(error = null) } }

    suspend fun load(force: Boolean = false) {
        if (state.value.loading || (state.value.loaded && !force)) return
        val session = generation
        mutableState.update { it.copy(loading = true, error = null) }
        try {
            val response = api.list()
            if (session != generation) return
            val remote = persistence?.restore(response) ?: response
            if (session != generation) return
            val merged = remote.filterNot { it.id in deleted }.associateBy { it.id }.toMutableMap()
            merged.putAll(changed)
            mutableState.update { it.copy(tasks = merged.values.toList(), loaded = true) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (session == generation) {
                val cached = runCatching { persistence?.cached() }.getOrNull()
                if (session == generation) mutableState.update { it.copy(tasks = cached?.takeIf { list -> list.isNotEmpty() } ?: it.tasks,
                    error = message(e)) }
            }
        }
        finally { if (session == generation) mutableState.update { it.copy(loading = false) } }
    }

    suspend fun loadDetail(id: String) {
        if (id in state.value.detailLoading || id in deleted) return
        val session = generation
        mutableState.update { it.copy(detailLoading = it.detailLoading + id, detailErrors = it.detailErrors - id) }
        try {
            persistence?.cached()?.find { it.id == id }?.let { local ->
                if (session == generation) mutableState.update { it.copy(tasks = upsert(it.tasks, local)) }
            }
            val remote = api.detail(id)
            check(remote.id == id) { "Máy chủ trả về công việc không đúng mã yêu cầu." }
            if (session != generation || id in deleted) return
            val task = changed[id] ?: persistence?.restore(listOf(remote))?.find { it.id == id } ?: remote
            if (session != generation || id in deleted) return
            mutableState.update { it.copy(tasks = upsert(it.tasks, task)) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (session == generation) mutableState.update { it.copy(detailErrors = it.detailErrors + (id to message(e))) }
        } finally { if (session == generation) mutableState.update { it.copy(detailLoading = it.detailLoading - id) } }
    }

    suspend fun save(task: TodoTask): Boolean {
        if (!TaskRules.valid(task)) {
            mutableState.update { it.copy(error = "Nhập tên công việc, ngày và giờ hợp lệ.") }
            return false
        }
        val creating = task.id.isBlank()
        val key = if (creating) "new" else task.id
        if (key in state.value.busy) return false
        return mutate(key) {
            val saved = if (creating) {
                val response = api.create(task)
                // Mock ID 7 can repeat; local IDs prevent replacing a previous task in this session.
                var localId = System.currentTimeMillis()
                while (state.value.tasks.any { it.id == localId.toString() }) localId++
                val id = response.id.takeUnless { candidate -> state.value.tasks.any { it.id == candidate } } ?: localId.toString()
                task.copy(id = id)
            } else {
                api.update(task)
                task
            }
            saved
        }
    }

    suspend fun delete(id: String): Boolean = mutate(id, remove = true) { api.delete(id); null }

    private suspend fun mutate(key: String, remove: Boolean = false, operation: suspend () -> TodoTask?): Boolean {
        if (key in state.value.busy) return false
        val session = generation
        mutableState.update { it.copy(busy = it.busy + key, error = null) }
        return try {
            val saved = operation()
            if (session != generation) return false
            if (remove) {
                deleted.add(key); changed.remove(key)
                mutableState.update { it.copy(tasks = it.tasks.filterNot { task -> task.id == key }) }
                try { persistence?.deleted(key) }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { if (session == generation)
                    mutableState.update { state -> state.copy(error = "Công việc đã xóa trên API; chưa dọn được dữ liệu thiết bị. Mở lại Cài đặt để thử lại.") }
                }
            } else if (saved != null) {
                changed[saved.id] = saved; deleted.remove(saved.id)
                mutableState.update { it.copy(tasks = upsert(it.tasks, saved), lastSavedId = saved.id) }
                try { persistence?.saved(saved) }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { if (session == generation)
                    mutableState.update { state -> state.copy(error = "Công việc đã lưu trên API; chưa lưu được bản trên thiết bị. Vui lòng thử lại.") }
                }
            }
            session == generation
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (session == generation) mutableState.update { it.copy(error = message(e)) }
            false
        } finally { if (session == generation) mutableState.update { it.copy(busy = it.busy - key) } }
    }

    private fun message(e: Exception): String = when (e) {
        is TaskApiException, is IllegalStateException -> e.message ?: "Không thể tải công việc."
        else -> "Không thể kết nối. Kiểm tra Internet rồi thử lại."
    }

    private fun upsert(tasks: List<TodoTask>, task: TodoTask): List<TodoTask> =
        if (tasks.any { it.id == task.id }) tasks.map { if (it.id == task.id) task else it } else tasks + task
}

object TaskGraph {
    val repository by lazy { TasksRepository(HttpTaskApi(), com.example.uthtodo.features.FeatureGraph.store) }
    val clock = TaskClock(BuildConfig.TASK_REFERENCE_DATE)
}
