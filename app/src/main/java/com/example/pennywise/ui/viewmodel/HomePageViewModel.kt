package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.remote.Category
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.User
import com.example.pennywise.remote.Wallet
import java.util.UUID

class HomePageViewModel(private val repository: PennyWiseRepository) : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _walletBalance = MutableLiveData<Double>()
    val walletBalance: LiveData<Double> get() = _walletBalance

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _todayTransactions = MutableLiveData<List<Transaction>>()
    val todayTransactions: LiveData<List<Transaction>> = _todayTransactions

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _pastTransactions = MutableLiveData<List<Transaction>>()
    val pastTransactions: LiveData<List<Transaction>> get() = _pastTransactions

    private val _categoriesMap = MutableLiveData<Map<String, Category>>()
    val categoriesMap: LiveData<Map<String, Category>> get() = _categoriesMap

    private val _exchangeRates = MutableLiveData<Map<String, Double>>()
    val exchangeRates: LiveData<Map<String, Double>> get() = _exchangeRates


    init {
        fetchUserData()
        checkAndUploadPredefinedCategories()
        fetchCategories()
    }

    private fun fetchUserData() {
        // Replace this with actual logic for fetching the logged-in user
        val dummyUser = User(
            userId = "123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            walletId = "wallet123"
        )

        val dummyWallet = Wallet(walletId = "wallet123", balance = 250.75)

        // Simulate user data fetching
        _userName.postValue(dummyUser.firstName)
        _walletBalance.postValue(dummyWallet.balance)
    }

    private fun fetchCategories() {
        repository.getCategories(
            onSuccess = { _categories.postValue(it) },
            onFailure = { /* Handle error */ }
        )
    }

    fun addTransaction(type: String, amount: Double, categoryId: String, date: String) {
        val walletId = "wallet123" // Replace with the actual wallet ID of the logged-in user

        repository.getWallet(walletId, onSuccess = { wallet ->
            val currentBalance = wallet.balance
            if (type == "Expense" && amount > currentBalance) {
                _errorMessage.postValue("Insufficient balance!")
                return@getWallet
            }

            val updatedBalance = if (type == "Income") {
                currentBalance + amount
            } else {
                currentBalance - amount
            }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                type = type,
                amount = amount,
                categoryId = categoryId,
                walletId = walletId,
                date = date
            )

            repository.addTransaction(
                transaction = transaction,
                onSuccess = {
                    repository.updateWalletBalance(walletId, updatedBalance, onSuccess = {
                        fetchUserData() // Refresh user data to update the balance in UI
                    }, onFailure = {
                        _errorMessage.postValue("Failed to update wallet balance!")
                    })
                },
                onFailure = { exception ->
                    _errorMessage.postValue("Failed to add transaction: ${exception.message}")
                }
            )
        }, onFailure = { exception ->
            _errorMessage.postValue("Failed to fetch wallet: ${exception.message}")
        })
    }

    fun fetchTodayTransactions() {
        repository.getTodayTransactions(
            onSuccess = { transactions ->
                _todayTransactions.value = transactions
            },
            onFailure = { exception ->
                _errorMessage.value = exception.message
            }
        )
    }


    fun fetchPastTransactions() {
        repository.getTransactions(
            onSuccess = { transactions -> _pastTransactions.postValue(transactions) },
            onFailure = { println("Failed to fetch past transactions: ${it.message}") }
        )
    }

    fun fetchCategoriesForMapping() {
        repository.getAllCategories(
            onSuccess = { _categoriesMap.postValue(it) },
            onFailure = { /* Handle error */ }
        )
    }

    fun addSavingGoal(savingGoal: SavingGoal) {
        repository.addSavingGoal(
            savingGoal,
            onSuccess = { println("Saving goal added successfully") },
            onFailure = { println("Failed to add saving goal") }
        )
    }

    private fun checkAndUploadPredefinedCategories() {
        repository.getCategories(
            onSuccess = { categories ->
                if (categories.isEmpty()) {
                    repository.uploadPredefinedCategories(
                        onSuccess = { println("Predefined categories uploaded successfully") },
                        onFailure = { println("Failed to upload predefined categories: ${it.message}") }
                    )
                }
            },
            onFailure = { println("Error fetching categories: ${it.message}") }
        )
    }

    fun fetchExchangeRates() {
        repository.getExchangeRates(
            onSuccess = { rates ->
                _exchangeRates.postValue(rates)
            },
            onFailure = { exception ->
                println("Failed to fetch exchange rates: ${exception.message}")
            }
        )
    }

    fun fetchFilteredTransactions(day: String?, startOfWeekOrMonth: String?, endOfWeek: String?) {
        repository.getFilteredTransactions(day, startOfWeekOrMonth, endOfWeek,
            onSuccess = { transactions ->
                _pastTransactions.postValue(transactions)
            },
            onFailure = { exception ->
                println("Failed to fetch filtered transactions: ${exception.message}")
            }
        )
    }

    fun fetchFilteredTransactionsByDay(day: String) {
        repository.getFilteredTransactions(
            day = day,
            startOfWeekOrMonth = null,
            endOfWeek = null,
            onSuccess = { _pastTransactions.postValue(it) },
            onFailure = { println("Error: ${it.message}") }
        )
    }

    fun fetchFilteredTransactionsByWeek(startOfWeek: String, endOfWeek: String) {
        repository.getFilteredTransactions(
            day = null,
            startOfWeekOrMonth = startOfWeek,
            endOfWeek = endOfWeek,
            onSuccess = { _pastTransactions.postValue(it) },
            onFailure = { println("Error: ${it.message}") }
        )
    }

    fun fetchFilteredTransactionsByMonth(month: String) {
        repository.getFilteredTransactions(
            day = null,
            startOfWeekOrMonth = month,
            endOfWeek = null,
            onSuccess = { _pastTransactions.postValue(it) },
            onFailure = { println("Error: ${it.message}") }
        )
    }
}
