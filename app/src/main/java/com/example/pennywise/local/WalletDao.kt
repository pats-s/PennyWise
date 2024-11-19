package com.example.pennywise.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pennywise.data.model.Wallet

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet): Long // Returns the ID of the inserted wallet

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets WHERE walletId = :walletId LIMIT 1")
    suspend fun getWalletById(walletId: Int): Wallet

    @Query("SELECT * FROM wallets")
    suspend fun getAllWallets(): List<Wallet>
}
