package com.example.pennywise.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pennywise.data.model.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE walletId = :walletId")
    suspend fun getTransactionsByWallet(walletId: Int): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE walletId = :walletId AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionsByDateRange(walletId: Int, startDate: Long, endDate: Long): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE walletId = :walletId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionsByDateRangeAndCategory(
        walletId: Int,
        categoryId: Int?,
        startDate: Long,
        endDate: Long
    ): List<Transaction>
}
