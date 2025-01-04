package com.example.pennywise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pennywise.PennyWiseRepository

class BillsViewModelFactory(private val repository: PennyWiseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}