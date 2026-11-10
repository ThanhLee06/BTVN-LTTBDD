package com.example.uthtodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uthtodo.data.TaskGraph
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {
    private val repository = TaskGraph.repository
    val state = repository.state
    val clock = TaskGraph.clock
    fun load(force: Boolean = false) { viewModelScope.launch { repository.load(force) } }
    fun detail(id: String) { viewModelScope.launch { repository.loadDetail(id) } }
    fun dismissError() = repository.dismissError()
    fun save(task: TodoTask, onSuccess: () -> Unit = {}) {
        viewModelScope.launch { if (repository.save(task)) onSuccess() }
    }
    fun toggle(id: String) {
        state.value.tasks.find { it.id == id }?.let { save(it.copy(isCompleted = !it.isCompleted)) }
    }
    fun delete(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch { if (repository.delete(id)) onSuccess() }
    }
}
