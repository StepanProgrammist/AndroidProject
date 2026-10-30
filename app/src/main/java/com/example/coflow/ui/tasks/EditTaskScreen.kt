
package com.example.coflow.ui.tasks

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coflow.R
import com.example.coflow.data.Task
import com.example.coflow.ui.components.CoFlowGlassTextField
import com.example.coflow.ui.components.CoFlowScreenHeader
import com.example.coflow.ui.theme.CoFlowSpacing
import com.example.coflow.ui.theme.coFlowTasksBackgroundBrush
import com.example.coflow.ui.components.CoFlowEditSaveButton
import com.example.coflow.ui.components.CoFlowEditDeleteButton
import kotlin.collections.joinToString

@Composable
fun EditTaskScreen(
    state: EditTaskUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateToAssignees: () -> Unit,
    onNavigateToProject: () -> Unit,
    onNavigateToDate: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAssigneesChange: (String) -> Unit,
    onProjectChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    // Возвращаем пользователя на предыдущий экран системной кнопкой «Назад».
    BackHandler(onBack = onBackClick)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(coFlowTasksBackgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = CoFlowSpacing.ScreenHorizontal)
        ) {
            CoFlowScreenHeader(
                title = stringResource(R.string.edit_task_header),
                onBackClick = {
                    focusManager.clearFocus()
                    onBackClick()
                }
            )

            // Поля формы прокручиваются, а кнопки остаются внизу экрана.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_name_label),
                    value = state.title,
                    onValueChange = onTitleChange,
                    placeholder = stringResource(R.string.creation_task_name_placeholder)
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_description_label),
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    placeholder = stringResource(R.string.creation_task_description_placeholder),
                    singleLine = false,
                    maxLength = 250
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_assignee_label),
                    value = state.assignees,
                    onValueChange = onAssigneesChange,
                    placeholder = stringResource(R.string.creation_task_assignee_placeholder),
                    leadingIcon = "person",
                    trailingIcon = "chevron",
                    onClick = onNavigateToAssignees
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_project_label),
                    value = state.project,
                    onValueChange = onProjectChange,
                    placeholder = stringResource(R.string.creation_task_project_placeholder),
                    leadingIcon = "folder",
                    trailingIcon = "chevron",
                    onClick = onNavigateToProject
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_deadline_label),
                    value = state.dueDate,
                    onValueChange = onDueDateChange,
                    placeholder = stringResource(R.string.creation_task_deadline_placeholder),
                    leadingIcon = "calendar",
                    trailingIcon = "chevron",
                    onClick = onNavigateToDate,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(modifier = Modifier.size(4.dp))
            }

            // Интерфейс передаёт действия наружу и сам не работает с БД.

            // Новые кнопки редактирования располагаются рядом.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoFlowEditSaveButton(
                    text = stringResource(R.string.edit_task_save_button),
                    onClick = {
                        focusManager.clearFocus()
                        onSaveClick()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = state.canSave
                )

                CoFlowEditDeleteButton(
                    text = stringResource(R.string.edit_task_delete_button),
                    onClick = {
                        focusManager.clearFocus()
                        onDeleteClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}


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
