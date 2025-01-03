package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _walletBalance = MutableLiveData<Double>()
    val walletBalance: LiveData<Double> get() = _walletBalance

    fun updateWalletBalance(newBalance: Double) {
        _walletBalance.value = newBalance
    }
}
