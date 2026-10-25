
package com.example.coflow.ui.tasks

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.coflow.data.Task
import com.example.coflow.data.TasksTestData
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringResource
import com.example.coflow.R
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect

@Composable
fun EditTaskRoute(
    task: Task,
    savedStateHandle: SavedStateHandle,
    onBackClick: () -> Unit,
    onNavigateToAssignees: () -> Unit,
    onNavigateToProject: () -> Unit,
    onNavigateToDate: () -> Unit
) {
    val context = LocalContext.current
    val editViewModel: EditTaskViewModel = viewModel()
    val state by editViewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(task.id) {
        editViewModel.loadTask(task)
    }

    // Получаем выбранную дату
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<String?>("selected_task_date", null)
            .collect { selectedDate ->
                if (selectedDate != null) {
                    editViewModel.onDueDateChange(selectedDate)
                    savedStateHandle.set("selected_task_date", null)
                }
            }
    }

// Получаем выбранный проект
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<String?>("selected_task_project", null)
            .collect { selectedProject ->
                if (selectedProject != null) {
                    editViewModel.onProjectChange(selectedProject)
                    savedStateHandle.set("selected_task_project", null)
                }
            }
    }

// Получаем выбранных исполнителей
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<Array<String>?>("selected_task_assignees", null)
            .collect { selectedAssignees ->
                if (selectedAssignees != null) {
                    editViewModel.onAssigneesChange(
                        selectedAssignees.joinToString(", ")
                    )
                    savedStateHandle.set("selected_task_assignees", null)
                }
            }
    }

    EditTaskScreen(
        state = state,
        onBackClick = onBackClick,
        onSaveClick = {
            val updatedTask = editViewModel.buildUpdatedTask(task)

            if (updatedTask != null) {
                TasksTestData.updateTask(updatedTask)
                Toast.makeText(
                    context,
                    context.getString(R.string.edit_task_saved),
                    Toast.LENGTH_SHORT
                ).show()
                onBackClick()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.edit_task_invalid_date),
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onDeleteClick = {
            showDeleteConfirmation = true
        },
        onNavigateToAssignees = onNavigateToAssignees,
        onNavigateToProject = onNavigateToProject,
        onNavigateToDate = onNavigateToDate,
        onTitleChange = editViewModel::onTitleChange,
        onDescriptionChange = editViewModel::onDescriptionChange,
        onAssigneesChange = editViewModel::onAssigneesChange,
        onProjectChange = editViewModel::onProjectChange,
        onDueDateChange = editViewModel::onDueDateChange
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text(stringResource(R.string.edit_task_delete_dialog_title))            },
            text = {
                Text(stringResource(R.string.edit_task_delete_dialog_message))            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        Toast.makeText(
                            context,
                            "хихи, я не удалю",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                ) {
                    Text(stringResource(R.string.edit_task_delete_confirm))                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.edit_task_delete_cancel))                }
            }
        )
    }
}
