package com.donation.auraappmarkup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    private fun getUserTasksRef() = database.reference
        .child("users")
        .child(getCurrentUserId())
        .child("tasks")

    // Add task to Firebase
    suspend fun addTask(task: Task): String {
        val taskRef = getUserTasksRef().push()
        task.id = taskRef.key ?: throw Exception("Failed to generate task ID")
        task.updatedAt = System.currentTimeMillis()
        taskRef.setValue(task.toMap()).await()
        return task.id
    }

    // Update task in Firebase
    suspend fun updateTask(task: Task): Boolean {
        return try {
            task.updatedAt = System.currentTimeMillis()
            getUserTasksRef().child(task.id).setValue(task.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete task from Firebase
    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            getUserTasksRef().child(taskId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get task by ID (optional - if you need it)
    suspend fun getTaskById(taskId: String): Task? {
        return try {
            val snapshot = getUserTasksRef().child(taskId).get().await()
            val taskMap = snapshot.value as? Map<*, *>
            taskMap?.let { Task.fromMap(it as Map<String, Any?>) }
        } catch (e: Exception) {
            null
        }
    }

    // Get all tasks
    fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val tasksRef = getUserTasksRef()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()
                snapshot.children.forEach { child ->
                    val taskMap = child.value as? Map<*, *>
                    taskMap?.let {
                        val task = Task.fromMap(it as Map<String, Any?>)
                        tasks.add(task)
                    }
                }
                trySend(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        tasksRef.addValueEventListener(valueEventListener)

        awaitClose {
            tasksRef.removeEventListener(valueEventListener)
        }
    }

    // Get tasks by filter
    fun getTasksByFilter(filter: TaskFilter): Flow<List<Task>> {
        return getAllTasks().map { tasks ->
            when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.TODO -> tasks.filter { !it.isCompleted }
                TaskFilter.IN_PROGRESS -> tasks.filter { !it.isCompleted }
                TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            }
        }
    }

    // Toggle task completion
    suspend fun toggleTaskCompletion(taskId: String): Boolean {
        return try {
            val taskRef = getUserTasksRef().child(taskId)
            val snapshot = taskRef.get().await()
            val taskMap = snapshot.value as? Map<*, *> ?: return false
            val task = Task.fromMap(taskMap as Map<String, Any?>)
            task.isCompleted = !task.isCompleted
            task.updatedAt = System.currentTimeMillis()
            taskRef.setValue(task.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}