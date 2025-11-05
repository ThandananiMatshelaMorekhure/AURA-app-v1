package com.donation.auraappmarkup

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class LanguageApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Load saved language on app start
        loadLanguagePreference()
    }

    private fun loadLanguagePreference() {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "en") ?: "en"
        setAppLocale(languageCode)
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }
}