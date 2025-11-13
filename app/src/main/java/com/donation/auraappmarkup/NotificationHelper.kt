package com.donation.auraappmarkup.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val PERMISSION_REQUEST_CODE = 1001

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for Android 12 and below
        }
    }

    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Get FCM token and save it
     */
    fun getFCMToken(context: Context, onTokenReceived: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // Save token to SharedPreferences
            saveTokenToPreferences(context, token)

            onTokenReceived(token)
        }
    }

    /**
     * Subscribe to notification topics
     */
    fun subscribeToTopics() {
        // Subscribe to general topics
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to all_users topic")
                } else {
                    Log.w(TAG, "Failed to subscribe to all_users", task.exception)
                }
            }

        // Subscribe to cycle reminders
        FirebaseMessaging.getInstance().subscribeToTopic("cycle_reminders")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to cycle_reminders topic")
                }
            }

        // Subscribe to health tips
        FirebaseMessaging.getInstance().subscribeToTopic("health_tips")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to health_tips topic")
                }
            }
    }

    /**
     * Unsubscribe from notification topics
     */
    fun unsubscribeFromTopics() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("cycle_reminders")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("health_tips")
        Log.d(TAG, "Unsubscribed from all topics")
    }

    /**
     * Save FCM token to SharedPreferences
     */
    private fun saveTokenToPreferences(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("AuraPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("fcm_token", token).apply()
    }

    /**
     * Get saved FCM token from SharedPreferences
     */
    fun getSavedToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("AuraPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("fcm_token", null)
    }
}