package com.example.pennywise.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    @ColumnInfo(name = "name") val name: String, // E.g., "Groceries", "Salary"
    @ColumnInfo(name = "type") val type: String, // Either "Income" or "Expense"
    @ColumnInfo(name = "last_modified") val lastModified: Long = System.currentTimeMillis()
)