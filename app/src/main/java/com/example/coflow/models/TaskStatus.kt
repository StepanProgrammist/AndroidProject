package com.example.coflow.models

enum class TaskStatus(val code: Int) {
    TODO(0),
    IN_PROCESS(1),
    DONE(2);

    companion object {
        fun fromCode(value: Int?): TaskStatus =
            entries.firstOrNull { it.code == value } ?: TODO
        }
    }
