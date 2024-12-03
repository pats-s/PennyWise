package com.example.pennywise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pennywise.R
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.activity.viewModels
import com.example.pennywise.databinding.ActivitySignInBinding
import com.example.pennywise.ui.viewmodel.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.common.api.ApiException
import java.security.MessageDigest


class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val signInViewModel: SignInViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseAuth.getInstance().signOut()


         auth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Call function to update password in Firestore
                updatePasswordInFirestore(currentUser.uid)
            }
        }

        // Reset Password Link
        val resetPasswordLink = findViewById<TextView>(R.id.resetPasswordLink)
        resetPasswordLink.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        val registerLink = findViewById<TextView>(R.id.registerLink)

        // Set click listener for "Register now" link
        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            signInViewModel.signIn(email, password)
        }

        // Observe the sign-in result
//        signInViewModel.signInResult.observe(this, Observer { result ->
//            result.onSuccess { message ->
//                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//                val intent = Intent(this, ProfileActivity::class.java)
//                startActivity(intent)
//                finish() // Optional: Close SignInActivity so the user can't navigate back to it
//            }.onFailure { exception ->
//                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
//            }
//        })

        signInViewModel.signInResult.observe(this, Observer { result ->
            result.onSuccess { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                navigateToProfileActivity()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onStart() {
        super.onStart()
        // Attach the AuthStateListener
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Detach the AuthStateListener
        auth.removeAuthStateListener(authStateListener)
    }
    private fun navigateToProfileActivity() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(password.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
    private fun updatePasswordInFirestore(userId: String) {
        val newPassword = binding.passwordEditText.text.toString().trim()
        if (newPassword.isNotEmpty()) {
            val hashedPassword = hashPassword(newPassword)

            // Update Firestore document
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userId)
                .update("hashedPassword", hashedPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password updated in Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update password: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

