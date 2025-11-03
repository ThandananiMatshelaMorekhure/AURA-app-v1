package com.donation.auraappmarkup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.donation.auraappmarkup.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "LoginActivity"
    }

    // Biometric constants and fields
    private val BIO_PREFS = "biometric_prefs"
    private val KEY_BIO_ENABLED = "biometric_enabled"
    private val KEY_BIO_EMAIL = "bio_email"
    private val KEY_BIO_PASSWORD = "bio_password"

    private lateinit var biometricExecutor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    // Modern way to handle activity results
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        Log.d(TAG, "Google sign in result: ${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google sign in successful, account: ${account?.email}")

                account?.idToken?.let { token ->
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { authTask ->
                            if (authTask.isSuccessful) {
                                Log.d(TAG, "Firebase authentication successful")
                                navigateToMain()
                            } else {
                                val errorMsg = "Firebase auth failed: ${authTask.exception?.message}"
                                Log.e(TAG, errorMsg)
                                showError(errorMsg)
                            }
                        }
                } ?: run {
                    val errorMsg = "Google sign in failed: No ID token"
                    Log.e(TAG, errorMsg)
                    showError(errorMsg)
                }
            } catch (e: ApiException) {
                val errorMsg = "Google sign in failed: ${e.statusCode} - ${e.message}"
                Log.e(TAG, errorMsg)
                showError("Google sign in failed. Please try again.")
            }
        } else {
            Log.d(TAG, "Google sign in cancelled or failed")
            if (result.resultCode != RESULT_CANCELED) {
                showError("Google sign in failed. Please try again.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        auth = Firebase.auth

        // Check if user is already signed in
        if (auth.currentUser != null) {
            Log.d(TAG, "User already signed in, navigating to main")
            navigateToMain()
            return
        }

        // Configure Google Sign In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign In configuration failed: ${e.message}")
            showError("Google Sign In not available")
        }

        setupClickListeners()
        setupWindowInsets()
        setupBiometricPrompt()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showError("Please fill all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
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
            Log.d(TAG, "Google sign in button clicked")
            try {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch Google sign in: ${e.message}")
                showError("Google Sign In not available. Check your configuration.")
            }
        }

        // Optional: add a biometric sign-in button (you can add in layout)
        binding.btnBiometricSignin.setOnClickListener {
            if (!isBiometricAvailable()) {
                showError("Biometric not available on this device.")
                return@setOnClickListener
            }
            if (!isBiometricEnabled()) {
                showError("Biometric sign-in not enabled yet.")
                return@setOnClickListener
            }
            biometricPrompt.authenticate(getBiometricPromptInfo())
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.loginButton.isEnabled = true

                if (task.isSuccessful) {
                    Log.d(TAG, "Email login successful")
                    saveBiometricCredentials(email, password)
                    navigateToMain()
                } else {
                    val errorMsg = "Authentication failed: ${task.exception?.message}"
                    Log.e(TAG, errorMsg)
                    showError("Login failed. Check your credentials.")
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, CycleDashboardAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // ------------------ BIOMETRIC HELPERS ------------------

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val can = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return can == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun setupBiometricPrompt() {
        biometricExecutor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val creds = readCredentialsFromEncryptedPrefs()
                    if (creds != null) {
                        val (email, password) = creds
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@LoginActivity) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(applicationContext, "Biometric sign-in successful", Toast.LENGTH_SHORT).show()
                                    navigateToMain()
                                } else {
                                    showError("Sign-in failed: ${task.exception?.message}")
                                }
                            }
                    } else {
                        showError("No stored credentials found.")
                    }
                }
            })
    }

    private fun getBiometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sign in with biometrics")
            .setSubtitle("Authenticate to sign in")
            .setNegativeButtonText("Use password")
            .build()
    }

    private fun saveBiometricCredentials(email: String, password: String) {
        try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                BIO_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            with(encPrefs.edit()) {
                putBoolean(KEY_BIO_ENABLED, true)
                putString(KEY_BIO_EMAIL, email)
                putString(KEY_BIO_PASSWORD, password)
                apply()
            }
            Log.d(TAG, "Biometric credentials saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save biometric credentials: ${e.message}")
        }
    }

    private fun isBiometricEnabled(): Boolean {
        return try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                BIO_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encPrefs.getBoolean(KEY_BIO_ENABLED, false)
        } catch (e: Exception) {
            false
        }
    }

    private fun readCredentialsFromEncryptedPrefs(): Pair<String, String>? {
        return try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                BIO_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val enabled = encPrefs.getBoolean(KEY_BIO_ENABLED, false)
            if (!enabled) return null
            val email = encPrefs.getString(KEY_BIO_EMAIL, null)
            val password = encPrefs.getString(KEY_BIO_PASSWORD, null)
            if (email != null && password != null) Pair(email, password) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
