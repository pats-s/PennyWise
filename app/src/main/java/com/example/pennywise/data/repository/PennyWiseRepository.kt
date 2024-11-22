package com.example.pennywise.data.repository

import com.example.pennywise.data.local.dao.*
import com.example.pennywise.data.model.*
import com.example.pennywise.data.local.PennyWiseDatabase
import kotlinx.coroutines.tasks.await

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

    // Firestore reference
    private val firebaseFirestore = PennyWiseDatabase.getFirestore()

    // Sync Users from Firestore to Room
    suspend fun syncUsers() {
        val firestoreUsers = firebaseFirestore.collection("users").get().await()
        for (document in firestoreUsers) {
            val user = document.toObject(User::class.java)
            userDao.insertUser(user)
        }
    }

    // Sync Wallets from Firestore to Room
    suspend fun syncWallets() {
        val firestoreWallets = firebaseFirestore.collection("wallets").get().await()
        for (document in firestoreWallets) {
            val wallet = document.toObject(Wallet::class.java)
            walletDao.insertWallet(wallet)
        }
    }

    // Sync Transactions from Firestore to Room
    suspend fun syncTransactions(walletId: Int) {
        val firestoreTransactions = firebaseFirestore.collection("transactions")
            .whereEqualTo("walletId", walletId)
            .get()
            .await()

        for (document in firestoreTransactions) {
            val transaction = document.toObject(Transaction::class.java)
            transactionDao.insertTransaction(transaction)
        }
    }

    // Sync SavingGoals from Firestore to Room
    suspend fun syncSavingGoals(userId: Int) {
        val firestoreSavingGoals = firebaseFirestore.collection("saving_goals")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        for (document in firestoreSavingGoals) {
            val savingGoal = document.toObject(SavingGoal::class.java)
            savingGoalDao.insertSavingGoal(savingGoal)
        }
    }

    // Sync Categories from Firestore to Room
    suspend fun syncCategories() {
        val firestoreCategories = firebaseFirestore.collection("categories").get().await()
        for (document in firestoreCategories) {
            val category = document.toObject(Category::class.java)
            categoryDao.insertCategory(category)
        }
    }

    // Add User to both Room and Firestore
    suspend fun addUser(user: User) {
        userDao.insertUser(user) // Add to Room
        firebaseFirestore.collection("users").add(user) // Add to Firestore
    }

    // Add Wallet to both Room and Firestore
    suspend fun addWallet(wallet: Wallet) {
        walletDao.insertWallet(wallet) // Add to Room
        firebaseFirestore.collection("wallets").add(wallet) // Add to Firestore
    }

    // Add a Transaction to both Room and Firestore
    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction) // Add to Room
        firebaseFirestore.collection("transactions").add(transaction) // Add to Firestore
    }

    // Add SavingGoal to both Room and Firestore
    suspend fun addSavingGoal(savingGoal: SavingGoal) {
        savingGoalDao.insertSavingGoal(savingGoal) // Add to Room
        firebaseFirestore.collection("saving_goals").add(savingGoal) // Add to Firestore
    }

    // Add Category to both Room and Firestore
    suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category) // Add to Room
        firebaseFirestore.collection("categories").add(category) // Add to Firestore
    }
}
