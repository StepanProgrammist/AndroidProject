package com.example.coflow.ui.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.coflow.R

@Composable
fun CreateTaskScreen(
    state: CreateTaskUiState,
    onBackClick: () -> Unit,
    onCreateTask: (CreateTaskFormData) -> Unit,
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

    BackHandler(onBack = onBackClick)

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {

            CoFlowScreenHeader(
                title = stringResource(R.string.create_task_header),
                onBackClick = { focusManager.clearFocus(); onBackClick() }
            )

            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_name_label), value = state.title, onValueChange = onTitleChange,
                    placeholder = stringResource(R.string.creation_task_name_placeholder)
                )
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_description_label), value = state.description, onValueChange = onDescriptionChange,
                    placeholder = stringResource(R.string.creation_task_description_placeholder), singleLine = false, maxLength = 250
                )

                // Клик сработает по ВСЕМУ полю. Клавиатура не вылезет благодаря readOnly
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_assignee_label), value = state.assignees, onValueChange = onAssigneesChange,
                    placeholder = stringResource(R.string.creation_task_assignee_placeholder), leadingIcon = "person", trailingIcon = "chevron",
                    onClick = { onNavigateToAssignees() }
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_project_label), value = state.project, onValueChange = onProjectChange,
                    placeholder = stringResource(R.string.creation_task_project_placeholder), leadingIcon = "folder", trailingIcon = "chevron",
                    onClick = { onNavigateToProject() }
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_deadline_label), value = state.dueDate, onValueChange = onDueDateChange,
                    placeholder = stringResource(R.string.creation_task_deadline_placeholder), leadingIcon = "calendar", trailingIcon = "chevron",
                    onClick = { onNavigateToDate() },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            CoFlowPrimaryButton(
                text = stringResource(R.string.create_task_button),
                onClick = {
                    focusManager.clearFocus()
                    onCreateTask(
                        CreateTaskFormData(
                            title = state.title.trim(),
                            description = state.description.trim(),
                            assignees = state.assignees.trim(),
                            project = state.project.trim(),
                            dueDate = state.dueDate.trim()
                        )
                    )
                },
                enabled = state.canCreate,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
fun CreateTaskRoute(
    onBackClick: () -> Unit,
    onTaskCreated: (CreateTaskFormData) -> Unit,
    onNavigateToAssignees: () -> Unit,
    onNavigateToProject: () -> Unit,
    onNavigateToDate: () -> Unit,
    viewModel: CreateTaskViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CreateTaskScreen(
        state = state,
        onBackClick = onBackClick,
        onCreateTask = { viewModel.onCreateTask(onTaskCreated) },
        onNavigateToAssignees = onNavigateToAssignees,
        onNavigateToProject = onNavigateToProject,
        onNavigateToDate = onNavigateToDate,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAssigneesChange = viewModel::onAssigneesChange,
        onProjectChange = viewModel::onProjectChange,
        onDueDateChange = viewModel::onDueDateChange
    )
}

@Preview(name = "Создание задачи", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CreateTaskScreenPreview() {
    CoFlowTheme {
        CreateTaskScreen(
            state = CreateTaskUiState(),
            onBackClick = { println("Preview: Назад") },
            onCreateTask = { println("Preview: Создание задачи: $it") },
            onNavigateToAssignees = { println("Preview: Переход к выбору исполнителей") },
            onNavigateToProject = { println("Preview: Переход к выбору проекта") },
            onNavigateToDate = { println("Preview: Переход к выбору даты") },
            onTitleChange = { },
            onDescriptionChange = { },
            onAssigneesChange = { },
            onProjectChange = { },
            onDueDateChange = { }
        )
    }
}
