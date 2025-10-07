package com.donation.auraappmarkup

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit,
    private val onTaskToggle: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks = listOf<Task>()

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_task_description)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        private val cbCompleted: CheckBox = itemView.findViewById(R.id.cb_task_completed)
        private val ivPriority: ImageView = itemView.findViewById(R.id.iv_priority)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_task)

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDescription.visibility = if (task.description.isEmpty()) View.GONE else View.VISIBLE
            tvDueDate.text = task.dueDate
            tvDueDate.visibility = if (task.dueDate.isEmpty()) View.GONE else View.VISIBLE
            cbCompleted.isChecked = task.isCompleted

            // Set priority color
            val priorityColor = when (task.priority) {
                TaskPriority.HIGH -> ContextCompat.getColor(itemView.context, R.color.priority_high)
                TaskPriority.MEDIUM -> ContextCompat.getColor(itemView.context, R.color.priority_medium)
                TaskPriority.LOW -> ContextCompat.getColor(itemView.context, R.color.priority_low)
            }
            ivPriority.setColorFilter(priorityColor)

            // Apply strikethrough for completed tasks
            if (task.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.alpha = 0.6f
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.alpha = 1.0f
            }

            // Set click listeners
            itemView.setOnClickListener { onTaskClick(task) }
            itemView.setOnLongClickListener {
                onTaskLongClick(task)
                true
            }
            cbCompleted.setOnCheckedChangeListener { _, _ -> onTaskToggle(task) }
            btnDelete.setOnClickListener { onDeleteClick(task) }
        }
    }
}