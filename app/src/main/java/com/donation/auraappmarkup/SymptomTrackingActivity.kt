package com.donation.auraappmarkup

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.donation.auraappmarkup.databinding.ActivitySymptomTrackingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SymptomTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySymptomTrackingBinding
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore } // Fixed: correct spelling
    private val tag = "SymptomTracking" // Fixed: lowercase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "Activity created") // Fixed: use lowercase tag
        setupUI()
        setupSaveButton()
    }

    private fun setupUI() {
        val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        binding.dateText.text = "Today: $today"
        Log.d(tag, "UI setup with date: $today") // Fixed: use lowercase tag
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            Log.d(tag, "Save button clicked") // Fixed: use lowercase tag

            val symptoms = mutableListOf<String>().apply {
                if (binding.cbCramps.isChecked) {
                    add("Cramps")
                    Log.d(tag, "Cramps selected") // Fixed: use lowercase tag
                }
                if (binding.cbHeadache.isChecked) {
                    add("Headache")
                    Log.d(tag, "Headache selected") // Fixed: use lowercase tag
                }
                if (binding.cbBloating.isChecked) {
                    add("Bloating")
                    Log.d(tag, "Bloating selected") // Fixed: use lowercase tag
                }
                if (binding.cbTenderBreasts.isChecked) {
                    add("Tender Breasts")
                    Log.d(tag, "Tender Breasts selected") // Fixed: use lowercase tag
                }
                if (binding.cbAcne.isChecked) {
                    add("Acne")
                    Log.d(tag, "Acne selected") // Fixed: use lowercase tag
                }
                if (binding.cbFatigue.isChecked) {
                    add("Fatigue")
                    Log.d(tag, "Fatigue selected") // Fixed: use lowercase tag
                }
            }

            val mood = when {
                binding.rbHappy.isChecked -> {
                    Log.d(tag, "Mood: Happy") // Fixed: use lowercase tag
                    "Happy"
                }
                binding.rbNeutral.isChecked -> {
                    Log.d(tag, "Mood: Neutral") // Fixed: use lowercase tag
                    "Neutral"
                }
                binding.rbSad.isChecked -> {
                    Log.d(tag, "Mood: Sad") // Fixed: use lowercase tag
                    "Sad"
                }
                binding.rbAnxious.isChecked -> {
                    Log.d(tag, "Mood: Anxious") // Fixed: use lowercase tag
                    "Anxious"
                }
                else -> {
                    Log.d(tag, "Mood: Default (Neutral)") // Fixed: use lowercase tag
                    "Neutral"
                }
            }

            val flow = when {
                binding.rbLight.isChecked -> {
                    Log.d(tag, "Flow: Light") // Fixed: use lowercase tag
                    "Light"
                }
                binding.rbMedium.isChecked -> {
                    Log.d(tag, "Flow: Medium") // Fixed: use lowercase tag
                    "Medium"
                }
                binding.rbHeavy.isChecked -> {
                    Log.d(tag, "Flow: Heavy") // Fixed: use lowercase tag
                    "Heavy"
                }
                else -> {
                    Log.d(tag, "Flow: Default (Medium)") // Fixed: use lowercase tag
                    "Medium"
                }
            }

            // Get notes from the EditText
            val notes = binding.notesEditText.text.toString().trim()
            Log.d(tag, "Notes: $notes") // Fixed: use lowercase tag

            Log.d(tag, "Collected data - Symptoms: $symptoms, Mood: $mood, Flow: $flow, Notes: ${if (notes.isNotEmpty()) "Provided" else "Empty"}") // Fixed: use lowercase tag
            saveSymptomEntry(symptoms, mood, flow, notes)
        }
    }

    private fun saveSymptomEntry(symptoms: List<String>, mood: String, flow: String, notes: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(tag, "User not logged in - cannot save") // Fixed: use lowercase tag
            Toast.makeText(this, "Please log in to save symptoms", Toast.LENGTH_SHORT).show()
            return
        }

        val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        Log.d(tag, "Saving entry for user: $userId, date: $today") // Fixed: use lowercase tag

        // Create entry with notes included
        val entry = mutableMapOf(
            "date" to today,
            "symptoms" to symptoms,
            "mood" to mood,
            "flow" to flow,
            "userId" to userId,  // Add userId to the entry
            "timestamp" to System.currentTimeMillis()  // Add timestamp for sorting
        )

        // Only add notes if they're not empty
        if (notes.isNotEmpty()) {
            entry["notes"] = notes
        }

        Log.d(tag, "Firestore data: $entry") // Fixed: use lowercase tag

        // OPTION 1: Save to direct collection (Recommended - simpler)
        val documentRef = fireStore.collection("symptomEntries")  // Fixed: use fireStore variable
            .document()  // Let Firestore auto-generate document ID

        documentRef.set(entry)
            .addOnSuccessListener {
                Log.d(tag, "✅ Successfully saved to Firestore!") // Fixed: use lowercase tag
                Log.d(tag, "Document path: symptomEntries/${documentRef.id}") // Fixed: use documentRef.id
                Toast.makeText(this, "Symptoms saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "❌ Save failed: ${e.message}") // Fixed: use lowercase tag
                Log.e(tag, "Error details: ${e.localizedMessage}") // Fixed: use lowercase tag
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}