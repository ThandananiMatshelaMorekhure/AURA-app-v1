package com.donation.auraappmarkup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donation.auraappmarkup.databinding.ActivityHistoryBinding

class HistoryAdapter(private val entries: List<HistoryActivity.HistoryEntry>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateText)
        val symptomsTextView: TextView = itemView.findViewById(R.id.symptomsText)
        val moodTextView: TextView = itemView.findViewById(R.id.moodText)    // Correct ID
        val flowTextView: TextView = itemView.findViewById(R.id.flowText)     // Correct ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = entries[position]

        holder.dateTextView.text = entry.date
        holder.symptomsTextView.text = entry.symptoms.joinToString(", ")
        holder.moodTextView.text = entry.mood
        holder.flowTextView.text = entry.flow
    }

    override fun getItemCount(): Int = entries.size
}