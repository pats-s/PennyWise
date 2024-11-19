package com.example.pennywise.data.repository

import com.example.pennywise.data.model.Category
import com.example.pennywise.data.model.SavingGoal
import com.example.pennywise.data.model.Transaction
import com.example.pennywise.data.model.User
import com.example.pennywise.data.model.Wallet
import com.example.pennywise.data.local.dao.UserDao
import com.example.pennywise.data.local.dao.WalletDao
import com.example.pennywise.data.local.dao.TransactionDao
import com.example.pennywise.data.local.dao.SavingGoalDao
import com.example.pennywise.data.local.dao.CategoryDao





class PennyWiseRepository(
    private val userDao: UserDao,
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao,
    private val savingGoalDao: SavingGoalDao,
    private val categoryDao: CategoryDao
) {
    // User-related operations
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun getUserByCredentials(email: String, password: String) = userDao.getUserByCredentials(email, password)

    // Wallet-related operations
    suspend fun insertWallet(wallet: Wallet) = walletDao.insertWallet(wallet)
    suspend fun getWalletById(walletId: Int) = walletDao.getWalletById(walletId)

    // Transaction-related operations
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun getTransactionsByWallet(walletId: Int) = transactionDao.getTransactionsByWallet(walletId)

    // SavingGoal-related operations
    suspend fun insertSavingGoal(savingGoal: SavingGoal) = savingGoalDao.insertSavingGoal(savingGoal)
    suspend fun getSavingGoalsByUser(userId: Int) = savingGoalDao.getSavingGoalsByUser(userId)

    // Category-related operations
    suspend fun insertCategories(categories: List<Category>) = categoryDao.insertCategories(categories)
    suspend fun getAllCategories() = categoryDao.getAllCategories()
}