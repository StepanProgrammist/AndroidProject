package com.example.coflow.ui.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.coflow.R

data class CreateTaskFormData(
    val title: String, val description: String, val assignees: String,
    val project: String, val dueDate: String
)

@Composable
fun CreateTaskScreen(
    onBackClick: () -> Unit,
    onCreateTask: (CreateTaskFormData) -> Unit,
    onNavigateToAssignees: () -> Unit,
    onNavigateToProject: () -> Unit,
    onNavigateToDate: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var assignees by rememberSaveable { mutableStateOf("") }
    var project by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val canCreate = title.isNotBlank()

    BackHandler(onBack = onBackClick)

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {

            CoFlowScreenHeader(
                title = stringResource(R.string.create_task_header),
                onBackClick = { focusManager.clearFocus(); onBackClick() }
            )

            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_name_label), value = title, onValueChange = { title = it },
                    placeholder = stringResource(R.string.creation_task_name_placeholder)
                )
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_description_label), value = description, onValueChange = { description = it.take(250) },
                    placeholder = stringResource(R.string.creation_task_description_placeholder), singleLine = false, maxLength = 250
                )

                // Клик сработает по ВСЕМУ полю. Клавиатура не вылезет благодаря readOnly
                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_assignee_label), value = assignees, onValueChange = { assignees = it },
                    placeholder = stringResource(R.string.creation_task_assignee_placeholder), leadingIcon = "person", trailingIcon = "chevron",
                    onClick = { onNavigateToAssignees() }
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_project_label), value = project, onValueChange = { project = it },
                    placeholder = stringResource(R.string.creation_task_project_placeholder), leadingIcon = "folder", trailingIcon = "chevron",
                    onClick = { onNavigateToProject() }
                )

                CoFlowGlassTextField(
                    label = stringResource(R.string.creation_task_deadline_label), value = dueDate, onValueChange = { dueDate = it },
                    placeholder = stringResource(R.string.creation_task_deadline_placeholder), leadingIcon = "calendar", trailingIcon = "chevron",
                    onClick = { onNavigateToDate() },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            CoFlowPrimaryButton(
                text = stringResource(R.string.create_task_button),
                onClick = {
                    focusManager.clearFocus()
                    onCreateTask(CreateTaskFormData(title.trim(), description.trim(), assignees.trim(), project.trim(), dueDate.trim()))
                },
                enabled = canCreate,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}

@Preview(name = "Создание задачи", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CreateTaskScreenPreview() {
    CoFlowTheme {
        CreateTaskScreen(
            onBackClick = { println("Preview: Назад") },
            onCreateTask = { println("Preview: Создание задачи: $it") },
            onNavigateToAssignees = { println("Preview: Переход к выбору исполнителей") },
            onNavigateToProject = { println("Preview: Переход к выбору проекта") },
            onNavigateToDate = { println("Preview: Переход к выбору даты") }
        )
    }
}