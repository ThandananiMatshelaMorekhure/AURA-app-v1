package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donation.auraappmarkup.databinding.ActivityPasswordChangeBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PasswordChange : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordChangeBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPasswordChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            val currentPassword = binding.etOldPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            when {
                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                    showToast("Please fill in all fields")
                }
                newPassword != confirmPassword -> {
                    showToast("New passwords do not match")
                }
                newPassword.length < 6 -> {
                    showToast("Password must be at least 6 characters")
                }
                else -> {
                    updatePassword(currentPassword, newPassword)
                }
            }
        }
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        if (user == null) {
            showToast("No user logged in")
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            showToast("Cannot verify user email")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        firestore.collection("users").document(user.uid)
                            .update("lastPasswordChange", System.currentTimeMillis())
                            .addOnSuccessListener {
                                showToast("Password successfully updated!")
                                val intent = Intent(this, dialog_password_success::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showToast("Password updated but failed to log Firestore: ${e.message}")
                                val intent = Intent(this, dialog_server_error::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                    .addOnFailureListener { e ->
                        showToast("Password update failed: ${e.message}")
                        val intent = Intent(this, dialog_server_error::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                showToast("Reauthentication failed: ${e.message}")
                val intent = Intent(this, dialog_server_error::class.java)
                startActivity(intent)
                finish()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
