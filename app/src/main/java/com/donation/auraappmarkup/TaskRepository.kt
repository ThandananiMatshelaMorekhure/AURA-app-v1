package com.donation.auraappmarkup

class TaskRepository {
    private val tasks = mutableListOf<Task>()

    fun addTask(task: Task): Long {
        tasks.add(task)
        return task.id
    }

    fun updateTask(updatedTask: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        return if (index != -1) {
            tasks[index] = updatedTask
            true
        } else false
    }

    fun deleteTask(taskId: Long): Boolean {
        return tasks.removeIf { it.id == taskId }
    }

    fun getTaskById(taskId: Long): Task? {
        return tasks.find { it.id == taskId }
    }

    fun getAllTasks(): List<Task> = tasks.toList()

    fun getTasksByFilter(filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODO -> tasks.filter { !it.isCompleted }
            TaskFilter.IN_PROGRESS -> tasks.filter { !it.isCompleted && it.description.isNotEmpty() }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    fun toggleTaskCompletion(taskId: Long): Boolean {
        val task = getTaskById(taskId)
        return if (task != null) {
            task.isCompleted = !task.isCompleted
            true
        } else false
    }
}