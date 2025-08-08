package com.example.tasklistapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasklistapp.data.TaskListRepository
import com.example.tasklistapp.model.Task
import com.example.tasklistapp.model.TaskList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskListRepository(application)
    private val fileName = "tasklist.json"

    private val _taskList = MutableStateFlow<TaskList?>(null)
    val taskList: StateFlow<TaskList?> = _taskList

    init {
        loadTaskList()
    }

    fun loadTaskList() {
        viewModelScope.launch {
            val loaded = repository.loadTaskList(fileName)
            _taskList.value = loaded ?: TaskList("", getDateStampString(), emptyList())
        }
    }

    fun addTask(name: String) {
        _taskList.value?.let {
            val updatedTasks = it.tasks + Task(name)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    fun toggleTaskCompletion(index: Int) {
        _taskList.value?.let {
            val updatedTasks = it.tasks.toMutableList()
            val task = updatedTasks[index]
            updatedTasks[index] = task.copy(isComplete = !task.isComplete)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = it.copy(tasks = updatedTasks)
            repository.saveTaskList(updatedList, fileName)
        }
    }

    fun updateTitle(newTitle: String) {
        _taskList.value?.let {
            _taskList.value = it.copy(title = newTitle)
        }
    }

    fun updateTaskName(index: Int, newName: String) {
        _taskList.value?.let {
            val updatedTasks = it.tasks.toMutableList()
            if (newName.isBlank()) {
                // Remove the task if the name is empty
                updatedTasks.removeAt(index)
            } else {
                // Update the task name
                val task = updatedTasks[index]
                updatedTasks[index] = task.copy(name = newName)
            }
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    fun getDateStampString() : String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return currentDate
    }
}


