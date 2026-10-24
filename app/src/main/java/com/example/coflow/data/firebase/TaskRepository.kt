package com.example.coflow.data.firebase

// Интерфейс репозитория для работы с задачами в Firestore
interface TaskRepository {
    suspend fun getAll(): List<Task>
    suspend fun getById(id: String): Task?
    suspend fun getByTitle(title: String): List<Task>
    suspend fun getByProjectId(id: String): List<Task>
    suspend fun addTask(task: Task): Task
    suspend fun deleteTask(id: String)
}
