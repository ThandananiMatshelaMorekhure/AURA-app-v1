package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.donation.auraappmarkup.data.AppDatabase
import com.donation.auraappmarkup.databinding.ActivityCycleDashboardBinding
import com.donation.auraappmarkup.db.UserPreferences
import com.donation.auraappmarkup.ui.Articles
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CycleDashboardAct : AppCompatActivity() {
    private lateinit var binding: ActivityCycleDashboardBinding
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCycleDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUserSetup()
        setupNavigation()
        setupBottomNavigation()
        setupButtonClicks() // Added for the plus button
        setupQuickAccessCards() // Added for the new cards
    }

    private fun setupQuickAccessCards() {
        val todoListCard: CardView = findViewById(R.id.todoListCard)
        val articlesCard: CardView = findViewById(R.id.articlesCard)

        todoListCard.setOnClickListener {
            startActivity(Intent(this, ToDoList::class.java))
        }

        articlesCard.setOnClickListener {
            startActivity(Intent(this, Articles::class.java))
        }
    }

    private fun setupButtonClicks() {
//        // Plus button (To Do List) functionality
//        binding.btnTodo.setOnClickListener {
//            val intent = Intent(this, ToDoList::class.java)
//            startActivity(intent)
//        }

        binding.ivProfileImage.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)

        }


        // Add Symptoms card functionality (the + card in mood section)
        // Note: You'll need to add an ID to this card in XML first
        // For example: android:id="@+id/addSymptomsCard"
        // Then uncomment below:
        /*
        binding.addSymptomsCard.setOnClickListener {
            val intent = Intent(this, SymptomTrackingActivity::class.java)
            startActivity(intent)
        }
        */

        // Mood cards click listeners (optional - for tracking mood directly)
        // You'll need to add IDs to your mood cards in XML first
        /*
        binding.sadMoodCard.setOnClickListener {
            trackMood("Sad")
        }
        binding.neutralMoodCard.setOnClickListener {
            trackMood("Neutral")
        }
        binding.happyMoodCard.setOnClickListener {
            trackMood("Happy")
        }
        */
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, SymptomTrackingActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun checkUserSetup() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val localPrefs = db.userPrefsDao().getPreferences(userId)
                if (localPrefs == null) {
                    startActivity(Intent(this@CycleDashboardAct, OnBoardingActivity::class.java))
                    finish()
                } else {
                    displayUserDashboard(localPrefs)
                }
            } catch (e: Exception) {
                Toast.makeText(this@CycleDashboardAct, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserDashboard(prefs: UserPreferences) {
        try {
            val predictions = calculatePredictions(prefs)

            binding.apply {
                // Set default name first
                welcomeText.text = "User" // Default until we fetch from Firestore

                // Get user name from Firestore
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    fetchUserName(userId)
                }

                // Current Cycle Card
                currentCycleDay.text = "Day ${predictions.currentDay}"

                // Update hidden elements
                nextPeriodDate.text = predictions.nextPeriodDate
                daysUntilNextPeriod.text = "${predictions.daysUntilNextPeriod} days away"
                fertileWindow.text = predictions.fertileWindow
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserName(userId: String) {
        fireStore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Try different possible field names for the user's name
                    val userName = when {
                        document.getString("name") != null -> document.getString("name")
                        document.getString("displayName") != null -> document.getString("displayName")
                        document.getString("firstName") != null -> document.getString("firstName")
                        document.getString("username") != null -> document.getString("username")
                        document.getString("email") != null -> {
                            // Extract name from email (e.g., "emily@email.com" -> "Emily")
                            val email = document.getString("email") ?: ""
                            email.substringBefore("@").replaceFirstChar { it.uppercase() }
                        }
                        else -> "User" // Default fallback
                    }

                    binding.welcomeText.text = userName ?: "User"
                    Log.d("CycleDashboardAct", "User name set to: $userName")
                } else {
                    // Document doesn't exist, use email as fallback
                    val userEmail = auth.currentUser?.email
                    val displayName = if (!userEmail.isNullOrEmpty()) {
                        userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                    } else {
                        "User"
                    }
                    binding.welcomeText.text = displayName
                    Log.d("CycleDashboardAct", "User document not found, using: $displayName")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CycleDashboardAct", "Error fetching user name: ${e.message}")
                // Fallback to email if Firestore fails
                val userEmail = auth.currentUser?.email
                val displayName = if (!userEmail.isNullOrEmpty()) {
                    userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                } else {
                    "User"
                }
                binding.welcomeText.text = displayName
            }
    }


    private fun calculatePredictions(prefs: UserPreferences): CyclePredictions {
        return try {
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val lastPeriod = format.parse(prefs.lastPeriod) ?: java.util.Date()

            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()

            // Safe conversion to Int with defaults
            val cycleLength = prefs.cycleLength.toIntOrNull() ?: 28
            val periodDuration = prefs.periodDuration.toIntOrNull() ?: 5

            // Calculate next period
            calendar.time = lastPeriod
            calendar.add(Calendar.DAY_OF_YEAR, cycleLength)
            val nextPeriodDate = format.format(calendar.time)

            // Calculate days until next period
            val diff = calendar.timeInMillis - today.timeInMillis
            val daysUntil = (diff / (24 * 60 * 60 * 1000)).toInt()

            // Calculate current cycle day
            calendar.time = lastPeriod
            val daysSinceLast = ((today.timeInMillis - calendar.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
            val currentDay = (daysSinceLast % cycleLength) + 1

            // Calculate fertile window (approx 10-17 days before next period)
            calendar.time = lastPeriod
            calendar.add(Calendar.DAY_OF_YEAR, cycleLength - 17)
            val fertileStart = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val fertileEnd = format.format(calendar.time)

            CyclePredictions(
                currentDay = currentDay.coerceIn(1, cycleLength),
                cycleLength = cycleLength,
                nextPeriodDate = nextPeriodDate,
                daysUntilNextPeriod = daysUntil.coerceAtLeast(0),
                fertileWindow = "$fertileStart - $fertileEnd"
            )
        } catch (e: Exception) {
            // Return default predictions if calculation fails
            CyclePredictions(
                currentDay = 1,
                cycleLength = 28,
                nextPeriodDate = "Not available",
                daysUntilNextPeriod = 0,
                fertileWindow = "Not available"
            )
        }
    }

    private fun setupNavigation() {
        binding.apply {
            logoutBtn.setOnClickListener {
                auth.signOut()
                Toast.makeText(this@CycleDashboardAct, "Logged out successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }

            // Settings button moved to setupButtonClicks() for better organization
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Optional: Method to track mood directly from dashboard
    private fun trackMood(mood: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

            val moodData = mapOf(
                "mood" to mood,
                "date" to today,
                "timestamp" to System.currentTimeMillis()
            )

            fireStore.collection("users").document(userId)
                .collection("moodEntries")
                .document(today)
                .set(moodData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Mood tracked: $mood", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to track mood", Toast.LENGTH_SHORT).show()
                }
        }
    }

    data class CyclePredictions(
        val currentDay: Int,
        val cycleLength: Int,
        val nextPeriodDate: String,
        val daysUntilNextPeriod: Int,
        val fertileWindow: String
    )
}