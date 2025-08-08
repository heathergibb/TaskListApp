package com.example.tasklistapp.viewmodel

import android.app.Application
import android.util.Log
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

    private val _taskList = MutableStateFlow<TaskList?>(null)
    val taskList: StateFlow<TaskList?> = _taskList

    init {
        loadTaskList()
    }

    fun loadTaskList(fileName: String = "tasklist.json") {
        viewModelScope.launch {
            val loaded = repository.loadTaskList(fileName)
            _taskList.value = loaded ?: TaskList("", getDateStampString(), emptyList())
        }
    }

    fun saveTaskList(fileName: String = "tasklist.json") {
        _taskList.value?.let {
            repository.saveTaskList(it.copy(lastUpdated = getDateStampString()), fileName)
        }
    }

    fun addTask(name: String, fileName: String = "tasklist.json") {
        _taskList.value?.let {
            val updatedTasks = it.tasks + Task(name)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    fun toggleTaskCompletion(index: Int, fileName: String = "tasklist.json") {
        _taskList.value?.let {
            val updatedTasks = it.tasks.toMutableList()
            val task = updatedTasks[index]
            updatedTasks[index] = task.copy(isComplete = !task.isComplete)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = it.copy(tasks = updatedTasks)
            repository.saveTaskList(updatedList, fileName)
        }
    }

    fun deleteTask(index: Int) {
        _taskList.value?.let {
            val updatedTasks = it.tasks.toMutableList()
            updatedTasks.removeAt(index)
            _taskList.value = it.copy(tasks = updatedTasks)
        }
    }

    fun updateTitle(newTitle: String) {
        _taskList.value?.let {
            _taskList.value = it.copy(title = newTitle)
        }
    }

    fun getDateStampString() : String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return currentDate
    }
}


