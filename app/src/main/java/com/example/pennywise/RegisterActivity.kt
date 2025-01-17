package com.example.pennywise

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.util.Calendar
import androidx.lifecycle.Observer
import com.example.pennywise.ui.viewmodel.RegisterViewModel
import com.google.firebase.auth.GoogleAuthProvider


class RegisterActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var firstnameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var dobTextView: TextView
    private lateinit var registerButton: Button
    private lateinit var googleSignInButton: Button
    private var selectedDob: String? = null // To store the selected date of birth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val registerViewModel: RegisterViewModel by viewModels() // Using ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        FirebaseAuth.getInstance().signOut()


        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        firstnameEditText = findViewById(R.id.firstnameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)
        dobTextView = findViewById(R.id.dobTextView)

        registerButton = findViewById(R.id.registerButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        // Observe registration status
        /*registerViewModel.registrationStatus.observe(this, Observer { status ->
            Toast.makeText(this, status, Toast.LENGTH_LONG).show()
            if (status == "Registration successful") {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })*/


        registerViewModel.registrationStatus.observe(this, Observer { status ->
            Toast.makeText(this, status, Toast.LENGTH_LONG).show()
            if (status.startsWith("Registration successful")) {
                checkEmailVerification()
            }
        })


        //Observe Google Sign-In status
        registerViewModel.googleSignInStatus.observe(this, Observer { status ->
            Toast.makeText(this, status, Toast.LENGTH_LONG).show()
            if (status.startsWith("Authentication successful")) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        dobTextView.setOnClickListener {
            showDatePickerDialog()
        }
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val firstname = firstnameEditText.text.toString().trim()
            val lastname = lastnameEditText.text.toString().trim()



            if (email.isNotEmpty() && password.isNotEmpty() && firstname.isNotEmpty() && lastname.isNotEmpty() && selectedDob != null) {
                if (isPasswordComplex(password)) {
                    registerViewModel.registerUser(email, password, firstname, lastname, selectedDob!!)
                } else {
                    Toast.makeText(this, "Password must be at least 8 characters long, include a digit, an uppercase letter, and a special character.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)
        }
    }

    private var hasShownVerificationMessage = false // Flag to ensure message is shown once

    private fun checkEmailVerification() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Periodically check for email verification
            val handler = android.os.Handler()
            val runnable = object : Runnable {
                override fun run() {
                    currentUser.reload().addOnCompleteListener {
                        if (currentUser.isEmailVerified) {
                            Toast.makeText(this@RegisterActivity, "Email verified. Redirecting to main activity...", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        } else {
                            if (!hasShownVerificationMessage) {
                                hasShownVerificationMessage = true // Set the flag
                                Toast.makeText(this@RegisterActivity, "Please verify your email to proceed.", Toast.LENGTH_SHORT).show()
                            }
                            handler.postDelayed(this, 5000) // Check every 5 seconds
                        }
                    }
                }
            }
            handler.post(runnable)
        } else {
            Toast.makeText(this, "Error: No user found", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDob = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            dobTextView.text = selectedDob
        }, year, month, day)

        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            registerViewModel.handleGoogleSignInResult(task)
        }
    }
    private fun isPasswordComplex(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$"
        return password.matches(passwordPattern.toRegex())
    }




}
