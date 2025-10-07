package com.donation.auraappmarkup

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.donation.auraappmarkup.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()
        loadUserData()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        // Country dropdown
        val countries = arrayOf(
            "South Africa", "United States", "United Kingdom", "Canada",
            "Australia", "Germany", "France", "India", "Brazil", "Mexico",
            "Nigeria", "Kenya", "Ghana", "Egypt", "Japan", "China"
        )
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        (binding.countryInput as? AutoCompleteTextView)?.setAdapter(countryAdapter)

        // Gender dropdown
        val genders = arrayOf("Female", "Male", "Non-binary", "Prefer not to say")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        (binding.genderInput as? AutoCompleteTextView)?.setAdapter(genderAdapter)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.apply {
                        fullNameInput.setText(document.getString("name") ?: "")
                        nicknameInput.setText(document.getString("nickname") ?: "")
                        profileEmail.setText(document.getString("email") ?: auth.currentUser?.email ?: "")
                        phoneInput.setText(document.getString("phone") ?: "")
                        (countryInput as? AutoCompleteTextView)?.setText(document.getString("country") ?: "", false)
                        (genderInput as? AutoCompleteTextView)?.setText(document.getString("gender") ?: "", false)
                        addressInput.setText(document.getString("address") ?: "")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate inputs
        val fullName = binding.fullNameInput.text.toString().trim()
        val email = binding.profileEmail.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.fullNameLayout.error = "Full name is required"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Valid email is required"
            return
        }

        // Clear errors
        binding.fullNameLayout.error = null
        binding.emailLayout.error = null

        // Prepare data
        val profileData = hashMapOf(
            "name" to fullName,
            "nickname" to binding.nicknameInput.text.toString().trim(),
            "email" to email,
            "phone" to binding.phoneInput.text.toString().trim(),
            "country" to (binding.countryInput as? AutoCompleteTextView)?.text.toString(),
            "gender" to (binding.genderInput as? AutoCompleteTextView)?.text.toString(),
            "address" to binding.addressInput.text.toString().trim(),
            "updatedAt" to System.currentTimeMillis()
        )

        // Disable button during save
        binding.submitButton.isEnabled = false

        firestore.collection("users")
            .document(userId)
            .set(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.submitButton.isEnabled = true
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}