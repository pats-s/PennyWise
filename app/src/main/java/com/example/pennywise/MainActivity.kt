package com.example.pennywise

import SyncWorker
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Example dynamic walletId (fetch this from your user/session data)
        val dynamicWalletId = 123 // Replace with actual walletId of the logged-in user

        // Schedule the SyncWorker
        scheduleSyncWorker(dynamicWalletId)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun scheduleSyncWorker(walletId: Int) {
        // Create input data with dynamic walletId
        val inputData = Data.Builder()
            .putInt("walletId", walletId)
            .build()

        // Create a one-time work request for SyncWorker
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(inputData)
            .build()

        // Enqueue the work using WorkManager
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}