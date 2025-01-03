package com.example.pennywise.remote

data class Wallet(
    val walletId: String = "",     // Firestore document ID
    val balance: Double = 0.0,     // Current wallet balance
    val createdAt: Long = 0L
)
