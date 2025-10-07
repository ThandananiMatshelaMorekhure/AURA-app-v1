package com.donation.auraappmarkup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.donation.auraappmarkup.databinding.ActivityToDoListBinding

class ToDoList : AppCompatActivity() {

    private lateinit var binding: ActivityToDoListBinding
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private var currentFilter = TaskFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupRecyclerView()
        setupClickListeners()
        setupFilterButtons()
        loadTasks()

        // Add some sample data
        addSampleTasks()
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
        loadTasks()
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

    private fun loadTasks() {
        val tasks = taskRepository.getTasksByFilter(currentFilter)
        taskAdapter.updateTasks(tasks)
    }

    private fun showAddTaskDialog() {
        AddEditTaskDialog.newInstance { task ->
            addTask(task)
        }.show(supportFragmentManager, "AddTaskDialog")
    }

    private fun addTask(task: Task) {
        taskRepository.addTask(task)
        loadTasks()
        Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun editTask(task: Task) {
        AddEditTaskDialog.newInstance(task) { updatedTask ->
            updateTask(updatedTask)
        }.show(supportFragmentManager, "EditTaskDialog")
    }

    private fun updateTask(task: Task) {
        if (taskRepository.updateTask(task)) {
            loadTasks()
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTaskCompletion(task: Task) {
        if (taskRepository.toggleTaskCompletion(task.id)) {
            loadTasks()
        }
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                if (taskRepository.deleteTask(task.id)) {
                    loadTasks()
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show()
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

    private fun addSampleTasks() {
        val sampleTasks = listOf(
            Task(
                title = "Track period symptoms",
                description = "Log flow, cramps, and energy levels in app",
                priority = TaskPriority.HIGH,
                dueDate = "25/05/2024"
            ),
            Task(
                title = "Restock menstrual supplies",
                description = "Buy pads, tampons, or menstrual cup",
                priority = TaskPriority.MEDIUM,
                dueDate = "26/05/2024"
            ),
            Task(
                title = "Schedule gynecologist appointment",
                description = "Annual checkup and pap smear",
                priority = TaskPriority.HIGH,
                dueDate = "27/05/2024"
            ),
            Task(
                title = "Prepare heating pad and comfort items",
                description = "Have pain relief ready for next cycle",
                priority = TaskPriority.MEDIUM
            ),
            Task(
                title = "Take iron supplement",
                description = "Daily vitamin during and after period",
                priority = TaskPriority.MEDIUM
            ),
            Task(
                title = "Gentle yoga session",
                description = "20-minute flow for cramp relief",
                priority = TaskPriority.LOW,
                isCompleted = true
            )
        )

        sampleTasks.forEach { task ->
            taskRepository.addTask(task)
        }
        loadTasks()
    }
}