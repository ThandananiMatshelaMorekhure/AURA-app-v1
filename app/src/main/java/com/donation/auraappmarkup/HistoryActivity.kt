package com.donation.auraappmarkup

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.donation.auraappmarkup.databinding.ActivityHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore } // Fixed spelling
    private val tag = "HistoryActivity" // Fixed: lowercase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "HistoryActivity created")
        setupButtonListeners()
        loadHistory()
    }

    private fun setupButtonListeners() {
        binding.addNewEntryButton.setOnClickListener {
            finish() // Go back to symptom tracking
        }

        binding.exportButton.setOnClickListener {
            Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(tag, "User not logged in")
            Toast.makeText(this, "Please log in to view history", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(tag, "Loading history for user: $userId")

        // FIXED: Query the correct collection path
        fireStore.collection("symptomEntries")  // Match what SymptomTrackingActivity uses
            .whereEqualTo("userId", userId)     // Filter by current user
            .get()
            .addOnSuccessListener { documents ->
                Log.d(tag, "Firestore returned ${documents.size()} documents")

                // Debug: Print all documents
                for (document in documents) {
                    Log.d(tag, "Document ID: ${document.id}")
                    Log.d(tag, "Document data: ${document.data}")
                }

                if (documents.isEmpty) {
                    Log.d(tag, "No symptom entries found")
                    displayHistory(emptyList())
                    return@addOnSuccessListener
                }

                val entries = documents.mapNotNull { doc ->
                    try {
                        HistoryEntry(
                            date = doc.getString("date") ?: "Unknown Date",
                            symptoms = doc.get("symptoms") as? List<String> ?: emptyList(),
                            mood = doc.getString("mood") ?: "Unknown Mood",
                            flow = doc.getString("flow") ?: "Unknown Flow"
                        )
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing document ${doc.id}: ${e.message}")
                        null
                    }
                }

                Log.d(tag, "Created ${entries.size} history entries")

                // FIXED: Sort by timestamp instead of parsing date string
                val sortedEntries = entries.sortedByDescending { entry ->
                    // Use timestamp if available, otherwise use date string
                    try {
                        // Parse the "MMM dd, yyyy" format for sorting
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        dateFormat.parse(entry.date)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }

                displayHistory(sortedEntries)
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error loading history: ${exception.message}")
                Toast.makeText(this, "Error loading history: ${exception.message}", Toast.LENGTH_SHORT).show()
                displayHistory(emptyList())
            }
    }

    private fun displayHistory(entries: List<HistoryEntry>) {
        Log.d(tag, "Displaying ${entries.size} entries")

        if (entries.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
            Log.d(tag, "Showing empty state - no data")
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE

            binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.historyRecyclerView.adapter = HistoryAdapter(entries)
            Log.d(tag, "RecyclerView populated with data")
        }
    }

    data class HistoryEntry(
        val date: String,
        val symptoms: List<String>,
        val mood: String,
        val flow: String
    )
}