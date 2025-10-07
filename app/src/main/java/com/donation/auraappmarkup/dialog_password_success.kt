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

class dialog_password_success : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_password_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val ivClose = findViewById<ImageView>(R.id.ivCloseSuccess)

        // Continue button → Go back to ChangePasswordActivity
        btnContinue.setOnClickListener {
            val intent = Intent(this, PasswordChange::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Close button → Also go back
        ivClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
