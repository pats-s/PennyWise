package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth



class SignInViewModel : ViewModel() {

    private val _signInResult = MutableLiveData<Result<String>>()
    val signInResult: LiveData<Result<String>> get() = _signInResult

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _signInResult.value = Result.failure(Exception("Please fill in all fields"))
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        fetchUserDetailsFromFirestore(userId)
                    } else {
                        _signInResult.value = Result.failure(Exception("Failed to retrieve user ID"))
                    }
                } else {
                    _signInResult.value = Result.failure(task.exception ?: Exception("Sign-in failed"))
                }
            }
    }

    private fun fetchUserDetailsFromFirestore(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstname") ?: "User"
                    _signInResult.value = Result.success("Welcome $firstName!")
                } else {
                    _signInResult.value = Result.failure(Exception("User data not found in Firestore"))
                }
            }
            .addOnFailureListener { exception ->
                _signInResult.value = Result.failure(exception)
            }
    }
}

//
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("users")
//            .whereEqualTo("email", email)
//            .whereEqualTo("password", password)
//            .get()
//            .addOnSuccessListener { documents ->
//                if (!documents.isEmpty) {
//                    val user = documents.documents[0]
//                    val firstName = user.getString("firstname") ?: "User"
//                    _signInResult.value = Result.success("Welcome $firstName!")
//                } else {
//                    _signInResult.value = Result.failure(Exception("Invalid email or password"))
//                }
//            }
//            .addOnFailureListener { exception ->
//                _signInResult.value = Result.failure(exception)
//            }
//    }
//}
