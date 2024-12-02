package com.example.pennywise.remote

data class SavingGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val startDate: String,
    val endDate: String,
    val walletId: String
)
