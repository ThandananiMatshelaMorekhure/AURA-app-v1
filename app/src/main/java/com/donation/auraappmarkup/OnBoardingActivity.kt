package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.donation.auraappmarkup.data.AppDatabase
import com.donation.auraappmarkup.db.UserPreferences
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnBoardingActivity : AppCompatActivity() {

    // ✅ Firebase setup
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }

    // ✅ Room DB setup
    private val db by lazy { AppDatabase.getDatabase(this) }

    // UI elements
    private lateinit var etLastPeriod: TextInputEditText
    private lateinit var spCycleLength: AutoCompleteTextView
    private lateinit var spPeriodDuration: AutoCompleteTextView
    private lateinit var spNotifications: AutoCompleteTextView
    private lateinit var btnCompleteSetup: Button
    private lateinit var btnSkip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        // Initialize views using findViewById
        initViews()

        setupDatePicker()
        setupDropdowns()
        setupButtons()
    }

    private fun initViews() {
        etLastPeriod = findViewById(R.id.etLastPeriod)
        spCycleLength = findViewById(R.id.spCycleLength)
        spPeriodDuration = findViewById(R.id.spPeriodDuration)
        spNotifications = findViewById(R.id.spNotifications)
        btnCompleteSetup = findViewById(R.id.btnCompleteSetup)
        btnSkip = findViewById(R.id.btnSkip)
    }

    private fun setupDatePicker() {
        etLastPeriod.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Last Period Date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDate ->
                val dateStr = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    .format(Date(selectedDate))
                etLastPeriod.setText(dateStr)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupDropdowns() {
        // Cycle Length options
        val cycleOptions = listOf("21 days", "25 days", "28 days", "30 days", "35 days")
        val cycleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cycleOptions)
        spCycleLength.setAdapter(cycleAdapter)
        spCycleLength.setOnClickListener { spCycleLength.showDropDown() }

        // Period Duration options
        val durationOptions = listOf("3 days", "4 days", "5 days", "6 days", "7 days")
        val durationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, durationOptions)
        spPeriodDuration.setAdapter(durationAdapter)
        spPeriodDuration.setOnClickListener { spPeriodDuration.showDropDown() }

        // Notifications options
        val notificationOptions = listOf("Daily", "Before period", "No notifications")
        val notifAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, notificationOptions)
        spNotifications.setAdapter(notifAdapter)
        spNotifications.setOnClickListener { spNotifications.showDropDown() }
    }

    private fun setupButtons() {
        btnCompleteSetup.setOnClickListener {
            if (validateInputs()) {
                saveUserPreferences()
            }
        }

        btnSkip.setOnClickListener {
            navigateToMain()
        }
    }

    // ✅ Validate all fields before saving
    private fun validateInputs(): Boolean {
        if (etLastPeriod.text.isNullOrBlank()) {
            Toast.makeText(this, "Please select your last period date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (spCycleLength.text.isNullOrBlank()) {
            Toast.makeText(this, "Please select your cycle length", Toast.LENGTH_SHORT).show()
            return false
        }
        if (spPeriodDuration.text.isNullOrBlank()) {
            Toast.makeText(this, "Please select your period duration", Toast.LENGTH_SHORT).show()
            return false
        }
        if (spNotifications.text.isNullOrBlank()) {
            Toast.makeText(this, "Please select a notification preference", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserPreferences() {
        val cycleData = CyclePreferences(
            lastPeriod = etLastPeriod.text.toString(),
            cycleLength = spCycleLength.text.toString(),
            periodDuration = spPeriodDuration.text.toString(),
            notifications = spNotifications.text.toString()
        )

        saveToFirestore(cycleData)
        saveToLocalDb(cycleData)
    }

    private fun saveToFirestore(data: CyclePreferences) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .set(data)
            .addOnSuccessListener {
                navigateToMain()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Online save failed, using offline mode", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
    }

    private fun saveToLocalDb(data: CyclePreferences) {
        val userId = auth.currentUser?.uid ?: return

        val userPrefs = UserPreferences(
            userId = userId,
            lastPeriod = data.lastPeriod,
            cycleLength = data.cycleLength,
            periodDuration = data.periodDuration
        )

        lifecycleScope.launch {
            db.userPrefsDao().savePreferences(userPrefs)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, CycleDashboardAct::class.java))
        finish()
    }

    // ✅ Firestore-compatible data class
    data class CyclePreferences(
        val lastPeriod: String = "",
        val cycleLength: String = "",
        val periodDuration: String = "",
        val notifications: String = ""
    )
}