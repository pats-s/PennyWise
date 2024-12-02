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


class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseAuth.getInstance().signOut()
        val registerLink = findViewById<TextView>(R.id.RegisterButton)

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
    private fun navigateToProfileActivity() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
        }
    }
}

