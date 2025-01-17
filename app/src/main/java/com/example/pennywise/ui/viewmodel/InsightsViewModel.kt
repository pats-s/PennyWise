package com.example.pennywise.ui.viewmodel

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.remote.SavingGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class InsightsViewModel(private val repository: PennyWiseRepository) : ViewModel() {

    private val _financialHealthScore = MutableLiveData<Int>()
    val financialHealthScore: LiveData<Int> get() = _financialHealthScore

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> get() = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> get() = _totalExpense

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _spendingByCategory = MutableLiveData<List<Pair<String, Double>>>()
    val spendingByCategory: LiveData<List<Pair<String, Double>>> get() = _spendingByCategory

    private val _savingGoals = MutableLiveData<List<SavingGoal>>()
    val savingGoals: LiveData<List<SavingGoal>> get() = _savingGoals


    fun calculateFinancialHealthScore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getUserData(
                userId,
                onSuccess = { user ->
                    fetchTransactionsAndCalculateScore(user.walletId)
                },
                onFailure = { exception ->
                    _errorMessage.postValue("Error fetching user data: ${exception.message}")
                }
            )
        }
    }



    //for a single user

    private fun fetchTransactionsAndCalculateScore(walletId: String) {
        repository.getTransactionsForWallet(walletId,
            onSuccess = { transactions ->
                val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

                _totalIncome.postValue(totalIncome)
                _totalExpense.postValue(totalExpense)

                val score = if (totalIncome > 0) {
                    val ratio = (totalIncome - totalExpense) / totalIncome * 100
                    ratio.coerceIn(0.0, 100.0).toInt()
                } else {
                    0
                }

                _financialHealthScore.postValue(score)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Error fetching transactions: ${exception.message}")
            }
        )
    }


    //function to fetch all the top spending categories
    fun fetchTopSpendingCategories(walletId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getTransactionsForWallet(
                walletId = walletId,
                onSuccess = { transactions ->
                    val spendingByCategory = transactions
                        .filter { it.type == "Expense" }
                        .groupBy { it.categoryId }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second } // Sort by amount (highest to lowest)

                    repository.getAllCategories(
                        onSuccess = { categoriesMap ->
                            val categorySpendingList = spendingByCategory.map { (categoryId, amount) ->
                                val categoryName = categoriesMap[categoryId]?.name ?: "Unknown"
                                Log.d("success", "category added")
                                Pair(categoryName, amount)
                            }
                            _spendingByCategory.postValue(categorySpendingList)
                        },
                        onFailure = { exception ->
                            _errorMessage.postValue("Error fetching categories: ${exception.message}")
                        }
                    )
                },
                onFailure = { exception ->
                    _errorMessage.postValue("Error fetching transactions: ${exception.message}")
                }
            )
        }
    }

    fun fetchSavingGoals(walletId: String) {
        repository.getSavingGoals(walletId,
            onSuccess = { goals ->
                _savingGoals.postValue(goals)
            },
            onFailure = { exception ->
                Log.e("InsightsViewModel", "Error fetching saving goals: ${exception.message}")
            }
        )
    }


    // jane
   /* private fun fetchTransactionsAndCalculateScore(walletId: String) {
        repository.getTransactions(
            onSuccess = { transactions ->
                val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

                _totalIncome.postValue(totalIncome)
                _totalExpense.postValue(totalExpense)

                val score = if (totalIncome > 0) {
                    val ratio = (totalIncome - totalExpense) / totalIncome * 100
                    ratio.coerceIn(0.0, 100.0).toInt()
                } else {
                    0
                }

                _financialHealthScore.postValue(score)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Error fetching transactions: ${exception.message}")
            }
        )
    }*/

//
//
//    fun fetchTopSpendingCategoriesWithFilter(walletId: String?, filterType: String, filterValue: String) {
//        if (walletId != null) {
//            Log.d("WALLET ID",walletId)
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            val (startOfPeriod, endOfPeriod) = when (filterType) {
//                "Day" -> Pair(filterValue, filterValue) // Single day
//                "Week" -> calculateStartAndEndDatesForWeek(filterValue) // Weekly range
//                "Month" -> calculateStartAndEndDatesForMonth(filterValue) // Monthly range
//                else -> Pair(null, null)
//            }
//
//            if (startOfPeriod != null && endOfPeriod != null) {
//                if (walletId != null) {
//                    repository.getFilteredTransactions1(
//                        walletId = walletId,
//                        day = if (filterType == "Day") filterValue else null,
//                        startOfWeekOrMonth = startOfPeriod,
//                        endOfWeek = endOfPeriod,
//                        onSuccess = { transactions ->
//
//                            val spendingByCategory = transactions
//                                .filter { it.type == "Expense" }
//                                .filter { it.walletId == walletId }
//                                .groupBy { it.categoryId }
//                                .mapValues { entry -> entry.value.sumOf { it.amount } }
//                                .toList()
//                                .sortedByDescending { it.second }
//
//                            repository.getAllCategories(
//                                onSuccess = { categoriesMap ->
//                                    val categorySpendingList = spendingByCategory.map { (categoryId, amount) ->
//                                        val categoryName = categoriesMap[categoryId]?.name ?: "Unknown"
//                                        Pair(categoryName, amount)
//                                    }
//                                    Log.d("Filter", "FilterType: $filterType, Start: $startOfPeriod, End: $endOfPeriod")
//
//                                    _spendingByCategory.postValue(categorySpendingList)
//                                },
//                                onFailure = { exception ->
//                                    _errorMessage.postValue("Error fetching categories: ${exception.message}")
//                                }
//                            )
//                        },
//                        onFailure = { exception ->
//                            _errorMessage.postValue("Error fetching transactions: ${exception.message}")
//                        }
//                    )
//                }
//            } else {
//                _errorMessage.postValue("Invalid date range")
//            }
//        }
//    }

/*
    private fun calculateStartAndEndDatesForWeek(selectedDate: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Parse the provided date
        calendar.time = dateFormat.parse(selectedDate) ?: Date()

        // Set to the start of the week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = dateFormat.format(calendar.time)

        // Set to the end of the week
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = dateFormat.format(calendar.time)

        // Debugging the week range
        Log.d("WeeklyFilter", "Start: $startOfWeek, End: $endOfWeek")

        return Pair(startOfWeek, endOfWeek)
    }


    private fun calculateStartAndEndDatesForMonth(selectedDate: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Parse the provided date
        calendar.time = dateFormat.parse(selectedDate) ?: Date()

        // Set to the start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = dateFormat.format(calendar.time)

        // Set to the end of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = dateFormat.format(calendar.time)

        // Debugging the month range
        Log.d("MonthlyFilter", "Start: $startOfMonth, End: $endOfMonth")

        return Pair(startOfMonth, endOfMonth)
    }





    // we were using
    fun fetchTopSpendingCategories1(walletId: String, filterType: String, filterValue: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getFilteredTransactions1(
                walletId = walletId,
                day = if (filterType == "Day") filterValue else null,
                startOfWeekOrMonth = if (filterType == "Week" || filterType == "Month") filterValue else null,
                endOfWeek = if (filterType == "Week") calculateEndOfWeek(filterValue) else null,
                onSuccess = { transactions ->
                    val spendingByCategory = transactions
                        .filter { it.type == "Expense" }
                        .groupBy { it.categoryId }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second }

                    repository.getAllCategories(
                        onSuccess = { categoriesMap ->
                            val categorySpendingList = spendingByCategory.map { (categoryId, amount) ->
                                val categoryName = categoriesMap[categoryId]?.name ?: "Unknown"
                                Pair(categoryName, amount)
                            }
                            _spendingByCategory.postValue(categorySpendingList)
                        },
                        onFailure = { exception ->
                            _errorMessage.postValue("Error fetching categories: ${exception.message}")
                        }
                    )
                },
                onFailure = { exception ->
                    _errorMessage.postValue("Error fetching transactions: ${exception.message}")
                }
            )
        }
    }*/
/*
    // Helper to calculate the end of the week
    private fun calculateEndOfWeek(startOfWeek: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = inputFormat.parse(startOfWeek) ?: Date()
        calendar.add(Calendar.DAY_OF_WEEK, 6) // Add 6 days to get the end of the week
        return inputFormat.format(calendar.time)
    }*/






}

