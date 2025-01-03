package com.example.pennywise

import android.content.Context
import com.example.pennywise.api.RetrofitInstance
import com.example.pennywise.local.dao.AppSettingsDao
import com.example.pennywise.local.entities.AppSettingsEntity
import com.example.pennywise.local.predefinedCategories
import com.example.pennywise.remote.Bill
import com.example.pennywise.remote.Category
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.User
import com.example.pennywise.remote.Wallet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PennyWiseRepository private constructor(context: Context) {

    private val appSettingsDao: AppSettingsDao
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val database = PennyWiseDatabase.getDatabase(context)
        appSettingsDao = database.appSettingsDao()
    }


    fun getUserData(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                   onSuccess(user)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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

    fun getCategoryName(
        categoryId: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("categories")
            .document(categoryId)
            .get()
            .addOnSuccessListener { snapshot ->
                val category = snapshot.toObject(Category::class.java)
                onSuccess(category?.name)
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
        firestore.collection("saving_goals")
            .whereEqualTo("walletId", walletId)
            .get()
            .addOnSuccessListener { snapshot ->
                val goals = snapshot.toObjects(SavingGoal::class.java)
                onSuccess(goals)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    //for a single user
    fun getTodayTransactionsForUser(
        userId: String,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Format today's date to match Firestore format
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Fetch user data to get the wallet ID
        getUserData(userId, onSuccess = { user ->
            val walletId = user.walletId

            // Query transactions for the wallet and today's date
            firestore.collection("transactions")
                .whereEqualTo("walletId", walletId)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener { result ->
                    val transactions = result.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    onSuccess(transactions)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }, onFailure = { exception ->
            onFailure(exception)
        })
    }

    //jane
    fun getTodayTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Format the current date as it appears in Firestore
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) // Adjust format to match Firestore

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
    val firebaseFirestore = FirebaseFirestore.getInstance()
    //for the logged in user only
    fun getTransactionsForWallet(
        walletId: String,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Assuming transactions are stored in a Firebase collection
        val transactionsRef = firebaseFirestore.collection("transactions")
        transactionsRef.whereEqualTo("walletId", walletId).get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.toObjects(Transaction::class.java)
                onSuccess(transactions)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }



    //jane
    fun getFilteredTransactions(
        day: String?,
        startOfWeekOrMonth: String?,
        endOfWeek: String?,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")
        var query: Query = transactionsCollection

        // Ensure all dates are formatted as `dd/MM/yyyy`
        val formattedStart = startOfWeekOrMonth?.let { formatDateToStandard(it) }
        val formattedEnd = endOfWeek?.let { formatDateToStandard(it) }

        when {
            day != null -> {
                query = query.whereEqualTo("date", formatDateToStandard(day))
            }
            formattedStart != null && formattedEnd != null -> {
                query = query.whereGreaterThanOrEqualTo("date", formattedStart)
                    .whereLessThanOrEqualTo("date", formattedEnd)
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



    //tried to do it today
    fun getFilteredTransactions1(
        walletId: String,
        day: String?,
        startOfWeekOrMonth: String?,
        endOfWeek: String?,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")
        var query: Query = transactionsCollection

        try {
            query = when {
                day != null -> query.whereEqualTo("date", day)
                startOfWeekOrMonth != null && endOfWeek != null -> query
                    .whereGreaterThanOrEqualTo("date", startOfWeekOrMonth)
                    .whereLessThanOrEqualTo("date", endOfWeek)
                else -> query
            }

            query.get()
                .addOnSuccessListener { snapshot ->
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    onSuccess(transactions)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }




    //we were using
    fun getFilteredTransactions2(
        day: String?,
        startOfWeekOrMonth: String?,
        endOfWeek: String?,
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val transactionsCollection = firestore.collection("transactions")
        var query: Query = transactionsCollection

        try {
            query = when {
                day != null -> query.whereEqualTo("date", day)
                startOfWeekOrMonth != null && endOfWeek != null -> query
                    .whereGreaterThanOrEqualTo("date", startOfWeekOrMonth)
                    .whereLessThanOrEqualTo("date", endOfWeek)
                startOfWeekOrMonth != null -> query.whereGreaterThanOrEqualTo("date", startOfWeekOrMonth)
                else -> query
            }

            query.get()
                .addOnSuccessListener { snapshot ->
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    onSuccess(transactions)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }




    private fun formatDateToStandard(date: String): String {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // Flexible input format
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Consistent output format
        return try {
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            date // Return original date if parsing fails
        }
    }


    // Helper to calculate end of the month
    private fun calculateEndOfMonth(startOfMonth: String): String {
        val dateFormat = SimpleDateFormat("M/yyyy", Locale.getDefault())
        val parsedDate = dateFormat.parse(startOfMonth)
        val calendar = Calendar.getInstance()
        parsedDate?.let {
            calendar.time = it
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(calendar.time)
    }


    fun getWallet(
        walletId: String,
        onSuccess: (Wallet) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (walletId.isEmpty()) {
            onFailure(Exception("Invalid wallet ID"))
            return
        }

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
        if (walletId.isEmpty()) {
            onFailure(Exception("Invalid wallet ID"))
            return
        }

        firestore.collection("wallets")
            .document(walletId)
            .update("balance", newBalance)
            .addOnSuccessListener {
                println("Wallet balance updated successfully.")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                println("Failed to update wallet balance: ${exception.message}")
                onFailure(exception)
            }

    }


    //BILL REMINDERS FEATURE
    fun addBill(
        bill: Bill,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("bills")
            .document(bill.id)
            .set(bill)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getBills(
        walletId: String,
        onSuccess: (List<Bill>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("bills")
            .whereEqualTo("walletId", walletId)
            .get()
            .addOnSuccessListener { snapshot ->
                val bills = snapshot.toObjects(Bill::class.java)
                onSuccess(bills)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deductBillAmount(
        bill: Bill,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (bill.walletId.isEmpty()) {
            onFailure(Exception("Invalid wallet ID"))
            return
        }

        println("Attempting to deduct bill. Wallet ID: ${bill.walletId}, Amount: ${bill.amount}")

        getWallet(bill.walletId, onSuccess = { wallet ->
            println("Current Wallet Balance: ${wallet.balance}")
            if (wallet.balance >= bill.amount) {
                val newBalance = wallet.balance - bill.amount
                println("New Wallet Balance: $newBalance")
                updateWalletBalance(wallet.walletId, newBalance, onSuccess, onFailure)
            } else {
                onFailure(Exception("Insufficient balance"))
            }
        }, onFailure = { exception ->
            println("Failed to fetch wallet: ${exception.message}")
            onFailure(exception)
        })
    }





}
