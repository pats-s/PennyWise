package com.example.pennywise.local

import com.example.pennywise.remote.Category

val predefinedCategories = listOf(
    // Income categories
    Category(id = "1", name = "Salary", type = "Income"),
    Category(id = "2", name = "Gifts", type = "Income"),
    Category(id = "3", name = "Investments", type = "Income"),

    // Expense categories
    Category(id = "4", name = "Groceries", type = "Expense"),
    Category(id = "5", name = "Rent", type = "Expense"),
    Category(id = "6", name = "Entertainment", type = "Expense"),
    Category(id = "7", name = "Utilities", type = "Expense"),
    Category(id = "8", name = "Transportation", type = "Expense"),
    Category(id = "9", name = "Dining Out", type = "Expense")
)
