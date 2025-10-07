package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donation.auraappmarkup.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Modern way to handle activity results
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { authTask ->
                            if (authTask.isSuccessful) {
                                navigateToMain()
                            } else {
                                showError("Google sign in failed: ${authTask.exception?.message}")
                            }
                        }
                } ?: showError("Google sign in failed: No ID token")
            } catch (e: ApiException) {
                showError("Google sign in failed: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
        setupWindowInsets()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showError("Please fill all fields")
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showError("Please enter a valid email")
                }
                else -> {
                    loginWithEmail(email, password)
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.googleSignInButton.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showError("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, CycleDashboardAct::class.java))
        finish()
    }

    private fun showError(message: String) {
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