package com.donation.auraappmarkup

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class LanguageSettings : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        radioGroup = findViewById(R.id.language_radio_group)

        // Load saved language preference
        val savedLanguage = sharedPreferences.getString("app_language", "en") ?: "en"
        setSelectedLanguage(savedLanguage)

        // Handle language selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.radio_english -> "en"
                R.id.radio_afrikaans -> "af"
                R.id.radio_zulu -> "zu"
                R.id.radio_xhosa -> "xh"
                else -> "en"
            }

            saveLanguagePreference(languageCode)
            setAppLocale(languageCode)
        }

        // Back button
        findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            finish()
        }
    }

    private fun setSelectedLanguage(languageCode: String) {
        val radioButtonId = when (languageCode) {
            "en" -> R.id.radio_english
            "af" -> R.id.radio_afrikaans
            "zu" -> R.id.radio_zulu
            "xh" -> R.id.radio_xhosa
            else -> R.id.radio_english
        }
        radioGroup.check(radioButtonId)
    }

    private fun saveLanguagePreference(languageCode: String) {
        sharedPreferences.edit().putString("app_language", languageCode).apply()
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        // For Android 7.0+
        val context = createConfigurationContext(configuration)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Restart the app completely
        restartApp()
    }

    private fun restartApp() {
        // Restart to the main dashboard (or login if not authenticated)
        val targetActivity = if (isUserLoggedIn()) {
            CycleDashboardAct::class.java
        } else {
            LoginActivity::class.java
        }

        val intent = Intent(this, targetActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity() // Important: finish all activities
    }

    private fun isUserLoggedIn(): Boolean {
        // Implement your actual login check logic here
        // This is a placeholder - replace with your actual authentication check
        val authPreferences = getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE)
        return authPreferences.getBoolean("is_logged_in", false)

        // If you don't have auth logic yet, you can temporarily use:
        // return true // Always go to dashboard
        // or
        // return false // Always go to login
    }

    // Override to apply language when activity is created
    override fun attachBaseContext(newBase: Context) {
        val sharedPreferences = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "en") ?: "en"
        val locale = Locale(languageCode)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}