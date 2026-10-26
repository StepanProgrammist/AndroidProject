package com.example.coflow.data.firebase

import com.example.coflow.models.MembershipModel

/**
 * Контракт (интерфейс) для управления данными о членстве в проектах.
 *
 * Все методы возвращают [Result], чтобы UI-слой мог безопасно обрабатывать ошибки сети или базы данных
 * без риска падения приложения (краша). Все методы являются suspend, так как работают с сетью асинхронно.
 */

interface MembershipRepository {
    suspend fun getByUserId(id: String): Result<MembershipModel>
    suspend fun getByProjectId(id: String): Result<List<MembershipModel>>
    suspend fun addMember(member: MembershipModel): Result<MembershipModel>
}