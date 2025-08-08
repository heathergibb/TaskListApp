package com.example.tasklistapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklistapp.model.Task
import com.example.tasklistapp.ui.theme.AppTheme
import com.example.tasklistapp.viewmodel.TaskListViewModel
import com.example.tasklistapp.viewmodel.TaskListViewModelFactory

/**
 * Entry point for app.
 * Sets up the UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskListApp() // Composable function to set up the UI.
        }
    }
}

/**
 * Builds the main UI for the app.
 * It displays the title, a list of tasks, and an input field to add new tasks.
 */
@Composable
fun TaskListApp() {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: TaskListViewModel = viewModel(factory = TaskListViewModelFactory(application))
    val taskListState by viewModel.taskList.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Display a loading message, while the task list is being loaded from storage.
    if (taskListState == null) {
        Text("Loading...")
        return
    }

    val taskList = taskListState!! // Unwrap the nullable TaskList object.
    // rememberSaveable survives configuration changes like screen rotation.
    var newTaskText by rememberSaveable { mutableStateOf("") }
    val spacing = AppTheme.spacing // Access spacing values from the AppTheme.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.medium)
        ) {
            // Display the editable title at the top of the app.
            // If the title is empty, display a placeholder.
            // Title is fixed at the top of the screen, so it doesn't scroll.
            TaskListTitle(
                title = taskList.title,
                onTitleChange = viewModel::updateTitle
            )

            Spacer(modifier = Modifier.height(spacing.small))

            // Display the list of tasks using a LazyColumn.
            // Task list is scrollable.
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

                item {
                    Spacer(modifier = Modifier.height(spacing.small))
                    Text(
                        text = "Last Updated: ${taskList.lastUpdated}",
                        style = TextStyle(
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.small)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            // Display the input field for adding new tasks.
            // Until the input field is filled, display a placeholder.
            // Input field is at the bottom of the screen, so it doesn't scroll.
            AddTaskInput(
                taskText = newTaskText,
                onTaskTextChange = { newTaskText = it },
                onTaskSubmit = {
                    if (newTaskText.isNotBlank()) {
                        viewModel.addTask(newTaskText)
                        newTaskText = "" // Clear the input field after adding the task.
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
            )
        }
    }
}

/**
 * Displays and editable text field for the title of the task list.
 *
 * @param title The current title of the task list.
 * @param onTitleChange A callback that is triggered when the title is changed.
 */
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

/**
 * Displays a single task row with a checkbox and a text field for editing the task name.
 *
 * @param task The Task object representing the task, including name and checkbox status.
 * @param onTaskNameChange A callback that is triggered when the task name is changed.
 * @param onCheckedChange A callback that is triggered when the checkbox status is changed.
 */
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

        val keyboardController = LocalSoftwareKeyboardController.current

        BasicTextField(
            value = task.name,
            onValueChange = {
                // Prevent line breaks
                newValue -> onTaskNameChange(newValue.replace("\n", ""))
            },
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            maxLines = Int.MAX_VALUE, // allow text wrapping
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done // shows a checkmark or Done on the keyboard
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide() // hide the keyboard
                }
            )
        )
    }
}

/**
 * Displays an input field for adding new tasks.
 * Submission occurs when the user presses the "+" button or clicks "Done" on the keyboard.
 *
 * @param taskText The current text in the input field.
 * @param onTaskTextChange A callback that is triggered when the input field is changed.
 * @param onTaskSubmit A callback that is triggered when the user submits the task.
 */
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
                    onTaskSubmit() // Add the task and clear the input field.
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