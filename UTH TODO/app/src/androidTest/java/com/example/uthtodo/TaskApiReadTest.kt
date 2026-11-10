package com.example.uthtodo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.uthtodo.data.HttpTaskApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only verification against the teaching mock; mutations are covered with a fake API. */
@RunWith(AndroidJUnit4::class)
class TaskApiReadTest {
    @Test fun authenticatedListAndSelectedDetailMatch() = runBlocking {
        val api = HttpTaskApi()
        val tasks = api.list()
        assertTrue(tasks.isNotEmpty())
        val expected = tasks.first { it.id == "2" }
        val detail = api.detail(expected.id)
        assertEquals(expected, detail)
        assertEquals(2, detail.subtasks.size)
    }
}
