package com.example.pennywise.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.security.MessageDigest

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    val registrationStatus = MutableLiveData<String>()
    val googleSignInStatus = MutableLiveData<String>()



    fun registerUser(email: String, password: String, firstname: String, lastname: String, dob: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            val userId = user.uid
                            saveAdditionalData(userId, firstname, lastname, email, hashPassword(password), dob)
                            registrationStatus.value = "Registration successful. Please check your email to verify your account."
                        } else {
                            registrationStatus.value = "Failed to send verification email: ${emailTask.exception?.message}"
                        }
                    }
                } else {
                    registrationStatus.value = "Registration failed: ${task.exception?.message}"
                }
            }
    }




    // was usign before email verification
    fun registerUser1(email: String, password: String, firstname: String, lastname: String, dob: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    saveAdditionalData(userId, firstname, lastname, email, hashPassword(password), dob)
                } else {
                    registrationStatus.value = "Registration failed: ${task.exception?.message}"
                }
            }
    }
    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(password.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }



    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                checkUserInFirestore(account)
            }
        } catch (e: ApiException) {
            googleSignInStatus.value = "Google Sign-In failed: ${e.message}"
        }
    }

    fun checkUserInFirestore(account: GoogleSignInAccount) {
        val firestore = FirebaseFirestore.getInstance()
        val email = account.email ?: return
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // User doesn't exist, so we register them
                    val firstname = account.givenName ?: ""
                    val lastname = account.familyName ?: ""
                    val userId = account.id ?: ""
                    val dob = null
                    authenticateWithFirebase(account) { userId ->
                        saveAdditionalData(userId,firstname,lastname,email,"GoogleAuth", dob)
                    }
                    //saveAdditionalData(userId, firstname, lastname, email, "GoogleAuth", dob)
                } else {
                    // if user exists we log them in
                    googleSignInStatus.value = "Welcome back, $email!"
                    authenticateWithFirebase(account, null)


                }
            }
            .addOnFailureListener {
                googleSignInStatus.value = "Failed to check user in Firestore: ${it.message}"
            }
    }

    private fun authenticateWithFirebase(account: GoogleSignInAccount, onSuccess: ((String) -> Unit)?) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    onSuccess?.invoke(userId) // Callback to save additional data
                    googleSignInStatus.value = "Authentication successful for ${account.email}"
                } else {
                    googleSignInStatus.value = "Authentication failed: ${task.exception?.message}"
                }
            }



    }
    private fun saveAdditionalData(userId: String, firstname: String, lastname: String, email: String, password: String, dob: String?) {
        val firestore = FirebaseFirestore.getInstance()

        // Create a new wallet
        val walletMap = hashMapOf(
            "balance" to 0.0,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("wallets")
            .add(walletMap)
            .addOnSuccessListener { walletDocument ->
                val walletId = walletDocument.id


                val userMap = hashMapOf(
                    "firstname" to firstname,
                    "lastname" to lastname,
                    "email" to email,
                    "password" to password,
                    "dob" to dob,
                    "profilePicture" to null,
                    "walletId" to walletId,
                    "lastModified" to System.currentTimeMillis()
                )

                firestore.collection("users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        registrationStatus.value = "Registration successful"
                    }
                    .addOnFailureListener { e ->
                        registrationStatus.value = "Failed to save user data: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                registrationStatus.value = "Failed to create wallet: ${e.message}"
            }
    }


}

