package com.example.uthtodo

import com.example.uthtodo.data.TaskApi
import com.example.uthtodo.data.TasksRepository
import com.example.uthtodo.model.TodoSubtask
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class TasksRepositoryTest {
    private val task = TodoTask("2", "Ôn chương 3", "Học tập", "14:00", date = "2026-10-20",
        subtasks = listOf(TodoSubtask("Đọc slide", true), TodoSubtask("Bài tập")))
    private inner class FakeApi : TaskApi {
        var failWrite = false
        var detailResult = task
        var gate: CompletableDeferred<Unit>? = null
        var detailId: String? = null
        override suspend fun list(): List<TodoTask> { gate?.await(); return listOf(task, task.copy(id = "3")) }
        override suspend fun detail(id: String): TodoTask { detailId = id; return detailResult }
        override suspend fun create(task: TodoTask) = this@TasksRepositoryTest.task.copy(id = "7", title = "Static sample")
        override suspend fun update(task: TodoTask) { if (failWrite) throw IOException("offline") }
        override suspend fun delete(id: String) { if (failWrite) throw IOException("offline") }
    }

    @Test fun acknowledgedEditsSurviveStaticListAndDetailWithoutReordering() = runBlocking {
        val api = FakeApi(); val repo = TasksRepository(api)
        repo.load()
        val edited = task.copy(isCompleted = true, subtasks = task.subtasks + TodoSubtask("Mới"))
        assertTrue(repo.save(edited))
        repo.load(force = true); repo.loadDetail("2")
        assertEquals("2", api.detailId)
        assertEquals(listOf("2", "3"), repo.state.value.tasks.map { it.id })
        assertEquals(edited, repo.state.value.tasks.first())
    }

    @Test fun failedWritesKeepOriginalTaskAndReportAnError() = runBlocking {
        val api = FakeApi(); val repo = TasksRepository(api); repo.load(); api.failWrite = true
        assertFalse(repo.save(task.copy(isCompleted = true)))
        assertFalse(repo.delete("2"))
        assertEquals(task, repo.state.value.tasks.first())
        assertTrue(repo.state.value.busy.isEmpty())
        assertNotNull(repo.state.value.error)
    }

    @Test fun deleteDoesNotReappearOnRefresh() = runBlocking {
        val repo = TasksRepository(FakeApi()); repo.load()
        assertTrue(repo.delete("2")); repo.load(force = true); repo.loadDetail("2")
        assertEquals(listOf("3"), repo.state.value.tasks.map { it.id })
    }

    @Test fun createPreservesInputAndDoesNotOverwriteOnRepeatedMockIds() = runBlocking {
        val repo = TasksRepository(FakeApi()); repo.load()
        assertTrue(repo.save(task.copy(id = "", title = "Việc mới 1")))
        assertTrue(repo.save(task.copy(id = "", title = "Việc mới 2")))
        assertEquals(4, repo.state.value.tasks.size)
        assertEquals(4, repo.state.value.tasks.map { it.id }.distinct().size)
        assertTrue(repo.state.value.tasks.any { it.title == "Việc mới 2" })
    }

    @Test fun wrongDetailIdDoesNotReplaceSelectedTask() = runBlocking {
        val api = FakeApi(); val repo = TasksRepository(api); repo.load()
        api.detailResult = task.copy(id = "1"); repo.loadDetail("2")
        assertEquals(task, repo.state.value.tasks.first())
        assertNotNull(repo.state.value.detailErrors["2"])
    }

    @Test fun responseFromPreviousAccountIsDiscarded() = runBlocking {
        val api = FakeApi().apply { gate = CompletableDeferred() }; val repo = TasksRepository(api)
        val request = async { repo.load() }
        kotlinx.coroutines.yield()
        repo.clear(); api.gate!!.complete(Unit); request.await()
        assertFalse(repo.state.value.loaded)
        assertTrue(repo.state.value.tasks.isEmpty())
    }
}
