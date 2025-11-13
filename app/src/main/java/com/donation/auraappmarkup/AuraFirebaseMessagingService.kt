package com.donation.auraappmarkup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AuraFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AuraFCM"
        private const val CHANNEL_ID = "aura_notifications"
        private const val CHANNEL_NAME = "Aura Notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Aura"
            val body = notification.body ?: ""
            showNotification(title, body)
        }

        // Handle data payload (for custom actions)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Save token to SharedPreferences
        saveTokenToPreferences(token)

        // TODO: Send token to your backend server if needed
        // sendTokenToServer(token)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for cycle reminders and health tips"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, CycleDashboardAct::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notifications) // Make sure this exists
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data from notification
        val type = data["type"]
        when (type) {
            "cycle_reminder" -> {
                Log.d(TAG, "Cycle reminder notification")
                showNotification(
                    "Cycle Reminder",
                    data["message"] ?: "Time to track your cycle!"
                )
            }
            "symptom_tracking" -> {
                Log.d(TAG, "Symptom tracking reminder")
                showNotification(
                    "Symptom Tracking",
                    data["message"] ?: "Don't forget to log your symptoms today!"
                )
            }
            "health_tip" -> {
                Log.d(TAG, "Health tip notification")
                showNotification(
                    "Health Tip",
                    data["message"] ?: "Check out today's health tip!"
                )
            }
            else -> {
                Log.d(TAG, "Unknown notification type: $type")
            }
        }
    }

    private fun saveTokenToPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("AuraPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "Token saved to preferences")
    }
}