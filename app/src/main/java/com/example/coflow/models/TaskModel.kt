package com.example.coflow.models

import java.time.LocalDateTime
import java.time.ZoneOffset

// Доменная модель. Никаких импортов Firebase — это важно.
data class TaskModel(
    val id: String = "",
    val projectId: String? = null,
    val authorId: String? = null,
    val executorId: String? = null,
    val title: String = "",
    val description: String = "",
    val deadline: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val status: TaskStatus = TaskStatus.TODO,
)