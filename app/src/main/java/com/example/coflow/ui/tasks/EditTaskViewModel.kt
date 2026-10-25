
package com.example.coflow.ui.tasks

import androidx.lifecycle.ViewModel
import com.example.coflow.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.format.DateTimeFormatter
import java.time.LocalDate

// Состояние формы редактирования задачи.
data class EditTaskUiState(
    val title: String = "",
    val description: String = "",
    val assignees: String = "",
    val project: String = "",
    val dueDate: String = "",
) {
    // Название задачи обязательно для заполнения.
    val canSave: Boolean
        get() = title.isNotBlank()
}

// ViewModel управляет только состоянием интерфейса редактирования.
class EditTaskViewModel : ViewModel() {

    private val _state = MutableStateFlow(EditTaskUiState())
    val state: StateFlow<EditTaskUiState> = _state.asStateFlow()

    // Преобразуем дату задачи в формат, используемый в форме.
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    // Заполняем форму данными выбранной задачи.
    fun loadTask(task: Task) {
        _state.update {
            it.copy(
                title = task.title,
                description = task.description,
                assignees = task.assignees.joinToString(", "),
                project = task.projectName,
                dueDate = task.dueDate.format(dateFormatter)
            )
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value.take(250)) }
    }

    fun onAssigneesChange(value: String) {
        _state.update { it.copy(assignees = value) }
    }

    fun onProjectChange(value: String) {
        _state.update { it.copy(project = value) }
    }

    fun onDueDateChange(value: String) {
        _state.update { it.copy(dueDate = value) }
    }

    fun buildUpdatedTask(originalTask: Task): Task? {
        val current = _state.value

        val parsedDate = runCatching {
            LocalDate.parse(current.dueDate, dateFormatter)
        }.getOrNull() ?: return null

        return originalTask.copy(
            title = current.title.trim(),
            description = current.description,
            assignees = current.assignees
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
            projectName = current.project,
            dueDate = parsedDate
        )
    }
}
