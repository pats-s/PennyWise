package com.example.pennywise.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Wallet::class,
        parentColumns = ["walletId"],
        childColumns = ["walletId"],
        onDelete = ForeignKey.CASCADE

    ), ForeignKey(
        entity = Category::class,
        parentColumns = ["categoryId"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    ) ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0,
    @ColumnInfo(name = "walletId") val walletId: Int,
    @ColumnInfo(name = "type") val type: String, // Income or Expense
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "categoryId") val categoryId: Int,
    @ColumnInfo(name = "date") val date: Long // Use timestamp for dates
)