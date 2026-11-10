package com.example.uthtodo.data

import com.example.uthtodo.model.TodoTask

interface TaskPersistence {
    suspend fun restore(remote: List<TodoTask>): List<TodoTask>
    suspend fun cached(): List<TodoTask>
    suspend fun saved(task: TodoTask)
    suspend fun deleted(id: String)
}
