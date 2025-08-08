package com.example.tasklistapp.model

/**
 * Represents a single task in the task list
 *
 * @property name The name of the task.
 * @property isComplete A boolean indicating whether the task is complete or not.
 */
data class Task(
    val name: String,
    val isComplete: Boolean = false
)

/**
 * Represents a entire task list with a title and last updated timestamp.
 *
 * @property title The title of the task list.
 * @property lastUpdated A string representation of the last time this list was updated.
 * @property tasks The list of individual Task objects belonging to this task list.
 */
data class TaskList(
    val title: String,
    val lastUpdated: String, // Not yet implemented, to be used on the main screen when app allows multiple lists
    val tasks: List<Task>
)
