package com.donation.auraappmarkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutUs : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbarNavigation()
    }

    private fun setupToolbarNavigation() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        // Set navigation icon click listener
        toolbar.setNavigationOnClickListener {
            returnToProfile()
        }
    }

    private fun returnToProfile() {
        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
        finish() // Close this activity
    }

    // Handle system back button press
    override fun onBackPressed() {
        super.onBackPressed()
        returnToProfile()
    }
}