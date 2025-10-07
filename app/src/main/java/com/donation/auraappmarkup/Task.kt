package com.donation.auraappmarkup

data class Task(
    val id: Long = System.currentTimeMillis(),
    var title: String,
    var description: String = "",
    var isCompleted: Boolean = false,
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskPriority { LOW, MEDIUM, HIGH }
enum class TaskFilter { ALL, TODO, IN_PROGRESS, COMPLETED }