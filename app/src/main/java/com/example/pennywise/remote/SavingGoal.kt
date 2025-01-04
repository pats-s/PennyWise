package com.example.pennywise.remote

data class SavingGoal(
    val id: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0, // New field
    val startDate: String = "",
    val endDate: String = "",
    val walletId: String = ""
) {
    constructor() : this("", "", 0.0, 0.0, "", "", "")
}

