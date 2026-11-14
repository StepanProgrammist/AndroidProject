package com.example.coflow.models

enum class TaskStatus(
    val code: Int,
) {
    TODO(0),
    IN_PROGRESS(1),
    DONE(2),
    ;

    companion object {
        fun fromInt(value: Int): TaskStatus = entries.firstOrNull { it.code == value } ?: TODO
    }
}
