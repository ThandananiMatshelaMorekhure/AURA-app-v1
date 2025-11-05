package com.donation.auraappmarkup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.donation.auraappmarkup.databinding.ActivityToDoListBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ToDoList : BaseActivity() {

    private lateinit var binding: ActivityToDoListBinding
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private var currentFilter = TaskFilter.ALL
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is authenticated
        if (auth.currentUser == null) {
            Toast.makeText(this, "Please sign in to use the todo list", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRepository()
        setupRecyclerView()
        setupClickListeners()
        setupFilterButtons()
        observeTasks()

        // Remove sample tasks since we're using Firebase now
        // addSampleTasks()
    }

    private fun setupRepository() {
        taskRepository = TaskRepository()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> editTask(task) },
            onTaskLongClick = { task -> showTaskOptions(task) },
            onTaskToggle = { task -> toggleTaskCompletion(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )

        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@ToDoList)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            // Handle notifications
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilterButtons() {
        binding.btnAll.setOnClickListener { applyFilter(TaskFilter.ALL) }
        binding.btnTodo.setOnClickListener { applyFilter(TaskFilter.TODO) }
        binding.btnInProgress.setOnClickListener { applyFilter(TaskFilter.IN_PROGRESS) }

        // Set initial filter
        updateFilterButtonsUI(TaskFilter.ALL)
    }

    private fun applyFilter(filter: TaskFilter) {
        currentFilter = filter
        updateFilterButtonsUI(filter)
        observeTasks()
    }

    private fun updateFilterButtonsUI(selectedFilter: TaskFilter) {
        // Reset all buttons
        listOf(binding.btnAll, binding.btnTodo, binding.btnInProgress).forEach { button ->
            button.setBackgroundResource(R.drawable.filter_unselected_bg)
            button.setTextColor(ContextCompat.getColor(this, R.color.filter_unselected_text))
        }

        // Highlight selected button
        val selectedButton = when (selectedFilter) {
            TaskFilter.ALL -> binding.btnAll
            TaskFilter.TODO -> binding.btnTodo
            TaskFilter.IN_PROGRESS -> binding.btnInProgress
            else -> binding.btnAll
        }

        selectedButton.setBackgroundResource(R.drawable.filter_selected_bg)
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.filter_selected_text))
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            taskRepository.getTasksByFilter(currentFilter).collect { tasks ->
                taskAdapter.updateTasks(tasks)
            }
        }
    }

    private fun showAddTaskDialog() {
        AddEditTaskDialog.newInstance { task ->
            addTask(task)
        }.show(supportFragmentManager, "AddTaskDialog")
    }

    private fun addTask(task: Task) {
        lifecycleScope.launch {
            try {
                taskRepository.addTask(task)
                Toast.makeText(this@ToDoList, "Task added successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ToDoList, "Failed to add task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editTask(task: Task) {
        AddEditTaskDialog.newInstance(task) { updatedTask ->
            updateTask(updatedTask)
        }.show(supportFragmentManager, "EditTaskDialog")
    }

    private fun updateTask(task: Task) {
        lifecycleScope.launch {
            if (taskRepository.updateTask(task)) {
                Toast.makeText(this@ToDoList, "Task updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ToDoList, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleTaskCompletion(task: Task) {
        lifecycleScope.launch {
            taskRepository.toggleTaskCompletion(task.id)
        }
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    if (taskRepository.deleteTask(task.id)) {
                        Toast.makeText(this@ToDoList, "Task deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ToDoList, "Failed to delete task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaskOptions(task: Task) {
        val options = arrayOf("Edit", "Delete", "Toggle Completion")
        AlertDialog.Builder(this)
            .setTitle("Task Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editTask(task)
                    1 -> deleteTask(task)
                    2 -> toggleTaskCompletion(task)
                }
            }
            .show()
    }
}