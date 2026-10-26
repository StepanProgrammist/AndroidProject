package com.example.coflow.models

/**
 * Доменная модель, представляющая членство пользователя в проекте.
 *
 * Эта модель является "чистой" (не зависит от Firebase) и используется на уровне бизнес-логики (ViewModel).
 * Конвертация в формат базы данных происходит в слое [com.example.coflow.data.firebase.MembershipSchema].
 *
 * @property id Уникальный идентификатор документа в базе.
 * @property projectId ID проекта, к которому относится участник.
 * @property userId ID пользователя.
 * @property role Роль пользователя в этом проекте.
 */

data class MembershipModel(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val role: Role = Role.READER
)