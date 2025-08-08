package com.example.tasklistapp.data

import android.content.Context
import com.example.tasklistapp.model.Task
import com.example.tasklistapp.model.TaskList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TaskListRepository(private val context: Context) {

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

    fun loadTaskList(fileName: String = "tasklist.json"): TaskList? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null

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

    fun listSavedFiles(): List<String> {
        return context.fileList().toList().filter { it.endsWith(".json") }
    }

    fun deleteFile(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}