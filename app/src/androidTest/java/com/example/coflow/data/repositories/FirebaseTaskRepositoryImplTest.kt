package com.example.coflow.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coflow.models.TaskModel
import com.example.coflow.models.TaskStatus
import com.example.coflow.repositories.FirebaseTaskRepositoryImpl
import com.example.coflow.repositories.TaskRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class FirebaseTaskRepositoryImplTest {

    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        repository = FirebaseTaskRepositoryImpl()
    }

    @Test
    fun testTaskRepositoryWorkflow_Success() = runBlocking {
        val testProjectId = "test_project_${System.currentTimeMillis()}"

        val newTask = TaskModel(
            projectId = testProjectId,
            title = "Тестовая задача Firestore",
            description = "Описание задачи для интеграционного теста",
            deadline = LocalDateTime.now(ZoneOffset.UTC),
            status = TaskStatus.IN_PROСESS,
        )

        var createdTaskId: String? = null

        try {
            // 1. Тестируем добавление задачи
            val addedTask = repository.addTask(newTask).getOrThrow()
            createdTaskId = addedTask.id
            assertTrue("ID созданной задачи должен быть заполнен", createdTaskId.isNotBlank())
            assertEquals("Тестовая задача Firestore", addedTask.title)

            // 2. Тестируем получение по ID
            val fetchedById = repository.getById(createdTaskId).getOrNull()
            assertNotNull("Задача должна существовать в БД по ID", fetchedById)
            assertEquals("Тестовая задача Firestore", fetchedById?.title)
            assertEquals(TaskStatus.IN_PROСESS, fetchedById?.status)
            assertEquals(testProjectId, fetchedById?.projectId)

            // 3. Тестируем получение по названию
            val tasksByTitle = repository.getByTitle("Тестовая задача Firestore").getOrThrow()
            assertTrue("Должна найтись хотя бы одна задача по заголовку", tasksByTitle.isNotEmpty())
            assertTrue("Созданная задача должна присутствовать в результатах", tasksByTitle.any { it.id == createdTaskId })

            // 4. Тестируем получение по ID проекта
            val tasksByProject = repository.getByProjectId(testProjectId).getOrThrow()
            assertTrue("Должна найтись задача по ID проекта", tasksByProject.isNotEmpty())
            assertTrue("Задача с нужным ID должна быть найдена", tasksByProject.any { it.id == createdTaskId })
        } finally {
            // 5. Очистка созданной задачи в БД после окончания теста
            if (!createdTaskId.isNullOrBlank()) {
                repository.deleteTask(createdTaskId)
                val checkDeleted = repository.getById(createdTaskId).getOrNull()
                assertNull("Задача должна быть удалена из Firestore", checkDeleted)
            }
        }
    }
}