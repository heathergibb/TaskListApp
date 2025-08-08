package com.example.tasklistapp.data

import android.content.Context
import com.example.tasklistapp.model.Task
import com.example.tasklistapp.model.TaskList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Handles saving and loading of task lists to and from a local storage as a JSON file.
 *
 * Currently, supports a single, default task list (tasklist.json), but is designed to scale to handle
 * multiple task lists with their own unique file names. This will be implemented in the future.
 *
 * @param context The application context used to access the file system.
 */
class TaskListRepository(private val context: Context) {

    /**
     * Saves a TaskList to local storage as a JSON file.
     *
     * @param taskList The TaskList to be saved.
     * @param fileName The name of the file to save the TaskList to (defaults to "tasklist.json").
     */
    fun saveTaskList(taskList: TaskList, fileName: String = "tasklist.json") {
        val jsonObject = JSONObject().apply {
            put("title", taskList.title)
            put("lastUpdated", taskList.lastUpdated)

            val tasksArray = JSONArray()
            for (task in taskList.tasks) {
                val taskObject = JSONObject().apply {
                    put("name", task.name)
                    put("isComplete", task.isComplete)
                }
                tasksArray.put(taskObject)
            }
            put("tasks", tasksArray)
        }
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(jsonObject.toString().toByteArray())
        }
    }

    /**
     * Loads a TaskList from local storage as a JSON file.
     *
     * @param fileName The name of the file to load (defaults to "tasklist.json").
     * @return The loaded TaskList, or null if the file does not exist.
     */
    fun loadTaskList(fileName: String = "tasklist.json"): TaskList? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null // if null, a new file will need to be created in the calling function.

        val jsonString = file.readText()
        val jsonObject = JSONObject(jsonString)

        val title = jsonObject.getString("title")
        val lastUpdated = jsonObject.getString("lastUpdated")
        val tasksArray = jsonObject.getJSONArray("tasks")

        val tasks = mutableListOf<Task>()
        for (i in 0 until tasksArray.length()) {
            val taskObject = tasksArray.getJSONObject(i)
            val name = taskObject.getString("name")
            val isComplete = taskObject.getBoolean("isComplete")
            tasks.add(Task(name, isComplete))
        }

        return TaskList(title, lastUpdated, tasks)
    }

    /**
     * Lists all the saved files in the local storage directory.
     * The functionality is not yet implemented.
     * It will eventually allow the app to display multiple task lists to choose from.
     */
    fun listSavedFiles(): List<String> {
        return context.fileList().toList().filter { it.endsWith(".json") }
    }

    /**
     * Deletes a file from local storage.
     * The functionality is not yet implemented.
     * It will eventually allow the user to delete unwanted lists from the app.
     */
    fun deleteFile(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}