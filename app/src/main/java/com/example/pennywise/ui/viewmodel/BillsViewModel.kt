package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.remote.Bill

class BillsViewModel(private val repository: PennyWiseRepository) : ViewModel() {

    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> get() = _bills



    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    fun addBill(bill: Bill) {
        repository.addBill(bill, { fetchBills(bill.walletId) }, { /* Handle Error */ })
    }

    fun fetchBills(walletId: String) {
        repository.getBills(walletId, { _bills.postValue(it) }, { /* Handle Error */ })
    }

    fun deductBill(bill: Bill) {
        repository.deductBillAmount(bill, bill.walletId,{ fetchBills(bill.walletId) }, { /* Handle Error */ })
    }


    fun payBill(bill: Bill, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deductBillAmount(bill, bill.walletId, onSuccess = {
            onSuccess()
        }, onFailure = { exception ->
            onFailure(exception)
        })
    }



    fun payBill1(bill: Bill) {
        repository.deductBillAmount(bill, bill.walletId,{
            fetchBills(bill.walletId)

        }, { exception ->
            _errorMessage.postValue(exception.message ?: "An error occurred")
        })
    }


}

