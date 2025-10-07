package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donation.auraappmarkup.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val fullName = binding.nameEditText.text.toString() // Get the full name

            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Save user profile with name to Firestore
                            saveUserProfile(fullName, email)
                        } else {
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveUserProfile(fullName: String, email: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userData = hashMapOf(
                "name" to fullName,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CycleDashboardAct::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Still proceed to main activity even if Firestore save fails
                    startActivity(Intent(this, CycleDashboardAct::class.java))
                    finish()
                }
        } else {
            Toast.makeText(this, "User creation failed", Toast.LENGTH_SHORT).show()
        }
    }
}