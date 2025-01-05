package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pennywise.remote.Transaction

class SharedViewModel : ViewModel() {
    private val _walletBalance = MutableLiveData<Double>()
    val walletBalance: LiveData<Double> get() = _walletBalance

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    fun updateWalletBalance(newBalance: Double) {
        _walletBalance.value = newBalance
    }

    fun addTransaction(transaction: Transaction) {
        val currentList = _transactions.value.orEmpty()
        _transactions.value = currentList + transaction
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        val currentList = _transactions.value.orEmpty()

        // Combine old and new, ensuring no duplicates
        val updatedList = (currentList + newTransactions).distinctBy { it.id }
        _transactions.value = updatedList
    }

}