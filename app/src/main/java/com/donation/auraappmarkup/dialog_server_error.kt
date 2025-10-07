package com.donation.auraappmarkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class dialog_server_error : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_server_error)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRetry = findViewById<Button>(R.id.btnWaitAndRetry)
        val ivClose = findViewById<ImageView>(R.id.ivCloseServerError)

        // Retry button → Go back to ChangePasswordActivity
        btnRetry.setOnClickListener {
            val intent = Intent(this, PasswordChange::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Close button → also go back
        ivClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
