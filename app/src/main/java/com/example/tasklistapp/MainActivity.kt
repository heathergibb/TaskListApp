package com.example.tasklistapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklistapp.model.Task
import com.example.tasklistapp.ui.theme.AppTheme
import com.example.tasklistapp.viewmodel.TaskListViewModel
import com.example.tasklistapp.viewmodel.TaskListViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskListApp()
        }
    }
}

@Composable
fun TaskListApp() {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: TaskListViewModel = viewModel(
        factory = TaskListViewModelFactory(application)
    )
    val taskListState by viewModel.taskList.collectAsState()

    if (taskListState == null) {
        Text("Loading...")
        return
    }
    val taskList = taskListState!!

    var newTaskText by rememberSaveable { mutableStateOf("") }

    val spacing = AppTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.medium)
    ) {
        TaskListTitle(
            title = taskList.title,
            onTitleChange = viewModel::updateTitle
        )

        Spacer(modifier = Modifier.height(spacing.small))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(taskList.tasks.size) { index ->
                val task = taskList.tasks[index]
                TaskItem(
                    task = task,
                    onTaskNameChange = { newName ->
                        viewModel.updateTaskName(index, newName)
                    },
                    onCheckedChange = { isChecked ->
                        viewModel.toggleTaskCompletion(index)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.small))

        AddTaskInput(
            taskText = newTaskText,
            onTaskTextChange = { newTaskText = it },
            onTaskSubmit = {
                if (newTaskText.isNotBlank()) {
                    viewModel.addTask(newTaskText)
                    newTaskText = ""
                }
            }
        )
    }
}

@Composable
fun TaskListTitle(title: String, onTitleChange: (String) -> Unit) {
    TextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Task List",
                style = TextStyle(
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            ) },
        singleLine = true,
        textStyle = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    )
}

@Composable
fun TaskItem(
    task: Task,
    onTaskNameChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isComplete,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(24.dp)
                .padding(start = 16.dp)
        )

        BasicTextField(
            value = task.name,
            onValueChange = onTaskNameChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
        )
    }
}

@Composable
fun AddTaskInput(taskText: String, onTaskTextChange: (String) -> Unit, onTaskSubmit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = taskText,
            onValueChange = onTaskTextChange,
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(
                    text = "Add a task",
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (taskText.isNotBlank()) {
                        onTaskSubmit()
                    }
                }
            )
        )

        IconButton(
            onClick = {
                if (taskText.isNotBlank()) {
                    onTaskSubmit()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task"
            )
        }
    }
}