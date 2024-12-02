package com.example.pennywise

import android.content.Context
import com.example.pennywise.api.RetrofitInstance
import com.example.pennywise.local.dao.AppSettingsDao
import com.example.pennywise.local.entities.AppSettingsEntity
import com.example.pennywise.local.predefinedCategories
import com.example.pennywise.remote.Category
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.Wallet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PennyWiseRepository private constructor(context: Context) {

    private val appSettingsDao: AppSettingsDao
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val database = PennyWiseDatabase.getDatabase(context)
        appSettingsDao = database.appSettingsDao()
    }

    // App Settings
    suspend fun saveSettings(settings: AppSettingsEntity) {
        appSettingsDao.saveSettings(settings)
    }

    suspend fun getSettings(): AppSettingsEntity? {
        return appSettingsDao.getSettings()
    }

    fun uploadPredefinedCategories(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val categoriesCollection = firestore.collection("categories")

        predefinedCategories.forEach { category ->
            categoriesCollection.document(category.id)
                .set(category)
                .addOnSuccessListener { println("Category ${category.name} uploaded successfully") }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
        onSuccess()
    }

    fun getCategories(
        onSuccess: (List<Category>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val categoriesCollection = firestore.collection("categories")

        categoriesCollection.get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.toObjects(Category::class.java)
                onSuccess(categories)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun addTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")

        transactionsCollection.document(transaction.id)
            .set(transaction)
            .addOnSuccessListener {
                println("Transaction ${transaction.id} added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                println("Failed to add transaction: ${exception.message}")
                onFailure(exception)
            }
    }

    fun getTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")

        transactionsCollection.get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.toObjects(Transaction::class.java)
                onSuccess(transactions)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getAllCategories(
        onSuccess: (Map<String, Category>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.documents.associate { doc ->
                    val category = doc.toObject(Category::class.java)
                    category?.id to category
                }.filterKeys { it != null } as Map<String, Category>
                onSuccess(categories)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun addSavingGoal(
        savingGoal: SavingGoal,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val savingGoalsCollection = firestore.collection("saving_goals")

        savingGoalsCollection.document(savingGoal.id)
            .set(savingGoal)
            .addOnSuccessListener {
                println("Saving goal ${savingGoal.id} added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                println("Failed to add saving goal: ${exception.message}")
                onFailure(exception)
            }
    }

    fun getSavingGoals(
        walletId: String,
        onSuccess: (List<SavingGoal>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val savingGoalsCollection = firestore.collection("saving_goals")

        savingGoalsCollection.whereEqualTo("walletId", walletId).get()
            .addOnSuccessListener { snapshot ->
                val savingGoals = snapshot.toObjects(SavingGoal::class.java)
                onSuccess(savingGoals)
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch saving goals: ${exception.message}")
                onFailure(exception)
            }
    }

    fun getTodayTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Format the current date as it appears in Firestore
        val today = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date()) // Adjust format to match Firestore

        firestore.collection("transactions")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { result ->
                val transactions = result.documents.mapNotNull { it.toObject(Transaction::class.java) }
                onSuccess(transactions)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    fun getExchangeRates(
        onSuccess: (Map<String, Double>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getExchangeRates()
                withContext(Dispatchers.Main) {
                    onSuccess(response.rates)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PennyWiseRepository? = null

        fun getInstance(context: Context): PennyWiseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = PennyWiseRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun getFilteredTransactions(
        day: String?,
        startOfWeekOrMonth: String?,
        endOfWeek: String?,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")
        var query: Query = transactionsCollection

        when {
            day != null -> {
                query = query.whereEqualTo("date", day)
            }
            startOfWeekOrMonth != null && endOfWeek != null -> {
                query = query.whereGreaterThanOrEqualTo("date", startOfWeekOrMonth)
                    .whereLessThanOrEqualTo("date", endOfWeek)
            }
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.toObjects(Transaction::class.java)
                onSuccess(transactions)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getWallet(
        walletId: String,
        onSuccess: (Wallet) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("wallets")
            .document(walletId)
            .get()
            .addOnSuccessListener { snapshot ->
                val wallet = snapshot.toObject(Wallet::class.java)
                if (wallet != null) {
                    onSuccess(wallet)
                } else {
                    onFailure(Exception("Wallet not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateWalletBalance(
        walletId: String,
        newBalance: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("wallets")
            .document(walletId)
            .update("balance", newBalance)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


}
