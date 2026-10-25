package com.example.coflow.repositories

import com.example.coflow.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override suspend fun register(
        email: String,
        password: String,
        name: String,
    ): Result<UserModel> =
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Не удалось получить UID")

            usersCollection
                .document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "email" to email,
                        "name" to name,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()

            Result.success(UserModel(id = uid, name = name, email = email))
        } catch (e: Exception) {
            val errorMessage =
                when (e) {
                    is FirebaseAuthWeakPasswordException -> "Пароль слишком простой (минимум 6 символов)"
                    is FirebaseAuthUserCollisionException -> "Пользователь с такой почтой уже существует"
                    else -> e.message ?: "Ошибка регистрации"
                }
            Result.failure(Exception(errorMessage))
        }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserModel> =
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid ?: throw Exception("Не удалось получить UID")

            val doc = usersCollection.document(uid).get().await()
            val user =
                doc.toObject(UserModel::class.java)?.copy(id = doc.id)
                    ?: throw Exception("Профиль пользователя не найден")

            Result.success(user)
        } catch (e: Exception) {
            val errorMessage =
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Неверная почта или пароль"
                    else -> e.message ?: "Ошибка входа"
                }
            Result.failure(Exception(errorMessage))
        }

    override suspend fun signInWithGoogle(idToken: String): Result<UserModel> =
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Не удалось получить пользователя")
            val uid = user.uid

            val email =
                user.email
                    ?: throw Exception("Google-аккаунт не содержит email")
            val name = user.displayName ?: "Пользователь Google"

            // Если пользователь заходит впервые — создаем документ в Firestore
            val userDoc = usersCollection.document(uid).get().await()
            val appUser =
                if (userDoc.exists()) {
                    userDoc.toObject(UserModel::class.java)?.copy(id = userDoc.id)
                        ?: throw Exception("Не удалось прочитать профиль")
                } else {
                    usersCollection
                        .document(uid)
                        .set(
                            mapOf(
                                "uid" to uid,
                                "email" to email,
                                "name" to name,
                                "createdAt" to FieldValue.serverTimestamp(),
                            ),
                        ).await()

                    UserModel(id = uid, name = name, email = email)
                }

            Result.success(appUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка входа через Google"))
        }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUserUid(): String? = auth.currentUser?.uid
}
