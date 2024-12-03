package com.example.pennywise.remote

data class SavingGoal(
    val id: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val walletId: String = ""
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", 0.0, "", "", "")
}
