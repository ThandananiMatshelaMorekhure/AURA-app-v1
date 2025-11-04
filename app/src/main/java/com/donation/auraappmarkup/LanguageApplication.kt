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

        val config = Configuration()
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun attachBaseContext(base: Context) {
        val sharedPreferences = base.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "en") ?: "en"
        val locale = Locale(languageCode)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}