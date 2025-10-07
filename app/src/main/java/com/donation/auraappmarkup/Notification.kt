package com.donation.auraappmarkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Notification : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noticatifcation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationButtons()
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

    private fun navigateToDashboard() {
        val intent = Intent(this, CycleDashboardAct::class.java) // Updated from Dashboard
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
        // For now, you can navigate to CycleDashboardAct or another relevant activity
        val intent = Intent(this, CycleDashboardAct::class.java)
        startActivity(intent)
        finish()
    }
}