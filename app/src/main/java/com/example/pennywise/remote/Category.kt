package com.example.pennywise.remote

data class Category(
    val id: String = "",       // Firestore document ID
    val name: String = "",     // Name of the category (e.g., "Groceries", "Salary")
    val type: String = ""      // "Income" or "Expense"
)
