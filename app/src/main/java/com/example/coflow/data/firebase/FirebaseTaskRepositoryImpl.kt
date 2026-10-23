package com.example.coflow.data.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Реализация репозитория задач для Firestore
class FirebaseTaskRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val taskCollection: CollectionReference = db.collection("Task")

    override suspend fun getById(id: String): Task? {
        val snapshot = taskCollection.document(id).get().await()
        return Task.fromDocument(snapshot)
    }

    override suspend fun getByTitle(title: String): List<Task> {
        val querySnapshot = taskCollection
            .whereEqualTo("title", title)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { Task.fromDocument(it) }
    }

    override suspend fun getByProjectId(id: String): List<Task> {
        val projectRef = db.collection("Project").document(id)
        val querySnapshot = taskCollection
            .whereEqualTo("projectRef", projectRef)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { Task.fromDocument(it) }
    }

    override suspend fun addTask(task: Task): Task {
        return if (task.id.isNotBlank()) {
            taskCollection.document(task.id).set(task.toMap()).await()
            task
        } else {
            val docRef = taskCollection.add(task.toMap()).await()
            task.copy(id = docRef.id)
        }
    }

    override suspend fun deleteTask(id: String) {
        taskCollection.document(id).delete().await()
    }
}
