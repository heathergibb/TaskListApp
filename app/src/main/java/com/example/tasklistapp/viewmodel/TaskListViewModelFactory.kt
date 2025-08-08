package com.example.tasklistapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class to create an instance of TaskListViewModel with a non-empty constructor.
 * This is required because the TaskListViewModel constructor takes an Application parameter, which
 * can't be handled by the default ViewModelProvider.
 */
class TaskListViewModelFactory(
    // Application instance passed in from Composable
    private val application: Application
) : ViewModelProvider.Factory {

    /**
     * Checks if the ViewModel class is assignable from TaskListViewModel.
     * If so, creates an instance of TaskListViewModel with the provided application.
     *
     * @return An instance of TaskListViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}