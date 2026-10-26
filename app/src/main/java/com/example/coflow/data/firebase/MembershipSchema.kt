package com.example.coflow.data.firebase

import com.example.coflow.models.Role
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Схема данных для Firestore, описывающая, как документ членства выглядит в базе данных.
 * Использует [DocumentReference] для связей и числовые коды для Enum.
 */

data class MembershipSchema(
    val id: String = "",
    val projectRef: DocumentReference? = null,
    val userRef: DocumentReference? = null,
    val role: Int = Role.READER.code
) {
    fun toMap(): Map<String, Any> = buildMap {
        projectRef?.let { put("projectRef", it) }
        userRef?.let { put("userRef", it) }
        put("role", role.toLong())
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): MembershipSchema? {
            if (!doc.exists()) return null
            return MembershipSchema(
                id = doc.id,
                projectRef = doc.getDocumentReference("projectRef"),
                userRef = doc.getDocumentReference("userRef"),
                role = (doc.getLong("role") ?: 0L).toInt()
            )
        }
    }
}