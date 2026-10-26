package com.example.coflow.data.firebase

import com.example.coflow.models.MembershipModel
import com.example.coflow.models.Role
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Реализация контракта [MembershipRepository] с использованием Firebase Firestore.
 *
 * @param db Экземпляр базы данных. Передается через конструктор для возможности подмены (Mock) в тестах.
 */

class FirebaseMembershipRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MembershipRepository {

    private val membershipCollection: CollectionReference = db.collection("Membership")
    private val projectCollection: CollectionReference = db.collection("Project")
    private val userCollection: CollectionReference = db.collection("users")

    override suspend fun getByUserId(id: String): Result<MembershipModel> = try {
        val userRef = userCollection.document(id)
        val qs = membershipCollection.whereEqualTo("userRef", userRef).get().await()
        val doc = qs.documents.firstOrNull()
            ?: throw Exception("Членство не найдено")
        val schema = MembershipSchema.fromDocument(doc)
            ?: throw Exception("Ошибка чтения членства")
        Result.success(schema.toDomain())
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки членства"))
    }

    override suspend fun getByProjectId(id: String): Result<List<MembershipModel>> = try {
        val projectRef = projectCollection.document(id)
        val qs = membershipCollection.whereEqualTo("projectRef", projectRef).get().await()
        Result.success(qs.documents.mapNotNull { MembershipSchema.fromDocument(it)?.toDomain() })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки участников проекта"))
    }

    override suspend fun addMember(member: MembershipModel): Result<MembershipModel> = try {
        val docRef = membershipCollection.document()
        val schema = member.toSchema(db).copy(id = docRef.id)
        docRef.set(schema.toMap()).await()
        val created = docRef.get().await()
            .toObject(MembershipSchema::class.java)
            ?: throw Exception("Не удалось прочитать созданное членство")
        Result.success(created.toDomain())
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка добавления участника"))
    }

    // --- Мапперы (можно вынести в Mappers.kt, но для удобства оставим здесь) ---
    private fun MembershipSchema.toDomain(): MembershipModel = MembershipModel(
        id = id,
        projectId = projectRef?.id ?: "",
        userId = userRef?.id ?: "",
        role = Role.fromInt(role)
    )

    private fun MembershipModel.toSchema(db: FirebaseFirestore): MembershipSchema = MembershipSchema(
        id = id,
        projectRef = projectId.takeIf { it.isNotBlank() }
            ?.let { db.collection("Project").document(it) },
        userRef = userId.takeIf { it.isNotBlank() }
            ?.let { db.collection("users").document(it) },
        role = role.code
    )
}