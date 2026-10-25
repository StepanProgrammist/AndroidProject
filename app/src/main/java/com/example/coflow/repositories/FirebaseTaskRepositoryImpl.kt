package com.example.coflow.repositories

import com.example.coflow.models.TaskModel
import com.example.coflow.models.TaskStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// Реализация репозитория задач для Firestore
class FirebaseTaskRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val taskCollection: CollectionReference = db.collection("Task")

    override suspend fun getAll(): Result<List<TaskModel>> = try {
        Result.success(taskCollection.get().await().documents.mapNotNull { it.toTaskModel() })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задач"))
    }

    override suspend fun getById(id: String): Result<TaskModel> = try {
        val snap = taskCollection.document(id).get().await()
        val task = snap.toTaskModel() ?: throw Exception("Задача не найдена")
        Result.success(task)
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задачи"))
    }

    override suspend fun getByTitle(title: String): Result<List<TaskModel>> = try {
        val qs = taskCollection.whereEqualTo("title", title).get().await()
        Result.success(qs.documents.mapNotNull { it.toTaskModel() })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка поиска задач"))
    }

    override suspend fun getByProjectId(id: String): Result<List<TaskModel>> = try {
        val projectRef = db.collection("Project").document(id)
        val qs = taskCollection.whereEqualTo("projectRef", projectRef).get().await()
        Result.success(qs.documents.mapNotNull { it.toTaskModel() })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задач проекта"))
    }

    override suspend fun addTask(task: TaskModel): Result<TaskModel> = try {
        val docRef = taskCollection.document()          // id генерим заранее
        val toSave = task.copy(id = docRef.id)
        docRef.set(toSave.toFirestoreMap()).await()
        Result.success(toSave)                          // перечитывать не нужно
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка создания задачи"))
    }


    override suspend fun deleteTask(id: String): Result<Unit> = try {
        taskCollection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка удаления задачи"))
    }

    // ---------- маппинг: единственное место, которое знает имена полей ----------

    private fun DocumentSnapshot.toTaskModel(): TaskModel? {
        if (!exists()) return null
        return TaskModel(
            id = id,
            projectId = safeRefId("projectRef"),
            authorId = safeRefId("authorRef"),
            executorId = safeRefId("executorRef"),
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            deadline = getTimestamp("deadline")?.toLocalDateTime()
                ?: LocalDateTime.now(ZoneOffset.UTC),
            status = TaskStatus.fromCode(getLong("status")?.toInt()),
        )
    }

    private fun TaskModel.toFirestoreMap(): Map<String, Any?> = mapOf(
        "projectRef" to projectId?.let { db.collection("Project").document(it) },
        "authorRef" to authorId?.let { db.collection("users").document(it) },
        "executorRef" to executorId?.let { db.collection("users").document(it) },
        "title" to title,
        "description" to description,
        "deadline" to deadline.toTimestamp(),
        "status" to status.code,
    )

    // На случай старых документов, где в поле ref лежит строка вместо ссылки
    private fun DocumentSnapshot.safeRefId(field: String): String? = try {
        getDocumentReference(field)?.id
    } catch (e: Exception) {
        null
    }
}

private fun LocalDateTime.toTimestamp(): Timestamp {
    val instant = toInstant(ZoneOffset.UTC)
    return Timestamp(instant.epochSecond, instant.nano)
}

private fun Timestamp.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoseconds.toLong()), ZoneOffset.UTC)
