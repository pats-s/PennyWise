package com.example.pennywise.remote

data class Transaction(
    val id: String = "",         // Firestore document ID
    val amount: Double = 0.0,    // Transaction amount
    val description: String = "",// Optional description
    val categoryId: String = "", // Reference to the Category ID
    val walletId: String = "",   // Reference to the Wallet ID
    val date: String = "",         // Timestamp for the transaction
    val type: String = ""        // "Income" or "Expense"
)

