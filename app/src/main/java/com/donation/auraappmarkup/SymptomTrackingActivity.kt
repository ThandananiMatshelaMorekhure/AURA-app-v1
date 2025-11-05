package com.donation.auraappmarkup

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.donation.auraappmarkup.databinding.ActivitySymptomTrackingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SymptomTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySymptomTrackingBinding
    private lateinit var symptomRepository: SymptomRepository
    private val auth = FirebaseAuth.getInstance()
    private val tag = "SymptomTracking"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "Activity created")
        setupRepository()
        setupUI()
        setupSaveButton()
        checkTodaysEntry()
    }

    private fun setupRepository() {
        symptomRepository = SymptomRepository()
    }

    private fun setupUI() {
        val today = SymptomEntry.getCurrentDate()
        binding.dateText.text = "Today: $today"
        Log.d(tag, "UI setup with date: $today")
    }

    private fun checkTodaysEntry() {
        lifecycleScope.launch {
            try {
                val todaysEntry = symptomRepository.getTodaysEntry()
                if (todaysEntry != null) {
                    // Pre-fill the form with today's existing data
                    prefillForm(todaysEntry)
                    Log.d(tag, "Found existing entry for today")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error checking today's entry: ${e.message}")
            }
        }
    }

    private fun prefillForm(entry: SymptomEntry) {
        // Pre-fill symptoms
        binding.cbCramps.isChecked = entry.symptoms.contains("Cramps")
        binding.cbHeadache.isChecked = entry.symptoms.contains("Headache")
        binding.cbBloating.isChecked = entry.symptoms.contains("Bloating")
        binding.cbTenderBreasts.isChecked = entry.symptoms.contains("Tender Breasts")
        binding.cbAcne.isChecked = entry.symptoms.contains("Acne")
        binding.cbFatigue.isChecked = entry.symptoms.contains("Fatigue")

        // Pre-fill mood
        when (entry.mood) {
            "Happy" -> binding.rbHappy.isChecked = true
            "Neutral" -> binding.rbNeutral.isChecked = true
            "Sad" -> binding.rbSad.isChecked = true
            "Anxious" -> binding.rbAnxious.isChecked = true
        }

        // Pre-fill flow
        when (entry.flow) {
            "Light" -> binding.rbLight.isChecked = true
            "Medium" -> binding.rbMedium.isChecked = true
            "Heavy" -> binding.rbHeavy.isChecked = true
        }

        // Pre-fill notes
        binding.notesEditText.setText(entry.notes)
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            Log.d(tag, "Save button clicked")

            val symptoms = mutableListOf<String>().apply {
                if (binding.cbCramps.isChecked) add("Cramps")
                if (binding.cbHeadache.isChecked) add("Headache")
                if (binding.cbBloating.isChecked) add("Bloating")
                if (binding.cbTenderBreasts.isChecked) add("Tender Breasts")
                if (binding.cbAcne.isChecked) add("Acne")
                if (binding.cbFatigue.isChecked) add("Fatigue")
            }

            val mood = when {
                binding.rbHappy.isChecked -> "Happy"
                binding.rbNeutral.isChecked -> "Neutral"
                binding.rbSad.isChecked -> "Sad"
                binding.rbAnxious.isChecked -> "Anxious"
                else -> "Neutral"
            }

            val flow = when {
                binding.rbLight.isChecked -> "Light"
                binding.rbMedium.isChecked -> "Medium"
                binding.rbHeavy.isChecked -> "Heavy"
                else -> "Medium"
            }

            val notes = binding.notesEditText.text.toString().trim()

            Log.d(tag, "Collected data - Symptoms: $symptoms, Mood: $mood, Flow: $flow, Notes: ${if (notes.isNotEmpty()) "Provided" else "Empty"}")
            saveSymptomEntry(symptoms, mood, flow, notes)
        }
    }

    private fun saveSymptomEntry(symptoms: List<String>, mood: String, flow: String, notes: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(tag, "User not logged in - cannot save")
            Toast.makeText(this, "Please log in to save symptoms", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val today = SymptomEntry.getCurrentDate()

                // Check if we're updating an existing entry or creating new
                val existingEntry = symptomRepository.getTodaysEntry()
                val symptomEntry = if (existingEntry != null) {
                    existingEntry.copy(
                        symptoms = symptoms,
                        mood = mood,
                        flow = flow,
                        notes = notes,
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    SymptomEntry(
                        date = today,
                        symptoms = symptoms,
                        mood = mood,
                        flow = flow,
                        notes = notes,
                        timestamp = System.currentTimeMillis()
                    )
                }

                if (existingEntry != null) {
                    symptomRepository.updateSymptomEntry(symptomEntry)
                    Log.d(tag, "✅ Successfully updated symptom entry in Realtime Database!")
                    Toast.makeText(this@SymptomTrackingActivity, "Symptoms updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    symptomRepository.addSymptomEntry(symptomEntry)
                    Log.d(tag, "✅ Successfully saved symptom entry to Realtime Database!")
                    Toast.makeText(this@SymptomTrackingActivity, "Symptoms saved successfully!", Toast.LENGTH_SHORT).show()
                }

                finish()
            } catch (e: Exception) {
                Log.e(tag, "❌ Save failed: ${e.message}")
                Toast.makeText(this@SymptomTrackingActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}