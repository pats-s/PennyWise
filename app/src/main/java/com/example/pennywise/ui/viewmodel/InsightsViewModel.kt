package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pennywise.PennyWiseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    private fun fetchTransactionsAndCalculateScore(walletId: String) {
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
    }


}

