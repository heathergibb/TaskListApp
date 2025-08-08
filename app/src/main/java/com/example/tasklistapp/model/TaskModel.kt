package com.example.tasklistapp.model
data class Task(
    val name: String,
    val isComplete: Boolean = false
)

data class TaskList(
    val title: String,
    val lastUpdated: String,
    val tasks: List<Task>
)
