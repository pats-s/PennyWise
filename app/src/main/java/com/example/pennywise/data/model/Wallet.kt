package com.example.pennywise.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val walletId: Int = 0,
    @ColumnInfo(name = "balance") val balance : Double = 0.0
)
