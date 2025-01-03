package com.example.pennywise.remote

data class Bill(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val paymentDate: String = "",
    val walletId: String = ""
)
