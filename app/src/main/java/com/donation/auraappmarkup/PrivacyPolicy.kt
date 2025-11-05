package com.donation.auraappmarkup

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrivacyPolicy : BaseActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_policy)
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
        finish() // Simply close this activity, returns to existing Profile
    }

    // Handle system back button press
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Simply close this activity
    }
}