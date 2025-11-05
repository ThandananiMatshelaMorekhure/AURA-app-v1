package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : BaseActivity() {
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
            // Optionally refresh profile data if needed
        }

        calendarButton?.setOnClickListener {
            navigateToCalendar()
        }
    }

    private fun setupProfileOptions() {
        // Change Password option
        val changePasswordOption = findViewById<LinearLayout>(R.id.change_password)
        changePasswordOption?.setOnClickListener {
            navigateToChangePassword()
        }

        // Edit Profile option
        val editProfileOption = findViewById<LinearLayout>(R.id.edit_profile)
        editProfileOption?.setOnClickListener {
            navigateToEditProfile()
        }

        // Notification option
        val notificationOption = findViewById<LinearLayout>(R.id.notification)
        notificationOption?.setOnClickListener {
            navigateToNotification()
        }

        // Privacy Policy option - NOW CHANGED TO LinearLayout
        val privacyPolicyOption = findViewById<LinearLayout>(R.id.privacy_policy)
        privacyPolicyOption?.setOnClickListener {
            navigateToPrivacyPolicy()
        }

        // About Us option - NOW CHANGED TO LinearLayout
        val aboutUsOption = findViewById<LinearLayout>(R.id.about_us)
        aboutUsOption?.setOnClickListener {
            navigateToAboutUs()
        }

        // Language option
        val languageOption = findViewById<LinearLayout>(R.id.language)
        languageOption?.setOnClickListener {
            navigateToLanguageSettings()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, CycleDashboardAct::class.java)
        startActivity(intent)
        finish() // Finish profile since we're going to dashboard
    }

    private fun navigateToToDoList() {
        val intent = Intent(this, ToDoList::class.java)
        startActivity(intent)
        finish() // Finish profile since we're going to todo list
    }

    private fun navigateToCalendar() {
        // If you have a Calendar activity, navigate to it
        // val intent = Intent(this, CalendarActivity::class.java)
        // startActivity(intent)
        // For now, show a message or do nothing
        // Note: Remove finish() here since we don't have calendar yet
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
        // Don't finish() so user can come back to profile
    }

    private fun navigateToNotification() {
        val intent = Intent(this, Notification::class.java)
        startActivity(intent)
        // Don't finish() so user can come back to profile
    }

    private fun navigateToPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicy::class.java)
        startActivity(intent)
        // Don't finish() so user can come back to profile
    }

    private fun navigateToAboutUs() {
        // Navigate to AboutUs activity
        val intent = Intent(this, AboutUs::class.java)
        startActivity(intent)
        // Don't finish() so user can come back to profile
    }

    private fun navigateToLanguageSettings() {
        // Navigate to Language settings activity
        val intent = Intent(this, LanguageSettings::class.java)
        startActivity(intent)
        // Don't finish() so user can come back to profile
    }

    // Handle back button press - go to dashboard
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToDashboard()
    }
}