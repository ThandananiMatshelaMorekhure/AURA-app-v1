package com.donation.auraappmarkup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutUs : BaseActivity() {

    private val TAG = "AboutUs"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_about_us)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupToolbarNavigation()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            e.printStackTrace()
            finish() // Close activity if there's an error
        }
    }

    private fun setupToolbarNavigation() {
        try {
            val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

            // Set navigation icon click listener
            toolbar?.setNavigationOnClickListener {
                returnToProfile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar: ${e.message}", e)
        }
    }

    private fun returnToProfile() {
        finish() // Simply close this activity, returns to existing Profile
    }

    // Handle system back button press
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Simply close this activity
    }
}