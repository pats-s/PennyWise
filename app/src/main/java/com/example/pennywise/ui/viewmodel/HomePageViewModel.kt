package com.example.pennywise.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.local.entities.AppSettingsEntity
import com.example.pennywise.remote.Category
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.User
import com.example.pennywise.remote.Wallet
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
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

    private val _savingGoals = MutableLiveData<List<SavingGoal>>()
    val savingGoals: LiveData<List<SavingGoal>> get() = _savingGoals

    init {
        fetchUserData()
        checkAndUploadPredefinedCategories()
        fetchCategories()
    }


    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            repository.getUserData(userId, onSuccess = { user ->
                _userName.postValue(user.firstName)
                Log.d("firstName",user.firstName)

                // Fetch wallet data using the wallet ID from the user document
                repository.getWallet(user.walletId, onSuccess = { wallet ->
                    _walletBalance.postValue(wallet.balance)
                }, onFailure = { exception ->
                    _walletBalance.postValue(0.0) // Default balance
                    _errorMessage.postValue("Failed to fetch wallet data: ${exception.message}")
                })
            }, onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch user data: ${exception.message}")
            })
        } else {
            _errorMessage.postValue("No user is logged in.")
        }
    }

    private fun fetchCategories() {
        repository.getCategories(
            onSuccess = { _categories.postValue(it) },
            onFailure = { /* Handle error */ }
        )
    }

    fun addTransaction(type: String, amount: Double, categoryId: String, date: String) {
        val formattedDate = formatDateToStandard(date) // Format date before saving

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            repository.getUserData(userId, onSuccess = { user ->
                repository.getWallet(user.walletId, onSuccess = { wallet ->
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
                        walletId = user.walletId,
                        date = formattedDate // Use the formatted date
                    )

                    repository.addTransaction(transaction, onSuccess = {
                        repository.updateWalletBalance(user.walletId, updatedBalance, onSuccess = {
                            _walletBalance.postValue(updatedBalance)
                            //fetchTodayTransactions()
                            fetchTodayUserTransactions() // for the logged in user only
                        }, onFailure = {
                            _errorMessage.postValue("Failed to update wallet balance!")
                        })
                    }, onFailure = {
                        _errorMessage.postValue("Failed to add transaction: ${it.message}")
                    })
                }, onFailure = {
                    _errorMessage.postValue("Failed to fetch wallet: ${it.message}")
                })
            }, onFailure = {
                _errorMessage.postValue("Failed to fetch user data: ${it.message}")
            })
        } else {
            _errorMessage.postValue("No user is logged in.")
        }
    }

    private fun formatDateToStandard(date: String): String {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            date // If parsing fails, return the original date
        }
    }







    fun fetchUserTransactions() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch user data to get the wallet ID
            repository.getUserData(userId, onSuccess = { user ->
                val walletId = user.walletId

                // Fetch transactions for the wallet
                repository.getTransactionsForWallet(walletId, onSuccess = { transactions ->
                    _pastTransactions.postValue(transactions) // Update LiveData
                }, onFailure = { exception ->
                    _errorMessage.postValue("Failed to fetch transactions: ${exception.message}")
                })
            }, onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch user data: ${exception.message}")
            })
        } else {
            _errorMessage.postValue("No user is logged in.")
        }
    }

    //for a single user
    fun fetchTodayUserTransactions() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            repository.getTodayTransactionsForUser(
                userId = userId,
                onSuccess = { transactions ->
                    _todayTransactions.postValue(transactions) // Update LiveData with today's transactions
                },
                onFailure = { exception ->
                    _errorMessage.postValue("Failed to fetch today's transactions: ${exception.message}")
                }
            )
        } else {
            _errorMessage.postValue("No user is logged in.")
        }
    }




    //janee
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

    fun fetchSavingGoals(walletId: String) {
        repository.getSavingGoals(walletId,
            onSuccess = { goals ->
                // Filter out completed saving goals
                val uncompletedGoals = goals.filter { it.targetAmount > it.savedAmount }
                _savingGoals.postValue(uncompletedGoals)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch saving goals: ${exception.message}")
            }
        )
    }


    fun addSavingGoal(savingGoal: SavingGoal) {
        repository.addSavingGoal(
            savingGoal,
            onSuccess = { fetchSavingGoals(savingGoal.walletId) },
            onFailure = { _errorMessage.postValue("Failed to add saving goal") }
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
                _errorMessage.postValue("Failed to fetch filtered transactions: ${exception.message}")
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

    fun updateSavingGoal(savingGoal: SavingGoal) {
        repository.updateSavingGoal(
            savingGoal,
            onSuccess = {
                fetchSavingGoals(savingGoal.walletId) // Refresh goals after updating
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to update saving goal: ${exception.message}")
            }
        )
    }

    fun fetchWalletDetails(walletId: String, onSuccess: (Wallet) -> Unit) {
        repository.getWallet(walletId,
            onSuccess = { wallet ->
                onSuccess(wallet)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch wallet details: ${exception.message}")
            }
        )
    }

    fun updateWalletBalance(walletId: String, newBalance: Double) {
        repository.updateWalletBalance(walletId, newBalance,
            onSuccess = {
                _walletBalance.postValue(newBalance) // Update LiveData for the UI
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to update wallet balance: ${exception.message}")
            }
        )
    }


}
