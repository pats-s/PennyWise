package com.example.pennywise.remote

data class User(
    val userId: String = "",         // Firestore document ID
    val firstName: String = "",      // User's first name
    val lastName: String = "",       // User's last name
    val email: String = "",          // User's email
    val password: String = "",       // User's password (should ideally be hashed)
    val dateOfBirth: Long? = 0L,   // Date of birth
    val walletId: String = "",       // Associated Wallet ID
    val profilePicture: String = "", // Profile picture URL
    val createdAt: Long = System.currentTimeMillis() // Account creation timestamp
)
