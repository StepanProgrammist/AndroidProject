
package com.example.coflow.ui.tasks

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coflow.R
import com.example.coflow.ui.components.CoFlowGlassTextField
import com.example.coflow.ui.components.CoFlowPrimaryButton
import com.example.coflow.ui.components.CoFlowScreenHeader
import com.example.coflow.ui.theme.CoFlowSpacing
import com.example.coflow.ui.theme.coFlowTasksBackgroundBrush
import com.example.coflow.ui.components.CoFlowEditSaveButton
import com.example.coflow.ui.components.CoFlowEditDeleteButton

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
