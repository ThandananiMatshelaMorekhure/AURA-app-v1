package com.donation.auraappmarkup

import com.google.firebase.database.Exclude

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskFilter {
    ALL, TODO, IN_PROGRESS, COMPLETED
}

data class Task(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var dueDate: String = "",
    var isCompleted: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", TaskPriority.MEDIUM, "", false, 0, 0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "priority" to priority.name,
            "dueDate" to dueDate,
            "isCompleted" to isCompleted,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Task {
            return Task(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                priority = try {
                    TaskPriority.valueOf(map["priority"] as? String ?: "MEDIUM")
                } catch (e: Exception) {
                    TaskPriority.MEDIUM
                },
                dueDate = map["dueDate"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Long ?: 0,
                updatedAt = map["updatedAt"] as? Long ?: 0
            )
        }
    }
}