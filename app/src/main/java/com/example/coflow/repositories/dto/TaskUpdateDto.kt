package com.example.coflow.repositories.dto

import com.example.coflow.models.TaskStatus
import java.time.LocalDateTime

/**
 * DTO для частичного обновления задачи.
 *
 * Поле, оставленное `null`, не меняется. Пустая строка в `projectId`
 * или `authorId` удаляет соответствующую ссылку из документа, а пустой
 * список `assigneeId` удаляет все ссылки на исполнителей.
 */
data class TaskUpdateDto(
    val title: String? = null,
    val description: String? = null,
    val projectId: String? = null,
    val authorId: String? = null,
    val assigneeId: List<String>? = null,
    val deadline: LocalDateTime? = null,
    val status: TaskStatus? = null,
)
