package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

class CycleDashboardAct : BaseActivity() {
    private lateinit var binding: ActivityCycleDashboardBinding
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCycleDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUserSetup()
        setupNavigation()
        setupBottomNavigation()
        setupButtonClicks()
        loadUserName() // Load user name immediately
    }

    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            binding.welcomeText.text = "User" // Default fallback
            return
        }

        // Try to get user name from Firestore
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "User"
                    binding.welcomeText.text = userName
                } else {
                    // If no user document exists, create one with default name
                    createUserDocument(userId)
                }
            }
            .addOnFailureListener { e ->
                binding.welcomeText.text = "User" // Fallback
            }
    }

    private fun createUserDocument(userId: String) {
        val userData = hashMapOf(
            "name" to "User", // Default name
            "email" to (auth.currentUser?.email ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                binding.welcomeText.text = "User"
            }
            .addOnFailureListener { e ->
                binding.welcomeText.text = "User"
            }
    }

    private fun setupButtonClicks() {
        binding.trackSymptomsButton.setOnClickListener{
            startActivity(Intent(this,SymptomTrackingActivity::class.java))
        }
        // To-Do List Card
        binding.todoListCard.setOnClickListener {
            startActivity(Intent(this, ToDoList::class.java))
        }

        // Articles Card
        binding.articlesCard.setOnClickListener {
            startActivity(Intent(this, Articles::class.java))
        }

        // Profile button
        binding.ivProfileImage.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
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
                // User name is already loaded in loadUserName()

                // Current Cycle Card - Update cycle day
                currentCycleDay.text = "Day ${predictions.currentDay}"

                // Update pregnancy probability in the heart card
                val pregnancyTextView = root.findViewById<android.widget.TextView>(R.id.pregnancyProbability)
                pregnancyTextView?.text = String.format("%.1f%%", predictions.pregnancyProbability)

                // Update the hidden elements for background calculations
                nextPeriodDate.text = predictions.nextPeriodDate
                daysUntilNextPeriod.text = "${predictions.daysUntilNextPeriod} days away"
                fertileWindow.text = predictions.fertileWindow
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying data", Toast.LENGTH_SHORT).show()
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

            // Calculate fertile window (typically days 10-17 for a 28-day cycle)
            // Ovulation occurs around day 14, fertile window is 5 days before + day of ovulation
            val ovulationDay = cycleLength - 14 // Day of ovulation in cycle
            val fertileStartDay = (ovulationDay - 5).coerceAtLeast(1)
            val fertileEndDay = (ovulationDay + 1).coerceAtMost(cycleLength)

            calendar.time = lastPeriod
            calendar.add(Calendar.DAY_OF_YEAR, fertileStartDay - 1)
            val fertileStart = format.format(calendar.time)
            calendar.time = lastPeriod
            calendar.add(Calendar.DAY_OF_YEAR, fertileEndDay - 1)
            val fertileEnd = format.format(calendar.time)

            // Calculate pregnancy probability based on cycle day
            val pregnancyProbability = calculatePregnancyProbability(
                currentDay = currentDay,
                ovulationDay = ovulationDay,
                fertileStartDay = fertileStartDay,
                fertileEndDay = fertileEndDay
            )

            CyclePredictions(
                currentDay = currentDay.coerceIn(1, cycleLength),
                cycleLength = cycleLength,
                nextPeriodDate = nextPeriodDate,
                daysUntilNextPeriod = daysUntil.coerceAtLeast(0),
                fertileWindow = "$fertileStart - $fertileEnd",
                pregnancyProbability = pregnancyProbability
            )
        } catch (e: Exception) {
            // Return default predictions if calculation fails
            CyclePredictions(
                currentDay = 1,
                cycleLength = 28,
                nextPeriodDate = "Not available",
                daysUntilNextPeriod = 0,
                fertileWindow = "Not available",
                pregnancyProbability = 0.0
            )
        }
    }

    private fun calculatePregnancyProbability(
        currentDay: Int,
        ovulationDay: Int,
        fertileStartDay: Int,
        fertileEndDay: Int
    ): Double {
        return when {
            // During menstruation (days 1-5): Very low probability
            currentDay in 1..5 -> 1.0

            // Pre-fertile phase (days 6-9): Low probability
            currentDay in 6..(fertileStartDay - 1) -> 5.0

            // Early fertile window (2-3 days before ovulation): Moderate probability
            currentDay in fertileStartDay..(ovulationDay - 3) -> 15.0

            // Peak fertile days (2 days before ovulation): High probability
            currentDay in (ovulationDay - 2)..(ovulationDay - 1) -> 30.0

            // Ovulation day: Highest probability
            currentDay == ovulationDay -> 33.0

            // Day after ovulation: Still high probability
            currentDay == (ovulationDay + 1) -> 25.0

            // Late fertile window: Decreasing probability
            currentDay in (ovulationDay + 2)..fertileEndDay -> 10.0

            // Post-fertile/luteal phase: Very low probability
            currentDay > fertileEndDay -> 2.0

            // Default
            else -> 1.0
        }
    }

    private fun setupNavigation() {
        binding.apply {
            logoutBtn.setOnClickListener {
                auth.signOut()
                Toast.makeText(this@CycleDashboardAct, "Logged out successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    data class CyclePredictions(
        val currentDay: Int,
        val cycleLength: Int,
        val nextPeriodDate: String,
        val daysUntilNextPeriod: Int,
        val fertileWindow: String,
        val pregnancyProbability: Double // NEW: Added pregnancy probability
    )
}