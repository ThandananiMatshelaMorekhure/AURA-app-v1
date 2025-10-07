package com.donation.auraappmarkup

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import com.donation.auraappmarkup.databinding.DialogAddEditTaskBinding
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class AddEditTaskDialog : DialogFragment() {
    private lateinit var binding: DialogAddEditTaskBinding
    private var task: Task? = null
    private var onTaskSaved: ((Task) -> Unit)? = null

    companion object {
        fun newInstance(task: Task? = null, onTaskSaved: (Task) -> Unit): AddEditTaskDialog {
            return AddEditTaskDialog().apply {
                this.task = task
                this.onTaskSaved = onTaskSaved
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditTaskBinding.inflate(layoutInflater)

        setupUI()
        setupClickListeners()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupUI() {
        binding.tvDialogTitle.text = if (task == null) "Add New Task" else "Edit Task"

        task?.let {
            binding.etTaskTitle.setText(it.title)
            binding.etTaskDescription.setText(it.description)
            binding.etDueDate.setText(it.dueDate)
            binding.spPriority.setSelection(it.priority.ordinal)
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            saveTask()
        }

        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Title is required"
            return
        }

        val description = binding.etTaskDescription.text.toString().trim()
        val dueDate = binding.etDueDate.text.toString().trim()
        val priority = TaskPriority.entries[binding.spPriority.selectedItemPosition]

        val savedTask = task?.copy(
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority
        ) ?: Task(
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority
        )

        onTaskSaved?.invoke(savedTask)
        dismiss()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                binding.etDueDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}