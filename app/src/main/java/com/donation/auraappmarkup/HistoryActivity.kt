package com.donation.auraappmarkup

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.donation.auraappmarkup.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var symptomRepository: SymptomRepository
    private val auth = FirebaseAuth.getInstance()
    private val tag = "HistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "HistoryActivity created")
        setupRepository()
        setupButtonListeners()
        observeHistory()
    }

    private fun setupRepository() {
        symptomRepository = SymptomRepository()
    }

    private fun setupButtonListeners() {
        binding.addNewEntryButton.setOnClickListener {
            finish() // Go back to symptom tracking
        }

        binding.exportButton.setOnClickListener {
            Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            symptomRepository.getSymptomEntriesSorted().collect { entries ->
                Log.d(tag, "Received ${entries.size} symptom entries")
                displayHistory(entries)
            }
        }
    }

    private fun displayHistory(entries: List<SymptomEntry>) {
        Log.d(tag, "Displaying ${entries.size} entries")

        if (entries.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
            Log.d(tag, "Showing empty state - no data")
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE

            // Convert SymptomEntry to HistoryEntry for the adapter
            val historyEntries = entries.map { symptomEntry ->
                HistoryEntry(
                    date = symptomEntry.date,
                    symptoms = symptomEntry.symptoms,
                    mood = symptomEntry.mood,
                    flow = symptomEntry.flow,
                    notes = symptomEntry.notes
                )
            }

            binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.historyRecyclerView.adapter = HistoryAdapter(historyEntries)
            Log.d(tag, "RecyclerView populated with data")
        }
    }

    data class HistoryEntry(
        val date: String,
        val symptoms: List<String>,
        val mood: String,
        val flow: String,
        val notes: String = ""
    )
}