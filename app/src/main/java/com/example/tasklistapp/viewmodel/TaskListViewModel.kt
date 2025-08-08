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

/**
 * ViewModel for managing a single task list.
 *
 * Handles loading, updating, and saving the task list using a repository.
 *
 * Currently, supports a single, default task list (tasklist.json), but is designed to scale to handle
 * multiple task lists with their own unique file names. This will be implemented in the future.
 *
 * @param application The application context used to access the file system.
 */
class TaskListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskListRepository(application)

    // Name of the file used to save the current task list.
    private val fileName = "tasklist.json"

    // Backing property for task list state
    private val _taskList = MutableStateFlow<TaskList?>(null)

    // Public read-only state flow for the task list
    val taskList: StateFlow<TaskList?> = _taskList

    /**
     * Loads the task list from storage when the ViewModel is created.
     */
    init {
        loadTaskList()
    }

    /**
     * Loads the task list from local storage.
     * If the file does not exist, a new empty task list is created and saved.
     */
    fun loadTaskList() {
        viewModelScope.launch {
            val loaded = repository.loadTaskList(fileName)
            _taskList.value = loaded ?: TaskList("", getDateStampString(), emptyList())
        }
    }

    /**
     * Adds a new task with the given name to the task list.
     * Saves the updated task list to storage.
     *
     * @param name The name of the new task.
     */
    fun addTask(name: String) {
        _taskList.value?.let {
            val updatedTasks = it.tasks + Task(name)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    /**
     * Toggles the checkbox state of a given task in the task list.
     * Saves the updated task list to storage.
     *
     * @param index The index of the task in the task list.
     */
    fun toggleTaskCompletion(index: Int) {
        _taskList.value?.let {
            val updatedTasks = it.tasks.toMutableList()
            val task = updatedTasks[index]
            updatedTasks[index] = task.copy(isComplete = !task.isComplete)
            val updatedList = it.copy(tasks = updatedTasks, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    /**
     * Updates the title of the task list.
     * Saves the updated task list to storage.
     *
     * @param newTitle The new title for the task list.
     */
    fun updateTitle(newTitle: String) {
        _taskList.value?.let {
            val updatedList = it.copy(title = newTitle, lastUpdated = getDateStampString())
            _taskList.value = updatedList
            repository.saveTaskList(updatedList, fileName)
        }
    }

    /**
     * Updates the name of a given task in the task list.
     * If the new name is empty, delete the task.
     * Saves the updated task list to storage.
     *
     * @param index The index of the task in the task list.
     * @param newName The new name for the task.
     */
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

    /**
     * Generates a string representation of the current date and time.
     *
     * @return A string in the format "yyyy-MM-dd HH:mm".
     */
    fun getDateStampString() : String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return currentDate
    }
}


