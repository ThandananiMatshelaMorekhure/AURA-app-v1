package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prolife)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up navigation button click listeners
        setupNavigationButtons()

        // Set up profile option click listeners
        setupProfileOptions()
    }

    private fun setupNavigationButtons() {
        // Find the navigation buttons
        val homeButton = findViewById<View>(R.id.ic_home)
        val addButton = findViewById<View>(R.id.ic_add)
        val profileButton = findViewById<View>(R.id.ic_profile)
        val calendarButton = findViewById<View>(R.id.ic_calendar)

        // Set click listeners
        homeButton?.setOnClickListener {
            navigateToDashboard()
        }

        addButton?.setOnClickListener {
            navigateToToDoList()
        }

        profileButton?.setOnClickListener {
            // Already on profile page, so do nothing or refresh
        }

        calendarButton?.setOnClickListener {
            navigateToCalendar()
        }
    }

    private fun setupProfileOptions() {
        // Change Password option - using LinearLayout ID
        val changePasswordOption = findViewById<LinearLayout>(R.id.change_password)
        changePasswordOption?.setOnClickListener {
            navigateToChangePassword()
        }

        // Edit Profile option - using LinearLayout ID
        val editProfileOption = findViewById<LinearLayout>(R.id.edit_profile)
        editProfileOption?.setOnClickListener {
            navigateToEditProfile()
        }

        // Notification option - using LinearLayout ID
        val notificationOption = findViewById<LinearLayout>(R.id.notification)
        notificationOption?.setOnClickListener {
            navigateToNotification()
        }

        // Privacy Policy option - using TextView ID
        val privacyPolicyOption = findViewById<TextView>(R.id.privacy_policy)
        privacyPolicyOption?.setOnClickListener {
            navigateToPrivacyPolicy()
        }

        // About Us option - using TextView ID
        val aboutUsOption = findViewById<TextView>(R.id.about_us)
        aboutUsOption?.setOnClickListener {
            navigateToAboutUs()
        }

//        // Language option - using LinearLayout ID
//        val languageOption = findViewById<LinearLayout>(R.id.language)
//        languageOption?.setOnClickListener {
//            navigateToLanguageSettings()
//        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, CycleDashboardAct::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToToDoList() {
        val intent = Intent(this, ToDoList::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToCalendar() {
        // If you have a Calendar activity, navigate to it
        // val intent = Intent(this, CalendarActivity::class.java)
        // startActivity(intent)
        // For now, show a message or do nothing
    }

    private fun navigateToChangePassword() {
        val intent = Intent(this, PasswordChange::class.java)
        startActivity(intent)
        // Don't finish() so user can come back to profile
    }

    private fun navigateToEditProfile() {
        // Navigate to EditProfile activity
        val intent = Intent(this, EditProfile::class.java)
        intent.putExtra("edit_mode", true)
        startActivity(intent)
    }

    private fun navigateToNotification() {
        val intent = Intent(this, Notification::class.java)
        startActivity(intent)
    }

    private fun navigateToPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicy::class.java)
        startActivity(intent)
    }

    private fun navigateToAboutUs() {
        // Navigate to AboutUs activity or show dialog
         val intent = Intent(this, AboutUs::class.java)
         startActivity(intent)
    }

    private fun navigateToLanguageSettings() {
        // Navigate to Language settings activity or show dialog
        // val intent = Intent(this, LanguageSettings::class.java)
        // startActivity(intent)
    }

    // Handle back button press
    override fun onBackPressed() {
        super.onBackPressed() // Added super call to fix the warning
        navigateToDashboard()
    }
}