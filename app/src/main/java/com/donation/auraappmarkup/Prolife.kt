package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View

class Prolife : AppCompatActivity() {
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
    }

    private fun setupNavigationButtons() {
        // Find the navigation buttons
        val homeButton = findViewById<View>(R.id.ic_home) // Make sure this ID exists in your XML
        val addButton = findViewById<View>(R.id.ic_add)   // Make sure this ID exists in your XML
        val profileButton = findViewById<View>(R.id.ic_profile) // Make sure this ID exists in your XML

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
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
        finish() // Optional: remove if you want to keep this activity in back stack
    }

    private fun navigateToToDoList() {
        val intent = Intent(this, ToDoList::class.java)
        startActivity(intent)
        finish() // Optional: remove if you want to keep this activity in back stack
    }
}